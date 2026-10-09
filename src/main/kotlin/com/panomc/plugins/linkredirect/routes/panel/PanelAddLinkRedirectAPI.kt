package com.panomc.plugins.linkredirect.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.BadRequest
import com.panomc.platform.model.*
import com.panomc.plugins.linkredirect.LinkRedirectPlugin
import com.panomc.plugins.linkredirect.db.dao.LinkRedirectDao
import com.panomc.plugins.linkredirect.db.model.LinkRedirect
import com.panomc.plugins.linkredirect.log.RedirectCreatedLog
import com.panomc.plugins.linkredirect.permission.ManageRedirectsPermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelAddLinkRedirectAPI(
    private val plugin: LinkRedirectPlugin,
    private val linkRedirectDao: LinkRedirectDao
) : PanelApi() {
    override val paths = listOf(Path("/link-redirects", RouteType.POST))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .body(
                com.panomc.platform.schema.dsl.Bodies.json(
                    objectSchema()
                        .requiredProperty("title", stringSchema())
                        .requiredProperty("path", stringSchema())
                        .requiredProperty("targetUrl", stringSchema())
                        .optionalProperty("delay", numberSchema())
                        .optionalProperty("showIntermediatePage", booleanSchema())
                        .optionalProperty("intermediatePageDesign", stringSchema())
                        .optionalProperty("useCustomPage", booleanSchema())
                        .optionalProperty("openInNewTab", booleanSchema())
                        .optionalProperty("showInNavigation", booleanSchema())
                        .optionalProperty("requireLogin", booleanSchema())
                        .optionalProperty("requirePermission", booleanSchema())
                        .optionalProperty("permissionNode", stringSchema())
                        .optionalProperty("htmlContent", stringSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageRedirectsPermission(), context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val title = data.getString("title")
        val path = data.getString("path")
        val targetUrl = data.getString("targetUrl")

        if (title.isNullOrBlank() || path.isNullOrBlank() || targetUrl.isNullOrBlank()) {
            throw BadRequest()
        }

        if (!path.startsWith("/") || path == "/") {
            throw BadRequest()
        }

        if (data.getBoolean("requirePermission") == true && data.getString("permissionNode").isNullOrBlank()) {
            throw BadRequest()
        }

        val sqlClient = databaseManager.getSqlClient()
        
        // Check if path already exists
        if (linkRedirectDao.getByPath(path, sqlClient) != null) {
            throw BadRequest() // Or a more specific error like AlreadyExists
        }

        val redirect = LinkRedirect(
            title = title,
            path = path,
            targetUrl = data.getString("targetUrl"),
            delay = data.getInteger("delay") ?: 0,
            showIntermediatePage = data.getBoolean("showIntermediatePage") ?: false,
            intermediatePageDesign = data.getString("intermediatePageDesign"),
            useCustomPage = data.getBoolean("useCustomPage") ?: false,
            openInNewTab = data.getBoolean("openInNewTab") ?: false,
            showInNavigation = data.getBoolean("showInNavigation") ?: false,
            requireLogin = data.getBoolean("requireLogin") ?: false,
            requirePermission = data.getBoolean("requirePermission") ?: false,
            permissionNode = data.getString("permissionNode"),
            htmlContent = data.getString("htmlContent")
        )

        linkRedirectDao.add(redirect, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            RedirectCreatedLog(userId, username, plugin.pluginId, title),
            sqlClient
        )

        return Successful()
    }
}
