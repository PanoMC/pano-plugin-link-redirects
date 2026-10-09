package com.panomc.plugins.linkredirect.db.impl

import com.panomc.platform.annotation.Dao
import com.panomc.platform.model.PageRequest
import com.panomc.plugins.linkredirect.db.dao.LinkRedirectDao
import com.panomc.plugins.linkredirect.db.model.LinkRedirect
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.mysqlclient.MySQLClient
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope

@Dao
@Lazy
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class LinkRedirectDaoImpl : LinkRedirectDao() {

    override suspend fun init(sqlClient: SqlClient) {
        sqlClient.query(
            """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                `id` bigint NOT NULL AUTO_INCREMENT,
                `title` varchar(255) NOT NULL,
                `path` varchar(255) NOT NULL,
                `targetUrl` text NOT NULL,
                `delay` int NOT NULL DEFAULT 0,
                `showIntermediatePage` tinyint(1) NOT NULL DEFAULT 0,
                `intermediatePageDesign` varchar(255),
                `useCustomPage` tinyint(1) NOT NULL DEFAULT 0,
                `openInNewTab` tinyint(1) NOT NULL DEFAULT 0,
                `showInNavigation` tinyint(1) NOT NULL DEFAULT 0,
                `requireLogin` tinyint(1) NOT NULL DEFAULT 0,
                `requirePermission` tinyint(1) NOT NULL DEFAULT 0,
                `permissionNode` varchar(255),
                `htmlContent` longtext,
                `createdAt` bigint NOT NULL,
                `updatedAt` bigint NOT NULL,
                PRIMARY KEY (`id`),
                UNIQUE KEY `path` (`path`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """.trimIndent()
        ).execute().coAwait()
    }

    override suspend fun add(redirect: LinkRedirect, sqlClient: SqlClient): Long {
        val query = """
            INSERT INTO `${getTablePrefix() + tableName}` 
            (`title`, `path`, `targetUrl`, `delay`, `showIntermediatePage`, `intermediatePageDesign`, `useCustomPage`, `openInNewTab`, `showInNavigation`, `requireLogin`, `requirePermission`, `permissionNode`, `htmlContent`, `createdAt`, `updatedAt`) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val rows = sqlClient.preparedQuery(query).execute(
            Tuple.of(
                redirect.title,
                redirect.path,
                redirect.targetUrl,
                redirect.delay,
                redirect.showIntermediatePage,
                redirect.intermediatePageDesign,
                redirect.useCustomPage,
                redirect.openInNewTab,
                redirect.showInNavigation,
                redirect.requireLogin,
                redirect.requirePermission,
                redirect.permissionNode,
                redirect.htmlContent,
                redirect.createdAt,
                redirect.updatedAt
            )
        ).coAwait()

        return rows.property(MySQLClient.LAST_INSERTED_ID)
    }

    override suspend fun update(redirect: LinkRedirect, sqlClient: SqlClient) {
        val query = """
            UPDATE `${getTablePrefix() + tableName}` SET 
            `title` = ?, `path` = ?, `targetUrl` = ?, `delay` = ?, `showIntermediatePage` = ?, `intermediatePageDesign` = ?, `useCustomPage` = ?, `openInNewTab` = ?, `showInNavigation` = ?, `requireLogin` = ?, `requirePermission` = ?, `permissionNode` = ?, `htmlContent` = ?, `updatedAt` = ? 
            WHERE `id` = ?
        """.trimIndent()

        sqlClient.preparedQuery(query).execute(
            Tuple.of(
                redirect.title,
                redirect.path,
                redirect.targetUrl,
                redirect.delay,
                redirect.showIntermediatePage,
                redirect.intermediatePageDesign,
                redirect.useCustomPage,
                redirect.openInNewTab,
                redirect.showInNavigation,
                redirect.requireLogin,
                redirect.requirePermission,
                redirect.permissionNode,
                redirect.htmlContent,
                redirect.updatedAt,
                redirect.id
            )
        ).coAwait()
    }

    override suspend fun deleteById(id: Long, sqlClient: SqlClient) {
        sqlClient.preparedQuery("DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?")
            .execute(Tuple.of(id)).coAwait()
    }

    override suspend fun getById(id: Long, sqlClient: SqlClient): LinkRedirect? {
        val rows = sqlClient.preparedQuery("SELECT * FROM `${getTablePrefix() + tableName}` WHERE `id` = ?")
            .execute(Tuple.of(id)).coAwait()
        return rows.toModels().firstOrNull()
    }

    override suspend fun getByPath(path: String, sqlClient: SqlClient): LinkRedirect? {
        val rows = sqlClient.preparedQuery("SELECT * FROM `${getTablePrefix() + tableName}` WHERE `path` = ?")
            .execute(Tuple.of(path)).coAwait()
        return rows.toModels().firstOrNull()
    }

    override suspend fun getAll(page: PageRequest, sqlClient: SqlClient): List<LinkRedirect> {
        val rows = sqlClient.preparedQuery("SELECT * FROM `${getTablePrefix() + tableName}` ORDER BY `id` DESC LIMIT ? OFFSET ?")
            .execute(Tuple.of(page.limit, page.offset)).coAwait()
        return rows.toModels()
    }

    override suspend fun getList(sqlClient: SqlClient): List<LinkRedirect> {
        val rows = sqlClient.query("SELECT * FROM `${getTablePrefix() + tableName}` ORDER BY `id` DESC")
            .execute().coAwait()
        return rows.toModels()
    }

    override suspend fun count(sqlClient: SqlClient): Long {
        val rows = sqlClient.query("SELECT COUNT(*) FROM `${getTablePrefix() + tableName}`").execute().coAwait()
        return rows.first().getLong(0)
    }

    override suspend fun uninstall(sqlClient: SqlClient) {
        sqlClient.query("DROP TABLE IF EXISTS `${getTablePrefix() + tableName}`").execute().coAwait()
    }

    private fun RowSet<Row>.toModels(): List<LinkRedirect> {
        return this.map { row ->
            LinkRedirect(
                id = row.getLong("id")!!,
                title = row.getString("title"),
                path = row.getString("path"),
                targetUrl = row.getString("targetUrl"),
                delay = row.getInteger("delay"),
                showIntermediatePage = row.getBoolean("showIntermediatePage"),
                intermediatePageDesign = row.getString("intermediatePageDesign"),
                useCustomPage = row.getBoolean("useCustomPage"),
                openInNewTab = row.getBoolean("openInNewTab"),
                showInNavigation = row.getBoolean("showInNavigation"),
                requireLogin = row.getBoolean("requireLogin"),
                requirePermission = row.getBoolean("requirePermission"),
                permissionNode = row.getString("permissionNode"),
                htmlContent = row.getString("htmlContent"),
                createdAt = row.getLong("createdAt"),
                updatedAt = row.getLong("updatedAt")
            )
        }
    }
}
