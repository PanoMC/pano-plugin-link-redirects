package com.panomc.plugins.linkredirect.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
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
class GetLinkRedirectsAPI(
    private val plugin: LinkRedirectPlugin,
    private val linkRedirectDao: LinkRedirectDao
) : Api() {
    override val paths = listOf(Path("/link-redirects", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "The link redirects, for the navigation and the redirect page.",
        tag = "link-redirects",
        response = objectSchema()
            .requiredProperty(
                "items",
                arraySchema().items(
                    objectSchema()
                    .requiredProperty("title", stringSchema())
                    .requiredProperty("path", stringSchema())
                    .requiredProperty("targetUrl", stringSchema())
                    .requiredProperty("delay", intSchema())
                    .requiredProperty("showIntermediatePage", booleanSchema())
                    .requiredProperty("intermediatePageDesign", stringSchema().nullable())
                    .requiredProperty("openInNewTab", booleanSchema())
                    .requiredProperty("showInNavigation", booleanSchema())
                    .requiredProperty("requireLogin", booleanSchema())
                    .requiredProperty("requirePermission", booleanSchema())
                    .requiredProperty("permissionNode", stringSchema().nullable())
                )
            )
    )

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val sqlClient = databaseManager.getSqlClient()
        val redirects = linkRedirectDao.getList(sqlClient)

        val response = redirects.map { redirect ->
            mapOf(
                "title" to redirect.title,
                "path" to redirect.path,
                "targetUrl" to redirect.targetUrl,
                "delay" to redirect.delay,
                "showIntermediatePage" to redirect.showIntermediatePage,
                "intermediatePageDesign" to redirect.intermediatePageDesign,
                "openInNewTab" to redirect.openInNewTab,
                "showInNavigation" to redirect.showInNavigation,
                "requireLogin" to redirect.requireLogin,
                "requirePermission" to redirect.requirePermission,
                "permissionNode" to redirect.permissionNode
            )
        }

        return Successful(mapOf("items" to response))
    }
}
