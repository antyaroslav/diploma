package ru.edu.qamid.diploma

import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import io.qameta.allure.kotlin.Allure
import org.junit.After
import org.junit.Before
import ru.edu.qamid.ui.AppActivity
import ru.edu.qamid.diploma.pages.*
import java.util.UUID

abstract class BaseUiTest {
    private lateinit var scenario: ActivityScenario<AppActivity>
    protected val ownNews = mutableListOf<String>()

    @Before fun launchCleanSession() {
        scenario = ActivityScenario.launch(AppActivity::class.java)
        AuthPage.normalizeSession()
    }

    protected fun login() {
        val args = InstrumentationRegistry.getArguments()
        val user = requireNotNull(args.getString("qaLogin")) { "Provide qaLogin instrumentation argument" }
        val password = requireNotNull(args.getString("qaPassword")) { "Provide qaPassword instrumentation argument" }
        AuthPage.login(user, password)
    }

    protected fun createOwnNews(): String {
        val title = "QA_DIPLOMA_${UUID.randomUUID()}"
        ownNews += title
        try { NewsForm.create(title) } catch (error: Throwable) {
            Allure.attachment("Creation failure", error.stackTraceToString(), "text/plain")
            throw error
        }
        return title
    }

    @After fun closeAndClean() {
        try {
            if (ownNews.isNotEmpty()) {
                NewsForm.cancelIfOpen()
                ownNews.forEach { PanelPage.deleteIfPresent(it) }
            }
        } finally { scenario.close() }
    }
}
