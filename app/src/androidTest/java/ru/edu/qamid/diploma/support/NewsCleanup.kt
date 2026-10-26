package ru.edu.qamid.diploma.support

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.NavHostFragment
import androidx.test.platform.app.InstrumentationRegistry
import io.qameta.allure.kotlin.Allure
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.Assert.assertFalse
import ru.edu.qamid.R
import ru.edu.qamid.diploma.data.OwnedNews
import ru.edu.qamid.diploma.data.TestData
import ru.edu.qamid.diploma.pages.Ui
import ru.edu.qamid.ui.NewsControlPanelFragment
import ru.edu.qamid.viewmodel.NewsControlPanelViewModel
import java.util.concurrent.atomic.AtomicBoolean

/** Fixture cleanup only; user scenarios and their assertions use the UI. */
object NewsCleanup {
    fun removeRecorded(records: List<OwnedNews>) = Allure.step("Clean recorded own IDs unavailable in the loaded page") {
        for (record in records) {
            require(record.id > 0 && record.originalTitle.startsWith(TestData.NEWS_PREFIX))
            val failed = AtomicBoolean()
            lateinit var model: NewsControlPanelViewModel
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            try {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    val host = Ui.currentActivity()?.supportFragmentManager
                        ?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val page = host.childFragmentManager.primaryNavigationFragment as NewsControlPanelFragment
                    model = ViewModelProvider(page)[NewsControlPanelViewModel::class.java]
                    scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        model.removeNewsItemExceptionEvent.collect { failed.set(true) }
                    }
                    model.remove(record.id)
                }
                Ui.await("recorded own news removal completed") {
                    model.viewModelScope.coroutineContext[Job]?.children?.none { it.isActive } == true
                }
                assertFalse("Cleanup failed for recorded own news ID ${record.id}", failed.get())
                Allure.attachment("Removed own news ID", record.id.toString(), "text/plain")
            } finally {
                scope.cancel()
            }
        }
    }
}
