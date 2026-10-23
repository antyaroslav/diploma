package ru.edu.qamid.diploma

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*

@RunWith(AndroidJUnit4::class)
@Feature("News")
class NewsTests : BaseUiTest() {
    @Test @Description("TC-041: create a unique news item and verify it in the panel; teardown deletes it")
    fun newNewsItemAppearsInControlPanel() {
        login(); MainPage.news(); NewsPage.panel()
        PanelPage.assertTitle(createOwnNews())
    }

    @Test @Description("TC-027: incomplete date range is rejected without leaving the filter")
    fun incompleteDateRangeIsRejected() { login(); MainPage.news(); NewsPage.invalidIncompletePeriod() }
}
