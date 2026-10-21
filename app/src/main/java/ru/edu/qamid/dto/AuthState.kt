package ru.edu.qamid.dto

data class AuthState(
    val accessToken: String,
    val refreshToken: String
)
