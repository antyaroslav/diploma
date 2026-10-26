package ru.edu.qamid.diploma.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.diploma.support.BaseUiTest

@RunWith(AndroidJUnit4::class)
@Feature("Our Mission") @Story("Mission quotes") @Severity(SeverityLevel.NORMAL)
class MissionTests : BaseUiTest() {
    @Test @Description("TC-057: mission heading and eight quotes")
    fun missionContainsEightQuotes() { login(); AppMenu.mission(); MissionPage.assertQuotes() }

    @Test @Description("TC-058: first quote expands and collapses")
    fun firstQuoteCanBeExpandedAndCollapsed() { login(); AppMenu.mission(); MissionPage.expandFirstQuote() }
}
