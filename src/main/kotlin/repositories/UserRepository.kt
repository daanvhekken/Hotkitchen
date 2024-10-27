package org.example.repositories

import org.example.dtos.*

interface UserRepository {
    suspend fun create(user: CreateUserDto): Long
    suspend fun read(id: Long): UserDto?
    suspend fun update(id: Long, user: UpdateUserDto)
    suspend fun delete(id: Long)
    suspend fun findByEmail(email: String): UserDto?
}