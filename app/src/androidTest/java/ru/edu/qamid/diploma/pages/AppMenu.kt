package ru.edu.qamid.diploma.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import org.hamcrest.Matchers.allOf
import ru.edu.qamid.R

/** Shared toolbar actions; page identity is asserted by the destination page. */
object AppMenu {
    private val menuButton = allOf(withId(R.id.main_menu_image_button), isDisplayed())
    private val missionButton = allOf(withId(R.id.our_mission_image_button), isDisplayed())
    private val authButton = allOf(withId(R.id.authorization_image_button), isDisplayed())
    private val mainItem = withText(R.string.main)
    private val newsItem = withText(R.string.news)
    private val logoutItem = withText(R.string.log_out)

    fun main() = Allure.step("Navigate to Main") {
        onView(menuButton).perform(click())
        onView(mainItem).perform(click())
        MainPage.assertOpen()
    }
    fun news() = Allure.step("Navigate to News") {
        onView(menuButton).perform(click())
        onView(newsItem).perform(click())
        NewsPage.assertOpen()
    }
    fun mission() = Allure.step("Navigate to Our Mission") {
        onView(missionButton).perform(click())
        MissionPage.assertOpen()
    }
    fun logout() = Allure.step("Log out through the current section toolbar") {
        onView(authButton).perform(click())
        onView(logoutItem).perform(click())
        AuthPage.assertOpen()
    }
}
