package com.panomc.plugins.linkredirect.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.linkredirect.LinkRedirectPlugin
import com.panomc.plugins.linkredirect.db.dao.LinkRedirectDao
import com.panomc.plugins.linkredirect.log.RedirectDeletedLog
import com.panomc.plugins.linkredirect.permission.ManageRedirectsPermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelDeleteLinkRedirectAPI(
    private val plugin: LinkRedirectPlugin,
    private val linkRedirectDao: LinkRedirectDao
) : PanelApi() {
    override val paths = listOf(Path("/link-redirects/:id", RouteType.DELETE))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("id", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageRedirectsPermission(), context)

        val id = context.pathParam("id").toLong()
        val sqlClient = databaseManager.getSqlClient()
        val existing = linkRedirectDao.getById(id, sqlClient) ?: throw NotFound()

        linkRedirectDao.deleteById(id, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            RedirectDeletedLog(userId, username, plugin.pluginId, existing.title),
            sqlClient
        )

        return Successful()
    }
}
