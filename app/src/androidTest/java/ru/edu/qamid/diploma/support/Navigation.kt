package ru.edu.qamid.diploma.support

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.qameta.allure.kotlin.Allure
import ru.edu.qamid.R
import ru.edu.qamid.diploma.data.TestData
import ru.edu.qamid.diploma.pages.*

object Navigation {
    fun backToMain() = Allure.step("System Back returns to Main") { pressBack(); MainPage.assertOpen() }
    fun backToNews() = Allure.step("System Back returns to News") { pressBack(); NewsPage.assertOpen() }
    fun recoverPanel() = Allure.step("Recover the UI to Control panel for cleanup") {
        Ui.dismissDialogs()
        NewsForm.leaveForCleanup()
        if (Ui.inspect { Ui.visible(R.id.filter_news_cancel_button) }) pressBack()
        if (Ui.inspect { Ui.visible(R.id.auth_app_bar) }) AuthPage.login(TestData.credentials())
        Ui.await("authenticated toolbar for cleanup") { Ui.visible(R.id.authorization_image_button) }
        if (!Ui.inspect { Ui.visible(R.id.news_control_panel_app_bar) }) {
            if (!Ui.inspect { Ui.visible(R.id.news_list_app_bar) }) AppMenu.news()
            NewsPage.panel()
        }
        PanelPage.assertOpen()
    }
}
