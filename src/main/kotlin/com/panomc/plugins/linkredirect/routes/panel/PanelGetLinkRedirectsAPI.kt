package com.panomc.plugins.linkredirect.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.linkredirect.LinkRedirectPlugin
import com.panomc.plugins.linkredirect.db.dao.LinkRedirectDao
import com.panomc.plugins.linkredirect.permission.ManageRedirectsPermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository

@Endpoint
class PanelGetLinkRedirectsAPI(
    private val plugin: LinkRedirectPlugin,
    private val linkRedirectDao: LinkRedirectDao
) : PanelApi() {
    override val paths = listOf(Path("/link-redirects", RouteType.GET))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        Paging.params(ValidationHandlerBuilder.create(schemaRepository)).build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageRedirectsPermission(), context)

        val page = Paging.request(context)
        val sqlClient = databaseManager.getSqlClient()

        val totalCount = linkRedirectDao.count(sqlClient)

        Paging.requireInRange(page, totalCount)

        val redirects = linkRedirectDao.getAll(page, sqlClient)

        return Successful(Paging.response(redirects, totalCount, page))
    }
}
