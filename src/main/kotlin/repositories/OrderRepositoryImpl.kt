package org.example.repositories

import kotlinx.coroutines.Dispatchers
import org.example.dtos.*
import org.example.repositories.MealRepositoryImpl.Meals
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class OrderRepositoryImpl(database: Database): OrderRepository {
    object Orders : Table() {
        val orderId = long("mealId").autoIncrement()
        val userEmail = varchar("title", length = 50)
        val price = float("price")
        val userAddress = varchar("userAddress", length = 255)
        val status = varchar("status", length = 50)

        override val primaryKey = PrimaryKey(orderId)
    }

    object OrderMealIds : Table() {
        val orderId = long("orderId").references(Orders.orderId)
        val mealId = long("mealId")

        override val primaryKey = PrimaryKey(orderId, mealId)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Meals)
            SchemaUtils.create(OrderMealIds)
        }
    }

    override suspend fun create(order: CreateOrderDTO): Long = dbQuery {
        val orderId = Orders
            .insert {
                it[userEmail] = order.userEmail
                it[price] = order.price
                it[userAddress] = order.userAddress
                it[status] = order.status
            }.resultedValues!!.first()[Orders.orderId]

        order.mealIds.forEach { mealId ->
            OrderMealIds.insert {
                it[OrderMealIds.orderId] = orderId
                it[OrderMealIds.mealId] = mealId
            }
        }

        orderId
    }

    override suspend fun update(id: Long, newStatus: String): Unit = dbQuery {
        Orders
            .update({ Orders.orderId eq id }) {
                it[status] = newStatus
            }
    }

    override suspend fun read(id: Long): OrderDTO? = dbQuery {
        val mealIds = OrderMealIds.select { OrderMealIds.orderId eq id }
            .map { it[OrderMealIds.mealId] }

        Orders
            .select { Orders.orderId eq id }
            .map { OrderDTO(
                it[Orders.orderId],
                it[Orders.userEmail],
                mealIds,
                it[Orders.price],
                it[Orders.userAddress],
                it[Orders.status]
            ) }
            .singleOrNull()
    }

    override suspend fun readAll(): List<OrderDTO> = dbQuery {
        Orders
            .selectAll()
            .map { OrderDTO(
                it[Orders.orderId],
                it[Orders.userEmail],
                OrderMealIds.select { OrderMealIds.orderId eq it[Orders.orderId] }
                    .map { it[OrderMealIds.mealId] },
                it[Orders.price],
                it[Orders.userAddress],
                it[Orders.status],
            ) }
    }

    override suspend fun readAllIncomplete(): List<OrderDTO> = dbQuery {
        Orders
            .select { Orders.status eq "IN PROGRESS" }
            .map { OrderDTO(
                it[Orders.orderId],
                it[Orders.userEmail],
                OrderMealIds.select { OrderMealIds.orderId eq it[Orders.orderId] }
                    .map { it[OrderMealIds.mealId] },
                it[Orders.price],
                it[Orders.userAddress],
                it[Orders.status],
            ) }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}