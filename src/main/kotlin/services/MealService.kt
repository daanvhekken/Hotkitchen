package org.example.services

import org.example.dtos.*
import org.example.repositories.MealRepository

class MealService(private val mealRepository: MealRepository) {
    suspend fun create(meal: CreateMealDTO): Long = mealRepository.create(meal)
    suspend fun find(id: Long): MealDTO? = mealRepository.read(id)
    suspend fun findAll(): List<MealDTO> = mealRepository.readAll()
}