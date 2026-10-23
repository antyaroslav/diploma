package ru.edu.qamid.diploma.pages

import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import ru.edu.qamid.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AuthPage {
    fun normalizeSession() {
        Ui.await("authorization or main screen") { Ui.visible(R.id.enter_button) || Ui.visible(R.id.main_menu_image_button) }
        var loggedIn = false
        onView(isRoot()).check { _, _ -> loggedIn = Ui.visible(R.id.main_menu_image_button) }
        if (loggedIn) MainPage.logout()
        Ui.ready(R.id.enter_button)
    }

    fun login(login: String, password: String) {
        Allure.step("Sign in using the configured educational account")
        onView(withId(R.id.login_edit_text)).perform(replaceText(login))
        onView(withId(R.id.password_edit_text)).perform(Ui.secretText(password), closeSoftKeyboard())
        onView(withId(R.id.enter_button)).perform(click())
        MainPage.assertOpen()
    }

    fun submitEmpty() {
        Allure.step("Submit empty authorization fields")
        onView(withId(R.id.enter_button)).perform(click())
        onView(withId(R.id.login_edit_text)).check(matches(withText("")))
        onView(withId(R.id.password_edit_text)).check(matches(withText("")))
        onView(withId(R.id.enter_button)).check(matches(isEnabled()))
        onView(withId(R.id.main_menu_image_button)).check(matches(not(isDisplayed())))
    }

    fun assertOpen() { Ui.ready(R.id.enter_button) }
}

object MainPage {
    fun assertOpen() { Ui.ready(R.id.main_menu_image_button) }
    fun news() {
        Allure.step("Open News from the main menu")
        onView(withId(R.id.main_menu_image_button)).perform(click())
        onView(withText(R.string.news)).perform(click())
        Ui.ready(R.id.news_edit_button)
    }
    fun mission() {
        Allure.step("Open Our Mission")
        onView(withId(R.id.our_mission_image_button)).perform(click())
        Ui.ready(R.id.our_mission_title_text_view)
    }
    fun logout() {
        Allure.step("Log out")
        onView(withId(R.id.authorization_image_button)).perform(click())
        onView(withText(R.string.log_out)).perform(click())
        Ui.ready(R.id.enter_button)
    }
}

object NewsPage {
    fun panel() {
        Allure.step("Open the news control panel")
        onView(withId(R.id.news_edit_button)).perform(click())
        Ui.ready(R.id.add_news_image_view)
    }
    fun invalidIncompletePeriod() {
        Allure.step("Apply a date filter with only its start filled")
        onView(withId(R.id.news_filter_button)).perform(click())
        Ui.ready(R.id.filter_news_date_start_edit_text)
        onView(withId(R.id.filter_news_date_start_edit_text)).perform(click())
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_news_apply_button)).perform(click())
        onView(withText(R.string.wrong_news_date_period)).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.filter_news_cancel_button)).perform(click())
    }
}

object NewsForm {
    fun create(title: String) {
        Allure.step("Create a unique test news item")
        onView(withId(R.id.add_news_image_view)).perform(click())
        Ui.ready(R.id.news_save_button)
        onView(allOf(withId(com.google.android.material.R.id.text_input_end_icon),
            isDescendantOfA(withId(R.id.news_category_text_input_layout)))).perform(click())
        onData(equalTo("Объявление"))
            .inRoot(androidx.test.espresso.matcher.RootMatchers.isPlatformPopup()).perform(click())
        onView(withId(R.id.news_title_edit_text)).perform(replaceText(title), closeSoftKeyboard())
        val today = LocalDate.now()
        onView(withId(R.id.news_publish_date_edit_text)).perform(click())
        onView(isAssignableFrom(DatePicker::class.java)).perform(PickerActions.setDate(today.year, today.monthValue, today.dayOfMonth))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.news_publish_time_edit_text)).perform(click())
        onView(isAssignableFrom(TimePicker::class.java)).perform(PickerActions.setTime(0, 1))
        onView(withId(android.R.id.button1)).perform(click())
        onView(withId(R.id.news_description_edit_text)).perform(replaceText("Diploma UI test"), closeSoftKeyboard())
        onView(withId(R.id.news_save_button)).perform(scrollTo(), click())
        Ui.ready(R.id.add_news_image_view)
        Ui.await("news form transition completed") { !Ui.visible(R.id.news_save_button) }
        PanelPage.show(title)
    }

    fun cancelIfOpen() {
        var open = false
        onView(isRoot()).check { _, _ -> open = Ui.visible(R.id.news_cancel_button) }
        if (open) {
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            androidx.test.espresso.Espresso.pressBack()
            Ui.ready(R.id.add_news_image_view)
        }
    }
}

object PanelPage {
    private fun card(title: String) = allOf(withId(R.id.news_item_material_card_view), hasDescendant(withText(title)))
    fun deleteIfPresent(title: String) {
        var present = false
        onView(isRoot()).check { _, _ ->
            val adapter = (Ui.find(R.id.news_list_recycler_view) as? RecyclerView)?.adapter as? androidx.recyclerview.widget.ListAdapter<*, *>
            present = adapter?.currentList?.any { it.toString().contains(title) } == true
        }
        if (present) delete(title)
    }
    fun show(title: String) {
        Ui.await("saved news item") {
            val list = Ui.find(R.id.news_list_recycler_view) as? RecyclerView
            (0 until (list?.adapter?.itemCount ?: 0)).any { i ->
                val adapter = list?.adapter as? androidx.recyclerview.widget.ListAdapter<*, *>
                adapter?.currentList?.getOrNull(i)?.toString()?.contains(title) == true
            }
        }
        onView(withId(R.id.news_list_recycler_view)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(card(title)))
    }
    fun assertTitle(title: String) {
        Allure.step("Verify the created news item")
        show(title)
        onView(allOf(withId(R.id.news_item_title_text_view), withText(title))).check(matches(isDisplayed()))
    }
    fun assertCreationDate(title: String, date: LocalDate) {
        Allure.step("BUG-001: creation date must equal the creation day") {
            show(title)
            val screenshot = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            val bytes = java.io.ByteArrayOutputStream()
            screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bytes)
            Allure.attachment("Creation date evidence", bytes.toByteArray().inputStream(), "image/png", ".png")
            screenshot.recycle()
            onView(allOf(withId(R.id.news_item_create_date_text_view), isDescendantOfA(card(title))))
                .check(matches(withText(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
        }
    }
    fun delete(title: String) {
        Allure.step("Delete only this test's own news item")
        show(title)
        onView(allOf(withId(R.id.news_item_delete_image_view), isDescendantOfA(card(title)))).perform(click())
        onView(withId(android.R.id.button1)).perform(click())
        Ui.await("news deletion") {
            val adapter = (Ui.find(R.id.news_list_recycler_view) as? RecyclerView)?.adapter as? androidx.recyclerview.widget.ListAdapter<*, *>
            adapter?.currentList?.none { it.toString().contains(title) } == true
        }
    }
}

object MissionPage {
    fun expandFirstQuote() {
        Allure.step("Expand and collapse the first mission quote")
        onView(withId(R.id.our_mission_item_list_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        val description = allOf(withId(R.id.our_mission_item_description_text_view),
            isDescendantOfA(allOf(withId(R.id.our_mission_item_material_card_view),
                hasDescendant(withText(containsString("Хоспис для меня"))))))
        onView(description).check(matches(isDisplayed()))
        onView(withId(R.id.our_mission_item_list_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(description).check(matches(not(isDisplayed())))
    }
    fun assertQuotes() {
        Allure.step("Verify the mission heading and eight quotes")
        onView(withId(R.id.our_mission_title_text_view)).check(matches(withText(R.string.our_mission_title_text)))
        onView(withId(R.id.our_mission_item_list_recycler_view)).check { view, _ ->
            assertEquals(8, (view as RecyclerView).adapter?.itemCount)
        }
    }
}
