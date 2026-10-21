package ru.edu.qamid.repository.authRepository

import ru.edu.qamid.dto.AuthState

interface AuthRepository {
    suspend fun login(login: String, password: String)

    /**
     * Обновляет токены при помощи [refreshToken]
     *
     * @return обновленные токены.
     * Если [refreshToken] истек возвращает `null`.
     */
    suspend fun updateTokens(refreshToken: String): AuthState?
}
