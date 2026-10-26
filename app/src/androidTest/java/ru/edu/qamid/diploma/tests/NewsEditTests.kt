package ru.edu.qamid.diploma.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.data.TestData
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.diploma.support.BaseUiTest

@RunWith(AndroidJUnit4::class)
@Feature("News") @Story("Editing and saved field values") @Severity(SeverityLevel.NORMAL)
class NewsEditTests : BaseUiTest() {
    @Test @Description("TC-086: activity can be disabled and enabled, with both changes preserved after reload")
    fun activityChangesPersistAfterReload() {
        openPanel()
        val original = createOwnNews()
        val inactive = TestData.inactive(original)
        PanelPage.edit(original.title)
        NewsForm.active(inactive.active)
        NewsForm.save()
        PanelPage.reload()
        PanelPage.assertStatus(original.title, inactive.active)
        PanelPage.edit(original.title)
        NewsForm.assertDraft(inactive)
        NewsForm.active(original.active)
        NewsForm.save()
        PanelPage.reload()
        PanelPage.assertStatus(original.title, original.active)
    }
    @Test @Description("TC-080: title edit persists after a new panel load")
    fun editedTitlePersistsAfterReload() {
        openPanel(); val original = createOwnNews()
        val edited = own(TestData.renamed(original))
        PanelPage.edit(original.title); NewsForm.title(edited.title); NewsForm.save()
        PanelPage.reload(); PanelPage.edit(edited.title); NewsForm.assertDraft(edited)
    }
    @Test @Description("TC-081: category, description, date and time edits all persist")
    fun editedFieldsPersistAfterReload() {
        openPanel(); val original = createOwnNews()
        val edited = TestData.editedFields(original)
        PanelPage.edit(original.title); NewsForm.fill(edited); NewsForm.save()
        PanelPage.reload(); PanelPage.edit(edited.title); NewsForm.assertDraft(edited)
    }
    @Test @Description("TC-082: confirmed cancellation discards edited fields")
    fun cancelledEditPreservesOriginalFields() {
        openPanel(); val original = createOwnNews()
        val edited = own(TestData.discardedEdits(original))
        PanelPage.edit(original.title); NewsForm.title(edited.title); NewsForm.description(edited.description)
        NewsForm.cancelConfirm(); PanelPage.reload(); PanelPage.edit(original.title); NewsForm.assertDraft(original)
    }
    @Test @Description("TC-083: dismissing cancellation retains the in-progress draft")
    fun dismissedCancellationKeepsDraft() {
        openPanel(); val original = createOwnNews()
        val edited = own(TestData.retainedEdits(original))
        PanelPage.edit(original.title); NewsForm.title(edited.title); NewsForm.cancelDismiss()
        NewsForm.assertDraft(edited); NewsForm.save(); PanelPage.reload(); PanelPage.edit(edited.title)
        NewsForm.assertDraft(edited)
    }
    @Test @Description("TC-084: confirming an unchanged saved date must preserve the future publication date")
    @Issue("BUG-003")
    fun unchangedDatePickerPreservesSavedDate() {
        openPanel(); val original = createOwnNews(TestData.futureNews())
        PanelPage.edit(original.title); NewsForm.acceptDateWithoutChanges(); NewsForm.assertDraft(original)
    }
    @Test @Description("TC-085: confirming an unchanged saved time must preserve 23:47")
    @Issue("BUG-003")
    fun unchangedTimePickerPreservesSavedTime() {
        openPanel(); val original = createOwnNews(TestData.futureNews())
        PanelPage.edit(original.title); NewsForm.acceptTimeWithoutChanges(); NewsForm.assertDraft(original)
    }
}
