package ru.edu.qamid.diploma.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import ru.edu.qamid.R
import ru.edu.qamid.diploma.data.DateRange
import ru.edu.qamid.ui.viewdata.NewsViewData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import android.widget.DatePicker

object NewsPage {
    private val marker = withId(R.id.news_list_app_bar)
    private val edit = withId(R.id.news_edit_button)
    private val filter = withId(R.id.news_filter_button)
    private val startDate = withId(R.id.filter_news_date_start_edit_text)
    private val endDate = withId(R.id.filter_news_date_end_edit_text)
    private val datePicker = isAssignableFrom(DatePicker::class.java)
    private val list = withId(R.id.news_list_recycler_view)
    private val cardBase = withId(R.id.news_item_material_card_view)
    private val emptyMessage = withId(R.id.empty_news_list_text_view)
    private fun card(title: String) = allOf(cardBase, hasDescendant(withText(title)))
    private fun items(): List<NewsViewData> =
        ((Ui.find(R.id.news_list_recycler_view) as RecyclerView).adapter as ListAdapter<*, *>)
            .currentList.map { it as NewsViewData }
    fun firstPublishedTitle(): String {
        Ui.await("a published news item for the filter scenario") { items().isNotEmpty() }
        var title = ""
        onView(list).check { _, _ -> title = items().first().title }
        return title
    }
    private val applyFilter = withId(R.id.filter_news_apply_button)
    private val cancelFilter = withId(R.id.filter_news_cancel_button)
    private val periodError = withText(R.string.wrong_news_date_period)
    private val dialogOk = withId(android.R.id.button1)
    fun assertOpen() = Allure.step("Verify News screen") {
        Ui.ready(R.id.news_list_app_bar)
        onView(marker).check(matches(isDisplayed()))
    }
    fun panel() = Allure.step("Open news Control panel") {
        onView(edit).perform(click())
        PanelPage.assertOpen()
    }
    fun assertTitle(title: String) = Allure.step("Verify own active news in the public list") {
        Ui.await("own published news in public list") { items().any { it.title == title } }
        onView(list).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(card(title)))
    }
    fun filterPeriod(period: DateRange) = Allure.step("Filter published news by a future date range") {
        onView(filter).perform(click())
        Ui.ready(R.id.filter_news_date_start_edit_text)
        onView(startDate).perform(click())
        onView(datePicker).perform(PickerActions.setDate(period.start.year, period.start.monthValue, period.start.dayOfMonth))
        onView(dialogOk).perform(click())
        onView(endDate).perform(click())
        onView(datePicker).perform(PickerActions.setDate(period.end.year, period.end.monthValue, period.end.dayOfMonth))
        onView(dialogOk).perform(click())
        onView(applyFilter).perform(click())
        assertOpen()
    }
    fun assertEmpty() = Allure.step("Verify the filtered published list is empty") {
        Ui.await("empty filtered list") { items().isEmpty() }
        onView(emptyMessage).check(matches(isDisplayed()))
    }
    fun clearFilter() = Allure.step("Apply the blank filter to restore all published news") {
        onView(filter).perform(click())
        Ui.ready(R.id.filter_news_date_start_edit_text)
        onView(startDate).check(matches(withText("")))
        onView(endDate).check(matches(withText("")))
        onView(applyFilter).perform(click())
        assertOpen()
    }
    fun assertEmptyMessageHidden() = Allure.step("Empty-list message must disappear when news are restored") {
        Ui.screenshot("Published list after clearing the empty filter")
        onView(emptyMessage).check(matches(not(isDisplayed())))
    }
    fun invalidIncompletePeriod() = Allure.step("Reject a filter with only the start date") {
        onView(filter).perform(click())
        Ui.ready(R.id.filter_news_date_start_edit_text)
        onView(startDate).perform(click())
        onView(dialogOk).perform(click())
        onView(applyFilter).perform(click())
        onView(periodError).check(matches(isDisplayed()))
        onView(dialogOk).perform(click())
        onView(cancelFilter).perform(click())
        assertOpen()
    }
}
