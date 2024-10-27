package org.example.repositories

import kotlinx.coroutines.Dispatchers
import org.example.dtos.CreateUserDto
import org.example.dtos.UpdateUserDto
import org.example.dtos.UserDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserRepositoryImpl(database: Database) : UserRepository {
    object Users : Table() {
        val id = long("id").autoIncrement()
        val userType = varchar("userType", length = 50)
        val email = varchar("email", length = 50)
        val password = varchar("password", length = 512)
        val name = varchar("name", length = 50)
        val phone = varchar("phone", length = 50)
        val address = varchar("address", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    override suspend fun create(user: CreateUserDto): Long = dbQuery {
        Users
            .insert {
                it[userType] = user.userType
                it[email] = user.email
                it[password] = user.password
                it[name] = user.password
                it[phone] = user.phone
                it[address] = user.address
            }[Users.id]
    }

    override suspend fun read(id: Long): UserDto? = dbQuery {
        Users
            .select { Users.id eq id }
            .map { UserDto(
                it[Users.id],
                it[Users.email],
                it[Users.userType],
                it[Users.password],
                it[Users.name],
                it[Users.phone],
                it[Users.address]
            ) }
            .singleOrNull()
    }

    override suspend fun update(id: Long, user: UpdateUserDto): Unit = dbQuery {
        Users
            .update({ Users.id eq id }) {
                it[name] = user.name
                it[userType] = user.userType
                it[phone] = user.phone
                it[address] = user.address
            }
    }

    override suspend fun delete(id: Long): Unit = dbQuery {
        Users
            .deleteWhere { Users.id.eq(id) }
    }

    override suspend fun findByEmail(email: String): UserDto? = dbQuery {
        Users
            .select { Users.email eq email }
            .map { UserDto(
                it[Users.id],
                it[Users.email],
                it[Users.userType],
                it[Users.password],
                it[Users.name],
                it[Users.phone],
                it[Users.address]
            ) }
            .singleOrNull()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}