package org.example.repositories

import org.example.dtos.*

interface MealRepository {
    suspend fun create(meal: CreateMealDTO): Long
    suspend fun read(id: Long): MealDTO?
    suspend fun readAll(): List<MealDTO>
}