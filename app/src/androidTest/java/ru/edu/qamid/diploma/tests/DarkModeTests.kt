package ru.edu.qamid.diploma.tests

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.diploma.support.BaseUiTest

@RunWith(AndroidJUnit4::class)
@Feature("Authorization") @Story("Night mode") @Severity(SeverityLevel.NORMAL)
class DarkModeTests : BaseUiTest() {
    override val nightMode = AppCompatDelegate.MODE_NIGHT_YES

    @Test @Description("TC-065: night mode is active and valid credentials still authorize")
    fun validCredentialsAuthorizeInNightMode() { AuthPage.assertNightMode(); login(); MainPage.assertOpen() }

    @Test @Description("TC-066: synthetic text in the night-mode login field has >= 4.5:1 contrast")
    @Issue("BUG-002")
    fun loginFieldHasReadableContrastInNightMode() { AuthPage.assertDarkFieldContrast() }
}
