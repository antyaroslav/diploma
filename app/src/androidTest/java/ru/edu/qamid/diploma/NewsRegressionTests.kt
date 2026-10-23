package ru.edu.qamid.diploma

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@Feature("News regression")
class NewsRegressionTests : BaseUiTest() {
    @Test @Description("TC-036 / BUG-001: expected real creation date; known defect must fail this assertion")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-001")
    fun creationDateEqualsToday() {
        login(); MainPage.news(); NewsPage.panel()
        val day = LocalDate.now()
        PanelPage.assertCreationDate(createOwnNews(), day)
    }
}
