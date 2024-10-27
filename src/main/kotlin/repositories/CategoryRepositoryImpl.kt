package org.example.repositories

import kotlinx.coroutines.Dispatchers
import org.example.dtos.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepositoryImpl(database: Database): CategoryRepository {
    object Categories : Table() {
        val categoryId = long("categoryId").autoIncrement()
        val title = varchar("title", length = 50)
        val description = varchar("description", length = 255)

        override val primaryKey = PrimaryKey(categoryId)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Categories)
        }
    }

    override suspend fun create(user: CreateCategoryDTO): Long = dbQuery {
        Categories
            .insert {
                it[title] = user.title
                it[description] = user.description
            }[Categories.categoryId]
    }

    override suspend fun read(id: Long): CategoryDTO? = dbQuery {
        Categories
            .select { Categories.categoryId eq id }
            .map { CategoryDTO(
                it[Categories.categoryId],
                it[Categories.title],
                it[Categories.description]
            ) }
            .singleOrNull()
    }

    override suspend fun readAll(): List<CategoryDTO> = dbQuery {
        Categories
            .selectAll()
            .map { CategoryDTO(
                it[Categories.categoryId],
                it[Categories.title],
                it[Categories.description]
            ) }
    }

    override suspend fun delete(id: Long): Unit = dbQuery {
        Categories.deleteWhere { categoryId.eq(id) }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}