package org.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderDTO(val userEmail: String, val mealIds: List<Long>, val price: Float, val userAddress: String, val status: String)

@Serializable
data class OrderDTO(val orderId: Long, val userEmail: String, val mealsIds: List<Long>, val price: Float, val userAddress: String, val status: String)

@Serializable
data class UpdateOrderDTO(val status: String)