package org.example.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryDTO(val title: String, val description: String)

@Serializable
data class CategoryDTO(val id: Long, val title: String, val description: String)