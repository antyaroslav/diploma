package ru.edu.qamid.diploma.pages

import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.Espresso.pressBack
import org.hamcrest.Matcher
import org.hamcrest.Matchers.anything
import java.util.concurrent.TimeoutException
import androidx.test.platform.app.InstrumentationRegistry
import io.qameta.allure.kotlin.Allure
import java.io.ByteArrayOutputStream

/** Waits for an observable UI condition while allowing the main looper to process work. */
object Ui {
    private val root = isRoot()

    fun hideKeyboard() {
        onView(root).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = root
            override fun getDescription() = "Hide the software keyboard and observe its insets"
            override fun perform(controller: UiController, view: View) {
                val manager = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
                manager.hideSoftInputFromWindow(view.windowToken, 0)
                androidx.core.view.ViewCompat.getWindowInsetsController(view)
                    ?.hide(androidx.core.view.WindowInsetsCompat.Type.ime())
                controller.loopMainThreadUntilIdle()
            }
        })
        await("software keyboard hidden") {
            val decor = currentActivity()?.window?.decorView
            decor != null && androidx.core.view.ViewCompat.getRootWindowInsets(decor)
                ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == false
        }
    }
    fun dismissDialogs() {
        repeat(3) {
            var transientWindow = false
            onView(root).check { view, error ->
                if (error != null) throw error
                val activity = checkNotNull(currentActivity()) { "No activity available for dialog cleanup" }
                transientWindow = view.rootView !== activity.window.decorView
            }
            if (!transientWindow) return
            pressBack()
        }
    }

    fun currentActivity(): androidx.fragment.app.FragmentActivity? {
        val monitor = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
        return monitor.getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)
            .firstOrNull() as? androidx.fragment.app.FragmentActivity
    }
    fun screenshot(name: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val bytes = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bytes)
        Allure.attachment(name, bytes.toByteArray().inputStream(), "image/png", ".png")
        bitmap.recycle()
    }
    fun inspect(condition: () -> Boolean): Boolean {
        var result = false
        onView(root).check { _, _ -> result = condition() }
        return result
    }
    fun secretText(value: String) = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(android.widget.EditText::class.java)
        override fun getDescription() = "Set confidential input"
        override fun perform(controller: UiController, view: View) {
            (view as android.widget.EditText).setText(value)
            controller.loopMainThreadUntilIdle()
        }
    }
    fun await(description: String, timeoutMs: Long = 20_000, condition: () -> Boolean) {
        var satisfied = false
        onView(root).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = root
            override fun getDescription() = "Wait for $description"
            override fun perform(controller: UiController, view: View) {
                val deadline = SystemClock.uptimeMillis() + timeoutMs
                do {
                    if (condition()) {
                        satisfied = true
                        return
                    }
                    controller.loopMainThreadForAtLeast(50)
                } while (SystemClock.uptimeMillis() < deadline)
            }
        })
        org.junit.Assert.assertTrue("Timed out waiting for $description", satisfied)
    }

    fun visible(id: Int): Boolean = find(id)?.isShown == true

    fun find(id: Int): View? {
        return currentActivity()?.findViewById(id)
    }

    fun ready(id: Int) = await("view $id") { visible(id) }
}
