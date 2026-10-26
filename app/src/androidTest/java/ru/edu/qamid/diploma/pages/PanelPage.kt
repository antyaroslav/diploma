package ru.edu.qamid.diploma.pages

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import io.qameta.allure.kotlin.Allure
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import ru.edu.qamid.R
import ru.edu.qamid.dto.NewsWithCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import ru.edu.qamid.ui.NewsControlPanelFragment
import ru.edu.qamid.viewmodel.NewsControlPanelViewModel

object PanelPage {
    private val marker = withId(R.id.news_control_panel_app_bar)
    private val list = withId(R.id.news_list_recycler_view)
    private val addButton = withId(R.id.add_news_image_view)
    private val rowBase = withId(R.id.news_item_material_card_view)
    private val titleBase = withId(R.id.news_item_title_text_view)
    private val editBase = withId(R.id.news_item_edit_image_view)
    private val deleteBase = withId(R.id.news_item_delete_image_view)
    private val creationBase = withId(R.id.news_item_create_date_text_view)
    private val statusBase = withId(R.id.news_item_published_text_view)
    private val dialogOk = withId(android.R.id.button1)
    private val dialogCancel = withId(android.R.id.button2)
    private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val errorMessage = withId(android.R.id.message)
    private fun row(title: String): Matcher<View> = allOf(rowBase, hasDescendant(allOf(titleBase, withText(title))))
    private fun rowChild(title: String, child: Matcher<View>) = allOf(child, isDescendantOfA(row(title)))
    private fun items(): List<NewsWithCategory> {
        val adapter = (Ui.find(R.id.news_list_recycler_view) as? RecyclerView)?.adapter as? ListAdapter<*, *>
            ?: throw AssertionError("Control panel has no loaded list adapter")
        return adapter.currentList.map {
            it as? NewsWithCategory ?: throw AssertionError("Unexpected Control panel item type")
        }
    }
    fun assertOpen() = Allure.step("Verify Control panel screen") {
        Ui.ready(R.id.news_control_panel_app_bar)
        onView(marker).check(matches(isDisplayed()))
    }
    fun create() = Allure.step("Open new news form") {
        onView(addButton).perform(click())
        NewsForm.assertOpen()
    }
    fun show(title: String) {
        Ui.await("exact news title in Control panel") { items().any { it.newsItem.title == title } }
        onView(list).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(row(title)))
    }
    fun assertTitle(title: String) = Allure.step("Verify the exact news title") {
        try {
            show(title)
        } catch (failure: AssertionError) {
            Ui.screenshot("Expected own news missing from Control panel")
            throw failure
        }
        onView(rowChild(title, titleBase)).check(matches(withText(title)))
    }
    fun ownedId(title: String): Int {
        var id: Int? = null
        onView(list).check { _, _ -> id = items().single { it.newsItem.title == title }.newsItem.id }
        return requireNotNull(id) { "Created own news has no server ID" }
    }
    fun assertStatus(title: String, active: Boolean) = Allure.step("Verify news publication activity in Control panel") {
        show(title)
        val text = if (active) R.string.news_control_panel_active else R.string.news_control_panel_not_active
        onView(rowChild(title, statusBase)).check(matches(withText(text)))
    }
    fun edit(title: String) = Allure.step("Edit this test's news item") {
        show(title)
        onView(rowChild(title, editBase)).perform(click())
        NewsForm.assertOpen()
    }
    fun reload() = Allure.step("Reload Control panel through News") {
        AppMenu.news()
        NewsPage.panel()
        lateinit var model: NewsControlPanelViewModel
        lateinit var adapter: ListAdapter<NewsWithCategory, *>
        onView(marker).check { _, _ ->
            val host = Ui.currentActivity()?.supportFragmentManager
                ?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val page = host.childFragmentManager.primaryNavigationFragment as NewsControlPanelFragment
            model = ViewModelProvider(page)[NewsControlPanelViewModel::class.java]
            @Suppress("UNCHECKED_CAST")
            val loadedAdapter = (Ui.find(R.id.news_list_recycler_view) as RecyclerView).adapter
                as ListAdapter<NewsWithCategory, *>
            adapter = loadedAdapter
        }
        fun refreshComplete() = model.viewModelScope.coroutineContext[Job]
            ?.children?.none { it.isActive } == true
        Ui.await("initial panel refresh completed") { refreshComplete() }
        onView(errorMessage).check(doesNotExist())
        // Observe a new emission after the server refresh has finished. This is
        // synchronization only: scenario assertions still inspect page elements.
        // Waiting for both model and adapter avoids accepting the pre-refresh cache.
        val refreshed = runBlocking {
            withTimeout(20_000) { model.data.first() }
        }
        Ui.await("refreshed model applied to panel adapter") { adapter.currentList == refreshed }
        onView(errorMessage).check(doesNotExist())
    }
    fun assertAbsent(title: String) = Allure.step("Verify news absent after a fresh panel load") {
        reload()
        assertFalse("News still exists: $title", Ui.inspect { items().any { it.newsItem.title == title } })
    }
    fun delete(title: String): Int = Allure.step("Confirm deletion of this test's exact news item") {
        show(title)
        val id = ownedId(title)
        onView(rowChild(title, deleteBase)).perform(click())
        onView(dialogOk).perform(click())
        Ui.await("exact news removed") { items().none { it.newsItem.title == title } }
        id
    }
    fun cancelDelete(title: String) = Allure.step("Cancel news deletion") {
        show(title)
        onView(rowChild(title, deleteBase)).perform(click())
        onView(dialogCancel).perform(click())
        assertTitle(title)
    }
    fun assertCreationDate(title: String, day: LocalDate) = Allure.step("Creation date must equal the actual creation day") {
        show(title)
        Ui.screenshot("Creation date evidence")
        onView(rowChild(title, creationBase)).check(matches(withText(day.format(dateFormat))))
    }
    fun cleanOwned(aliases: Set<String>): Set<Int> = Allure.step("Clean visible registered unique news titles") {
        assertOpen()
        val deleted = mutableSetOf<Int>()
        for (title in aliases) {
            require(title.startsWith(ru.edu.qamid.diploma.data.TestData.NEWS_PREFIX))
            if (Ui.inspect { items().any { it.newsItem.title == title } }) deleted += delete(title)
        }
        deleted
    }
}
