package ru.edu.qamid.diploma

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*

@RunWith(AndroidJUnit4::class)
@Feature("Our Mission")
class MissionTests : BaseUiTest() {
    @Test @Description("TC-057: mission heading and eight quotes")
    fun missionContainsEightQuotes() { login(); MainPage.mission(); MissionPage.assertQuotes() }

    @Test @Description("TC-058 (first quote): expand and collapse its description")
    fun firstQuoteCanBeExpandedAndCollapsed() { login(); MainPage.mission(); MissionPage.expandFirstQuote() }
}
