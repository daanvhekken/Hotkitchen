package org.example.repositories

import org.example.dtos.*

interface OrderRepository {
    suspend fun create(order: CreateOrderDTO): Long
    suspend fun read(id: Long): OrderDTO?
    suspend fun readAll(): List<OrderDTO>
    suspend fun update(id: Long, status: String)
    suspend fun readAllIncomplete(): List<OrderDTO>
}