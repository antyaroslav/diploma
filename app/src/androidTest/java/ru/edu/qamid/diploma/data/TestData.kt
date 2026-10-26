package ru.edu.qamid.diploma.data

import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Credentials(val login: String, val password: String) {
    override fun toString() = "Credentials([REDACTED])"
}

data class NewsDraft(
    val title: String,
    val description: String = "Diploma UI test",
    val category: String = "Объявление",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.of(0, 1),
    val active: Boolean = true
)

data class DateRange(val start: LocalDate, val end: LocalDate)
data class OwnedNews(val id: Int, val originalTitle: String)

object TestData {
    const val NEWS_PREFIX = "QA_DIPLOMA_"
    val contrastCredentials = Credentials("ContrastSample", "Synthetic123")
    fun news() = NewsDraft(NEWS_PREFIX + UUID.randomUUID(), date = LocalDate.now().plusYears(10))
    fun publishedNews() = NewsDraft(NEWS_PREFIX + UUID.randomUUID())
    fun renamed(original: NewsDraft) = original.copy(title = original.title + "_edited")
    fun editedFields(original: NewsDraft) = original.copy(
        category = "День рождения",
        description = "Edited diploma description",
        date = LocalDate.now().plusYears(11),
        time = LocalTime.of(23, 47)
    )
    fun discardedEdits(original: NewsDraft) = original.copy(
        title = original.title + "_discarded", description = "Unsaved description"
    )
    fun retainedEdits(original: NewsDraft) = original.copy(title = original.title + "_retained")
    fun futureNews() = news().copy(date = LocalDate.now().plusDays(3), time = LocalTime.of(23, 47))
    fun inactive(original: NewsDraft) = original.copy(active = false)
    fun futurePeriod() = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(1))
    fun credentials(): Credentials {
        val args = InstrumentationRegistry.getArguments()
        return Credentials(
            requireNotNull(args.getString("qaLogin")) { "Configure qaLogin" },
            requireNotNull(args.getString("qaPassword")) { "Configure qaPassword" }
        )
    }
}
