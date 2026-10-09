package com.panomc.plugins.linkredirect.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PermissionManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NoPermission
import com.panomc.platform.error.NotFound
import com.panomc.platform.error.NotLoggedIn
import com.panomc.platform.model.*
import com.panomc.plugins.linkredirect.LinkRedirectPlugin
import com.panomc.plugins.linkredirect.db.dao.LinkRedirectDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class GetLinkRedirectByPathAPI(
    private val plugin: LinkRedirectPlugin,
    private val linkRedirectDao: LinkRedirectDao
) : Api() {

    override val paths = listOf(Path("/link-redirects/check", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "One redirect by its path, when the visitor may use it.",
        tag = "link-redirects",
        response = objectSchema()
            .requiredProperty("requireLogin", booleanSchema())
            .requiredProperty("requirePermission", booleanSchema())
            .requiredProperty("delay", intSchema())
            .requiredProperty("targetUrl", stringSchema())
            .requiredProperty("showIntermediatePage", booleanSchema())
            .requiredProperty("intermediatePageDesign", stringSchema().nullable())
            .requiredProperty("useCustomPage", booleanSchema())
            .requiredProperty("openInNewTab", booleanSchema())
            .requiredProperty("htmlContent", stringSchema().nullable())
            .requiredProperty("allowed", booleanSchema()),
        errors = listOf(NotFound::class, NotLoggedIn::class, NoPermission::class)
    )

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val permissionManager: PermissionManager by lazy {
        plugin.applicationContext.getBean(PermissionManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val path = context.request().getParam("path") ?: throw NotFound()

        val sqlClient = databaseManager.getSqlClient()
        val redirect = linkRedirectDao.getByPath(path, sqlClient) ?: throw NotFound()

        if (redirect.requireLogin) {
            val userId = authProvider.getUserIdFromRoutingContext(context)
            if (userId == null) {
                throw NotLoggedIn()
            }

            if (redirect.requirePermission && redirect.permissionNode != null) {
                val hasPermission = permissionManager.hasPermissionNode(userId, redirect.permissionNode)
                if (!hasPermission) {
                    throw NoPermission()
                }
            }
        }

        val response = mapOf(
            "requireLogin" to redirect.requireLogin,
            "requirePermission" to redirect.requirePermission,
            "delay" to redirect.delay,
            "targetUrl" to redirect.targetUrl,
            "showIntermediatePage" to redirect.showIntermediatePage,
            "intermediatePageDesign" to redirect.intermediatePageDesign,
            "useCustomPage" to redirect.useCustomPage,
            "openInNewTab" to redirect.openInNewTab,
            "htmlContent" to redirect.htmlContent,
            "allowed" to true
        )

        return Successful(response)
    }
}
