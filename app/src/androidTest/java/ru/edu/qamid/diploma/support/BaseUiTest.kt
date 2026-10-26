package ru.edu.qamid.diploma.support

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import io.qameta.allure.kotlin.Allure
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import ru.edu.qamid.diploma.data.*
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.ui.AppActivity

abstract class BaseUiTest {
    protected open val nightMode = AppCompatDelegate.MODE_NIGHT_NO
    private val ownTitles = linkedSetOf<String>()
    private val created = linkedMapOf<Int, OwnedNews>()
    private val deleted = mutableSetOf<Int>()
    private val pendingSaves = mutableSetOf<String>()
    @get:Rule val session = TestRule { base, _ -> object : Statement() {
        override fun evaluate() {
            var scenario: ActivityScenario<AppActivity>? = null
            val previousMode = AppCompatDelegate.getDefaultNightMode()
            var primaryFailure: Throwable? = null
            try {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                }
                scenario = ActivityScenario.launch(AppActivity::class.java)
                AuthPage.normalizeSession()
                base.evaluate()
            } catch (failure: Throwable) {
                primaryFailure = failure
            } finally {
                try {
                    if (ownTitles.isNotEmpty()) {
                        Navigation.recoverPanel()
                        deleted += PanelPage.cleanOwned(ownTitles)
                        NewsCleanup.removeRecorded(created.values.filter { it.id !in deleted })
                        check(pendingSaves.isEmpty()) { "Save outcome and cleanup are unconfirmed for $pendingSaves" }
                    }
                } catch (cleanup: Throwable) {
                    Allure.attachment("Unresolved cleanup", cleanup.stackTraceToString(), "text/plain")
                    if (primaryFailure == null) primaryFailure = cleanup else primaryFailure!!.addSuppressed(cleanup)
                } finally {
                    try { scenario?.close() } catch (close: Throwable) {
                        if (primaryFailure == null) primaryFailure = close else primaryFailure!!.addSuppressed(close)
                    }
                    try {
                        InstrumentationRegistry.getInstrumentation().runOnMainSync {
                            AppCompatDelegate.setDefaultNightMode(previousMode)
                        }
                    } catch (restore: Throwable) {
                        if (primaryFailure == null) primaryFailure = restore else primaryFailure!!.addSuppressed(restore)
                    }
                }
            }
            primaryFailure?.let { throw it }
        }
    } }
    protected fun login() = AuthPage.login(TestData.credentials())
    protected fun openPanel() { login(); AppMenu.news(); NewsPage.panel() }
    protected fun own(draft: NewsDraft): NewsDraft {
        require(draft.title.startsWith(TestData.NEWS_PREFIX))
        ownTitles += draft.title
        Allure.attachment("Registered own news title", draft.title, "text/plain")
        return draft
    }
    protected fun createOwnNews(draft: NewsDraft = TestData.news()): NewsDraft {
        own(draft) // Register before save, including partial-failure paths.
        PanelPage.create()
        NewsForm.fill(draft)
        pendingSaves += draft.title
        NewsForm.save()
        PanelPage.assertTitle(draft.title)
        val record = OwnedNews(PanelPage.ownedId(draft.title), draft.title)
        created[record.id] = record
        pendingSaves -= draft.title
        Allure.attachment("Registered own news ID", record.id.toString(), "text/plain")
        return draft
    }
    protected fun deleteOwnNews(draft: NewsDraft) {
        val id = PanelPage.ownedId(draft.title)
        require(created.containsKey(id)) { "Only a recorded own news item can be removed" }
        PanelPage.delete(draft.title)
        deleted += id
    }
}
