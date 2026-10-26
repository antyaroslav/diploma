package ru.edu.qamid.diploma.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.PanelPage
import ru.edu.qamid.diploma.support.BaseUiTest
import ru.edu.qamid.diploma.data.TestData
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Feature("News regression") @Story("Creation date") @Severity(SeverityLevel.NORMAL)
class NewsRegressionTests : BaseUiTest() {
    @Test @Description("TC-088 / BUG-004: a created published news item remains accessible after section navigation")
    @Issue("BUG-004")
    @Severity(SeverityLevel.CRITICAL)
    fun publishedNewsRemainsAfterSectionNavigation() {
        openPanel()
        val draft = createOwnNews(TestData.publishedNews())
        PanelPage.reload()
        PanelPage.assertTitle(draft.title)
    }
    @Test @Description("TC-036 / BUG-001: creation date equals the real creation day") @Issue("BUG-001")
    fun creationDateEqualsToday() {
        openPanel(); val day = LocalDate.now(); val draft = createOwnNews()
        PanelPage.assertCreationDate(draft.title, day)
    }
}
