package ru.edu.qamid.diploma

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*

@RunWith(AndroidJUnit4::class)
@Feature("Authorization")
class AuthTests : BaseUiTest() {
    @Test @Description("TC-002: valid credentials open the main screen")
    fun validCredentialsOpenMainScreen() { login(); MainPage.assertOpen() }

    @Test @Description("TC-003: empty credentials cannot authorize")
    fun emptyCredentialsRemainOnAuthorizationScreen() { AuthPage.submitEmpty() }

    @Test @Description("TC-014: logout returns to authorization")
    fun logoutReturnsToAuthorization() { login(); MainPage.logout(); AuthPage.assertOpen() }
}
