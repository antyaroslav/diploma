package ru.edu.qamid.diploma.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import ru.edu.qamid.R

object MissionPage {
    private val marker = withId(R.id.our_mission_app_bar)
    private val title = withId(R.id.our_mission_title_text_view)
    private val list = withId(R.id.our_mission_item_list_recycler_view)
    private val firstCard = allOf(withId(R.id.our_mission_item_material_card_view),
        hasDescendant(withText(containsString("Хоспис для меня"))))
    private val firstDescription = allOf(withId(R.id.our_mission_item_description_text_view), isDescendantOfA(firstCard))
    fun assertOpen() = Allure.step("Verify Our Mission screen") {
        Ui.ready(R.id.our_mission_app_bar)
        onView(marker).check(matches(isDisplayed()))
    }
    fun assertQuotes() = Allure.step("Verify mission heading and eight quotes") {
        assertOpen()
        onView(title).check(matches(withText(R.string.our_mission_title_text)))
        Ui.await("eight mission quotes") {
            (Ui.find(R.id.our_mission_item_list_recycler_view) as? RecyclerView)?.adapter?.itemCount == 8
        }
        onView(list).check { view, _ -> assertEquals(8, (view as RecyclerView).adapter?.itemCount) }
    }
    fun expandFirstQuote() = Allure.step("Expand and collapse the first quote") {
        onView(list).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(firstDescription).check(matches(isDisplayed()))
        onView(list).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(firstDescription).check(matches(not(isDisplayed())))
    }
}
