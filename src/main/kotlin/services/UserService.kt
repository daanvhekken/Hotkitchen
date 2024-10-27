package org.example.services

import org.example.dtos.CreateUserDto
import org.example.dtos.UpdateUserDto
import org.example.dtos.UserDto
import org.example.repositories.UserRepository


class UserService(private val userRepository: UserRepository) {
    suspend fun create(user: CreateUserDto): Long = userRepository.create(user)

    suspend fun find(id: Long): UserDto? = userRepository.read(id)

    suspend fun update(id: Long, user: UpdateUserDto) = userRepository.update(id, user)

    suspend fun delete(id: Long) = userRepository.delete(id)

    suspend fun findByEmail(email: String): UserDto? = userRepository.findByEmail(email)

    suspend fun authenticate(email: String, password: String): UserDto? {
        val user = findByEmail(email)

        if (user == null || user.password != password)
            return null

        return user
    }
}