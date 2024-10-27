package org.example.services

import org.example.dtos.*
import org.example.repositories.CategoryRepository

class CategoryService(private val categoryRepository: CategoryRepository) {
    suspend fun create(category: CreateCategoryDTO): Long = categoryRepository.create(category)

    suspend fun find(id: Long): CategoryDTO? = categoryRepository.read(id)

    suspend fun findAll(): List<CategoryDTO> = categoryRepository.readAll()
}