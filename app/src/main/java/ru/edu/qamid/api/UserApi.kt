package ru.edu.qamid.api

import retrofit2.Response
import retrofit2.http.GET
import ru.edu.qamid.dto.User

interface UserApi {
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("authentication/userInfo")
    suspend fun getUserInfo(): Response<User>
}
