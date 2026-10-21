package ru.edu.qamid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.edu.qamid.auth.AppAuth
import ru.edu.qamid.exceptions.AuthorizationException
import ru.edu.qamid.exceptions.LostConnectException
import ru.edu.qamid.exceptions.UnknownException
import ru.edu.qamid.repository.authRepository.AuthRepository
import ru.edu.qamid.repository.userRepository.UserRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    val nonAuthorizedEvent = MutableSharedFlow<Unit>()
    val authorizedEvent = MutableSharedFlow<Unit>()
    val loginExceptionEvent = MutableSharedFlow<Unit>()
    val loginEvent = MutableSharedFlow<Unit>()
    val getUserListExceptionEvent = MutableSharedFlow<Unit>()
    val userListLoadedEvent = MutableSharedFlow<Unit>()
    val authorizationFailedExceptionEvent = MutableSharedFlow<Unit>()
    val lostConnectionExceptionEvent = MutableSharedFlow<Unit>()

    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading = _isLoginLoading.asStateFlow()

    fun login(login: String, password: String) {
        if (_isLoginLoading.value) return
        viewModelScope.launch {
            _isLoginLoading.value = true
            try {
                authRepository.login(login, password)
                userRepository.getUserInfo()
                loginEvent.emit(Unit)
            } catch (e: LostConnectException) {
                e.printStackTrace()
                lostConnectionExceptionEvent.emit(Unit)
            } catch (e: AuthorizationException) {
                e.printStackTrace()
                authorizationFailedExceptionEvent.emit(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                loginExceptionEvent.emit(Unit)
            } finally {
                _isLoginLoading.value = false
            }
        }
    }

    fun authorization() {
        viewModelScope.launch {
            val authState = appAuth.authState
            if (authState == null) {
                nonAuthorizedEvent.emit(Unit)
            } else {
                try {
                    userRepository.getUserInfo()
                    authorizedEvent.emit(Unit)
                } catch (e: AuthorizationException) {
                    nonAuthorizedEvent.emit(Unit)
                } catch (e: UnknownException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun logOut() {
        appAuth.authState = null
        userRepository.userLogOut()
    }

    suspend fun loadUserList() {
        try {
            userRepository.getAllUsers()
            userListLoadedEvent.emit(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            getUserListExceptionEvent.emit(Unit)
        }
    }
}
