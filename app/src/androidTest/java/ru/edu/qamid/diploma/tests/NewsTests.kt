package ru.edu.qamid.diploma.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.data.TestData
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.diploma.support.BaseUiTest

@RunWith(AndroidJUnit4::class)
@Feature("News") @Story("Creation and filtering") @Severity(SeverityLevel.CRITICAL)
class NewsTests : BaseUiTest() {
    @Test @Description("TC-087: clearing an empty date filter restores news without the empty-list message")
    fun clearingEmptyFilterHidesEmptyMessage() {
        login()
        AppMenu.news()
        val title = NewsPage.firstPublishedTitle()
        NewsPage.filterPeriod(TestData.futurePeriod())
        NewsPage.assertEmpty()
        NewsPage.clearFilter()
        NewsPage.assertTitle(title)
        NewsPage.assertEmptyMessageHidden()
    }
    @Test @Description("TC-041: a unique created news item appears in Control panel")
    fun newNewsItemAppearsInControlPanel() { openPanel(); PanelPage.assertTitle(createOwnNews().title) }

    @Test @Description("TC-027: incomplete date range is rejected")
    fun incompleteDateRangeIsRejected() { login(); AppMenu.news(); NewsPage.invalidIncompletePeriod() }

    @Test @Description("TC-077: cancelling a completed creation form creates no news")
    fun cancelledCreationDoesNotCreateNews() {
        openPanel()
        val draft = own(TestData.news())
        PanelPage.create(); NewsForm.fill(draft); NewsForm.cancelConfirm()
        PanelPage.assertAbsent(draft.title)
    }
}
