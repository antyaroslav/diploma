package ru.edu.qamid.repository.userRepository

import ru.edu.qamid.dto.User

interface UserRepository {
    val currentUser: User
    val userList: List<User>
    suspend fun getAllUsers(): List<User>
    suspend fun getUserInfo()
    fun userLogOut()
}