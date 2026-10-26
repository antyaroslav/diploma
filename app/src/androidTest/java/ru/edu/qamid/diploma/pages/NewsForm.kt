package ru.edu.qamid.diploma.pages

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import org.hamcrest.Matchers.*
import ru.edu.qamid.R
import ru.edu.qamid.diploma.data.NewsDraft
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object NewsForm {
    private val saveButton = withId(R.id.news_save_button)
    private val cancelButton = withId(R.id.news_cancel_button)
    private val categoryField = withId(R.id.news_category_auto_complete)
    private val categoryArrow = allOf(withId(com.google.android.material.R.id.text_input_end_icon),
        isDescendantOfA(withId(R.id.news_category_text_input_layout)))
    private val titleField = withId(R.id.news_title_edit_text)
    private val descriptionField = withId(R.id.news_description_edit_text)
    private val dateField = withId(R.id.news_publish_date_edit_text)
    private val timeField = withId(R.id.news_publish_time_edit_text)
    private val activeSwitch = withId(R.id.news_active_switch)
    private val datePicker = isAssignableFrom(DatePicker::class.java)
    private val timePicker = isAssignableFrom(TimePicker::class.java)
    private val dialogOk = withId(android.R.id.button1)
    private val dialogCancel = withId(android.R.id.button2)
    private val popupRoot = isPlatformPopup()
    private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun assertOpen() = Allure.step("Verify news form") {
        Ui.ready(R.id.news_save_button)
        onView(saveButton).check(matches(isDisplayed()))
    }
    fun fill(draft: NewsDraft) = Allure.step("Fill the unique news draft") {
        category(draft.category)
        title(draft.title)
        date(draft.date)
        time(draft.time)
        description(draft.description)
        active(draft.active)
    }
    fun active(value: Boolean) = Allure.step("Set news publication activity") {
        var current = false
        onView(activeSwitch).check { view, _ -> current = (view as android.widget.CompoundButton).isChecked }
        if (current != value) onView(activeSwitch).perform(scrollTo(), click())
        onView(activeSwitch).check(matches(if (value) isChecked() else not(isChecked())))
    }
    fun title(value: String) = Allure.step("Set news title") {
        onView(titleField).perform(replaceText(value))
        Ui.hideKeyboard()
    }
    fun description(value: String) = Allure.step("Set news description") {
        onView(descriptionField).perform(replaceText(value))
        Ui.hideKeyboard()
    }
    fun category(value: String) = Allure.step("Select news category") {
        Ui.await("news category options") {
            (Ui.find(R.id.news_category_auto_complete) as? android.widget.AutoCompleteTextView)?.adapter?.count?.let { it > 0 } == true
        }
        onView(categoryArrow).perform(click())
        onData(equalTo(value)).inRoot(popupRoot).perform(click())
    }
    fun date(value: LocalDate) = Allure.step("Select publication date") {
        onView(dateField).perform(click())
        onView(datePicker).perform(PickerActions.setDate(value.year, value.monthValue, value.dayOfMonth))
        onView(dialogOk).perform(click())
    }
    fun time(value: LocalTime) = Allure.step("Select publication time") {
        onView(timeField).perform(click())
        onView(timePicker).perform(PickerActions.setTime(value.hour, value.minute))
        onView(dialogOk).perform(click())
    }
    fun acceptDateWithoutChanges() = Allure.step("Open saved publication date and confirm without changing it") {
        onView(dateField).perform(click())
        onView(dialogOk).perform(click())
    }
    fun acceptTimeWithoutChanges() = Allure.step("Open saved publication time and confirm without changing it") {
        onView(timeField).perform(click())
        onView(dialogOk).perform(click())
    }
    fun assertDraft(draft: NewsDraft) = Allure.step("Verify every saved news field") {
        assertOpen()
        Ui.screenshot("News form field values")
        onView(categoryField).check(matches(withText(draft.category)))
        onView(titleField).check(matches(withText(draft.title)))
        onView(descriptionField).check(matches(withText(draft.description)))
        onView(dateField).check(matches(withText(draft.date.format(dateFormat))))
        onView(timeField).check(matches(withText(draft.time.format(timeFormat))))
        onView(activeSwitch).check(matches(if (draft.active) isChecked() else not(isChecked())))
    }
    fun save() = Allure.step("Save news and return to Control panel") {
        onView(saveButton).perform(scrollTo(), click())
        Ui.await("form closed after save") { !Ui.visible(R.id.news_save_button) }
        PanelPage.assertOpen()
    }
    fun cancelConfirm() = Allure.step("Discard form changes through the cancellation dialog") {
        onView(cancelButton).perform(scrollTo(), click())
        onView(dialogOk).perform(click())
        PanelPage.assertOpen()
    }
    fun cancelDismiss() = Allure.step("Dismiss cancellation and keep editing") {
        onView(cancelButton).perform(scrollTo(), click())
        onView(dialogCancel).perform(click())
        assertOpen()
    }
    fun leaveForCleanup() {
        if (Ui.inspect { Ui.visible(R.id.news_save_button) }) {
            Ui.hideKeyboard()
            Espresso.pressBack()
            PanelPage.assertOpen()
        }
    }
}
