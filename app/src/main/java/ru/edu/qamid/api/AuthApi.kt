package ru.edu.qamid.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.edu.qamid.dto.AuthState
import ru.edu.qamid.dto.LoginData

interface AuthApi {
    @POST("authentication/login")
    suspend fun getTokens(
        @Body loginData: LoginData
    ): Response<AuthState>
}
