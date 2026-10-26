package ru.edu.qamid.diploma.pages

import android.content.res.Configuration
import android.graphics.Color
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.textfield.TextInputLayout
import io.qameta.allure.kotlin.Allure
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import ru.edu.qamid.R
import ru.edu.qamid.diploma.data.Credentials
import ru.edu.qamid.diploma.data.TestData

object AuthPage {
    private val marker = withId(R.id.auth_app_bar)
    private val loginField = withId(R.id.login_edit_text)
    private val passwordField = withId(R.id.password_edit_text)
    private val submit = withId(R.id.enter_button)
    private val root = isRoot()

    fun normalizeSession() = Allure.step("Start from a signed-out session") {
        Ui.await("authorization or authenticated toolbar") {
            Ui.visible(R.id.auth_app_bar) || Ui.visible(R.id.authorization_image_button)
        }
        var signedIn = false
        onView(root).check { _, _ -> signedIn = Ui.visible(R.id.authorization_image_button) }
        if (signedIn) AppMenu.logout()
        assertOpen()
    }
    fun assertOpen() = Allure.step("Verify Authorization screen") {
        Ui.ready(R.id.auth_app_bar)
        onView(marker).check(matches(isDisplayed()))
        onView(submit).check(matches(isDisplayed()))
    }
    fun login(credentials: Credentials) = Allure.step("Sign in with the configured account") {
        onView(loginField).perform(Ui.secretText(credentials.login))
        onView(passwordField).perform(Ui.secretText(credentials.password))
        Ui.hideKeyboard()
        onView(submit).perform(click())
        MainPage.assertOpen()
    }
    fun submitEmpty() = Allure.step("Submit empty credentials and remain signed out") {
        onView(loginField).perform(replaceText(""))
        onView(passwordField).perform(replaceText(""))
        Ui.hideKeyboard()
        onView(submit).perform(click())
        assertOpen()
        onView(loginField).check(matches(withText("")))
        onView(passwordField).check(matches(withText("")))
        onView(submit).check(matches(isEnabled()))
        onView(root).check { _, _ -> assertFalse("Must remain signed out", Ui.visible(R.id.main_app_bar)) }
    }
    fun assertNightMode() = Allure.step("Verify night resources are active") {
        onView(root).check { view, _ ->
            assertEquals(Configuration.UI_MODE_NIGHT_YES,
                view.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        }
    }
    fun assertDarkFieldContrast() = Allure.step("Verify synthetic login text contrast is at least 4.5:1") {
        assertNightMode()
        onView(loginField).perform(replaceText(TestData.contrastCredentials.login))
        onView(passwordField).perform(replaceText(TestData.contrastCredentials.password))
        Ui.hideKeyboard()
        Ui.screenshot("Night mode: synthetic input only")
        onView(loginField).check { view, _ ->
            val text = view as TextView
            val input = Ui.find(R.id.login_text_input_layout) as TextInputLayout
            val background = input.boxBackgroundColor
            assertEquals("Input background must be opaque", 255, Color.alpha(background))
            val foreground = ColorUtils.compositeColors(text.currentTextColor, background)
            val ratio = ColorUtils.calculateContrast(foreground, background)
            Allure.attachment("Login field contrast ratio", ratio.toString(), "text/plain")
            assertTrue("Expected contrast >= 4.5:1, actual=$ratio", ratio >= 4.5)
        }
    }
}
