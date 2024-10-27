package org.example.repositories

import org.example.dtos.*

interface CategoryRepository {
    suspend fun create(user: CreateCategoryDTO): Long
    suspend fun read(id: Long): CategoryDTO?
    suspend fun readAll(): List<CategoryDTO>
    suspend fun delete(id: Long)
}