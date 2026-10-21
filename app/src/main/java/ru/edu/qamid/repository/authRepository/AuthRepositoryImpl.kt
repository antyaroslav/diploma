package ru.edu.qamid.repository.authRepository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.edu.qamid.api.AuthApi
import ru.edu.qamid.api.RefreshTokensApi
import ru.edu.qamid.auth.AppAuth
import ru.edu.qamid.dto.AuthState
import ru.edu.qamid.dto.JwtResponse
import ru.edu.qamid.dto.LoginData
import ru.edu.qamid.dto.RefreshRequest
import ru.edu.qamid.exceptions.ApiException
import ru.edu.qamid.exceptions.AuthorizationException
import ru.edu.qamid.exceptions.UnknownException
import ru.edu.qamid.utils.Utils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val refreshTokensApi: RefreshTokensApi,
    private val appAuth: AppAuth
) : AuthRepository {

    override suspend fun login(login: String, password: String) =
        Utils.makeRequest(
            request = { authApi.getTokens(LoginData(login = login, password = password)) },
            onSuccess = { body -> appAuth.authState = body },
            onFailure = {
                // Было бы здорово вынести этот код в отдельную функцию.
                val gson = Gson()
                val type = object : TypeToken<JwtResponse>() {}.type
                val errorResponse: JwtResponse? = gson.fromJson(it.errorBody()?.charStream(), type)
                if (errorResponse?.message.equals("ERR_INVALID_LOGIN")) {
                    throw AuthorizationException
                } else {
                    throw UnknownException
                }
            }
        )

    override suspend fun updateTokens(refreshToken: String): AuthState? =
        Utils.makeRequest(
            request = {
                refreshTokensApi.refreshTokens(
                    refreshToken,
                    RefreshRequest(refreshToken)
                )
            },
            onSuccess = { body ->
                appAuth.authState = body
                body
            },
            onFailure = {
                if (it.code() == 401) {
                    appAuth.authState = null
                    null
                } else {
                    throw ApiException(it.code(), it.message())
                }
            }
        )
}
