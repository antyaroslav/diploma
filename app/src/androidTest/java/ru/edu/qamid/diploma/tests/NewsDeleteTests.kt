package ru.edu.qamid.diploma.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.diploma.support.BaseUiTest

@RunWith(AndroidJUnit4::class)
@Feature("News") @Story("Deletion confirmation") @Severity(SeverityLevel.CRITICAL)
class NewsDeleteTests : BaseUiTest() {
    @Test @Description("TC-078: cancelling deletion preserves the exact news after panel reload")
    fun cancelledDeletionPreservesNews() {
        openPanel(); val draft = createOwnNews()
        PanelPage.cancelDelete(draft.title); PanelPage.reload(); PanelPage.assertTitle(draft.title)
    }
    @Test @Description("TC-079: confirmed deletion persists after a fresh Control panel load")
    fun confirmedDeletionRemovesNewsAfterReload() {
        openPanel(); val draft = createOwnNews()
        deleteOwnNews(draft); PanelPage.assertAbsent(draft.title)
    }
}
