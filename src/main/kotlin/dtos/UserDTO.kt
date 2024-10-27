package org.example.dtos

import kotlinx.serialization.Serializable

class UserPrincipal(val user: UserDto)

@Serializable
data class CreateUserDto(val email: String, val userType: String, val password: String, val name: String, val phone: String, val address: String)

@Serializable
data class UserDto(val id: Long, val email: String, val userType: String, val password: String, val name: String, val phone: String, val address: String)

@Serializable
data class UpdateUserDto(val userType: String, val name: String, val phone: String, val address: String)