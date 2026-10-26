package ru.edu.qamid.diploma.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import ru.edu.qamid.R

object MainPage {
    private val marker = withId(R.id.main_app_bar)
    private val allNews = withId(R.id.all_news_text_view)
    fun assertOpen() = Allure.step("Verify Main screen") {
        Ui.ready(R.id.main_app_bar)
        onView(marker).check(matches(isDisplayed()))
    }
    fun allNews() = Allure.step("Open ALL NEWS shortcut") {
        onView(allNews).perform(click())
        NewsPage.assertOpen()
    }
    fun news() = AppMenu.news()
    fun mission() = AppMenu.mission()
    fun logout() = AppMenu.logout()
}
