package org.example.services

import org.example.dtos.*
import org.example.repositories.OrderRepository

class OrderService(private val orderRepository: OrderRepository) {
    suspend fun create(order: CreateOrderDTO): Long = orderRepository.create(order)
    suspend fun find(id: Long): OrderDTO? = orderRepository.read(id)
    suspend fun findAll(): List<OrderDTO> = orderRepository.readAll()
    suspend fun markReady(id: Long) = orderRepository.update(id, "COMPLETE")
    suspend fun findAllIncomplete(): List<OrderDTO> = orderRepository.readAllIncomplete()
}