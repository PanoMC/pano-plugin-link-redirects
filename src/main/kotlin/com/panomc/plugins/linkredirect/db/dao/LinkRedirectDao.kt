package com.panomc.plugins.linkredirect.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.model.PageRequest
import com.panomc.plugins.linkredirect.db.model.LinkRedirect
import io.vertx.sqlclient.SqlClient

abstract class LinkRedirectDao : Dao<LinkRedirect>(LinkRedirect::class.java) {
    abstract suspend fun add(redirect: LinkRedirect, sqlClient: SqlClient): Long
    abstract suspend fun update(redirect: LinkRedirect, sqlClient: SqlClient)
    abstract suspend fun deleteById(id: Long, sqlClient: SqlClient)
    abstract suspend fun getById(id: Long, sqlClient: SqlClient): LinkRedirect?
    abstract suspend fun getByPath(path: String, sqlClient: SqlClient): LinkRedirect?
    abstract suspend fun getAll(page: PageRequest, sqlClient: SqlClient): List<LinkRedirect>
    abstract suspend fun getList(sqlClient: SqlClient): List<LinkRedirect>
    abstract suspend fun count(sqlClient: SqlClient): Long
}
