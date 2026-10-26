package ru.edu.qamid.diploma.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.edu.qamid.diploma.pages.*
import ru.edu.qamid.diploma.support.*

@RunWith(AndroidJUnit4::class)
@Feature("Navigation") @Story("Section identity and back stack") @Severity(SeverityLevel.NORMAL)
class NavigationTests : BaseUiTest() {
    @Test @Description("TC-067: News menu opens the real Main screen")
    fun newsMenuReturnsToMain() { login(); AppMenu.news(); AppMenu.main(); MainPage.assertOpen() }

    @Test @Description("TC-068: Control panel menu opens Main")
    fun panelMenuOpensMain() { openPanel(); AppMenu.main(); MainPage.assertOpen() }

    @Test @Description("TC-069: Control panel menu opens News")
    fun panelMenuOpensNews() { openPanel(); AppMenu.news(); NewsPage.assertOpen() }

    @Test @Description("TC-070: Our Mission menu opens News")
    fun missionMenuOpensNews() { login(); AppMenu.mission(); AppMenu.news(); NewsPage.assertOpen() }

    @Test @Description("TC-071: Back from Our Mission returns to Main")
    fun missionBackReturnsToMain() { login(); AppMenu.mission(); Navigation.backToMain() }

    @Test @Description("TC-072: Back from Control panel returns to News")
    fun panelBackReturnsToNews() { openPanel(); Navigation.backToNews() }

    @Test @Description("TC-073: ALL NEWS shortcut opens News")
    fun allNewsShortcutOpensNews() { login(); MainPage.allNews(); NewsPage.assertOpen() }

    @Test @Description("TC-074: logout from News returns to Authorization")
    fun newsLogoutReturnsToAuthorization() { login(); AppMenu.news(); AppMenu.logout(); AuthPage.assertOpen() }

    @Test @Description("TC-075: logout from Control panel returns to Authorization")
    fun panelLogoutReturnsToAuthorization() { openPanel(); AppMenu.logout(); AuthPage.assertOpen() }

    @Test @Description("TC-076: logout from Our Mission returns to Authorization")
    fun missionLogoutReturnsToAuthorization() { login(); AppMenu.mission(); AppMenu.logout(); AuthPage.assertOpen() }
}
