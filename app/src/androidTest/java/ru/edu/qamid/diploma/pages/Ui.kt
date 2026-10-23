package ru.edu.qamid.diploma.pages

import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matcher
import org.hamcrest.Matchers.anything
import java.util.concurrent.TimeoutException

/** Waits for an observable UI condition while allowing the main looper to process work. */
object Ui {
    fun secretText(value: String) = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(android.widget.EditText::class.java)
        override fun getDescription() = "Set confidential input"
        override fun perform(controller: UiController, view: View) {
            (view as android.widget.EditText).setText(value)
            controller.loopMainThreadUntilIdle()
        }
    }
    fun await(description: String, timeoutMs: Long = 20_000, condition: () -> Boolean) {
        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "Wait for $description"
            override fun perform(controller: UiController, view: View) {
                val deadline = SystemClock.uptimeMillis() + timeoutMs
                do {
                    if (condition()) return
                    controller.loopMainThreadForAtLeast(50)
                } while (SystemClock.uptimeMillis() < deadline)
                throw TimeoutException("Timed out waiting for $description")
            }
        })
    }

    fun visible(id: Int): Boolean = find(id)?.isShown == true

    fun find(id: Int): View? {
        val lifecycle = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
        val activity = lifecycle.getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).firstOrNull()
        return activity?.findViewById(id)
    }

    fun ready(id: Int) = await("view $id") { visible(id) }
}
