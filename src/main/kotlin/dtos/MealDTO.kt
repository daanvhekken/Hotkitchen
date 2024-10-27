package org.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateMealDTO(val title: String, val price: Float, val imageUrl: String, val categoryIds: List<Long>)

@Serializable
data class MealDTO(val id: Long, val title: String, val price: Float, val imageUrl: String, val categories: List<Long>)