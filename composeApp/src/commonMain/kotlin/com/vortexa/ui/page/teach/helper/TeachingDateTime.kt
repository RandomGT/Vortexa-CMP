package com.vortexa.ui.page.teach.helper

import kotlin.math.min
import kotlin.time.Clock

data class TeachingDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<TeachingDate> {
    override fun compareTo(other: TeachingDate): Int =
        compareValuesBy(this, other, TeachingDate::year, TeachingDate::month, TeachingDate::day)

    fun plusMonths(delta: Int): TeachingDate {
        val totalMonths = year * 12 + (month - 1) + delta
        val nextYear = floorDiv(totalMonths, 12)
        val nextMonth = floorMod(totalMonths, 12) + 1
        return copy(
            year = nextYear,
            month = nextMonth,
            day = min(day, daysInMonth(nextYear, nextMonth))
        )
    }

    fun formatSlash(): String = "${year.toString().padStart(4, '0')}/${month.twoDigits()}/${day.twoDigits()}"

    fun monthYearLabel(): String = "${monthName()} $year"

    fun monthName(): String = monthNames.getOrElse(month - 1) { "" }

    fun epochDays(): Long = civilToEpochDays(year, month, day)

    fun dayOfWeekIndexSundayFirst(): Int = floorMod((epochDays() + 4).toInt(), 7)

    companion object {
        fun today(): TeachingDate {
            val epochDay = floorDiv(currentTeachingEpochMillis(), MILLIS_PER_DAY)
            return epochDaysToCivil(epochDay)
        }
    }
}

data class TeachingCalendarDay(
    val date: TeachingDate,
    val isCurrentMonth: Boolean
)

fun teachingMonthGridDays(monthDate: TeachingDate): List<TeachingCalendarDay> {
    val first = TeachingDate(monthDate.year, monthDate.month, 1)
    val previousMonth = first.plusMonths(-1)
    val nextMonth = first.plusMonths(1)
    val leading = first.dayOfWeekIndexSundayFirst()
    val currentDays = daysInMonth(first.year, first.month)
    val previousDays = daysInMonth(previousMonth.year, previousMonth.month)
    val cells = mutableListOf<TeachingCalendarDay>()
    for (i in leading downTo 1) {
        cells += TeachingCalendarDay(
            date = TeachingDate(previousMonth.year, previousMonth.month, previousDays - i + 1),
            isCurrentMonth = false
        )
    }
    for (day in 1..currentDays) {
        cells += TeachingCalendarDay(TeachingDate(first.year, first.month, day), true)
    }
    var trailing = 1
    while (cells.size < 42) {
        cells += TeachingCalendarDay(TeachingDate(nextMonth.year, nextMonth.month, trailing++), false)
    }
    return cells
}

internal fun currentTeachingEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

internal fun parseTeachingEpochMilli(raw: String): Long? {
    val text = raw.trim()
    if (text.isEmpty()) return null
    val parts = text.replace('T', ' ').split(' ', limit = 2)
    val dateParts = parts.getOrNull(0)?.replace('-', '/')?.split('/') ?: return null
    if (dateParts.size != 3) return null
    val timeParts = parts.getOrNull(1)?.split(':').orEmpty()
    val year = dateParts[0].toIntOrNull() ?: return null
    val month = dateParts[1].toIntOrNull() ?: return null
    val day = dateParts[2].toIntOrNull() ?: return null
    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val second = timeParts.getOrNull(2)?.toIntOrNull() ?: 0
    if (month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
    if (hour !in 0..23 || minute !in 0..59 || second !in 0..59) return null
    return civilToEpochDays(year, month, day) * MILLIS_PER_DAY +
        hour * 3_600_000L + minute * 60_000L + second * 1_000L
}

internal fun formatTeachingCountdownHms(remainingMs: Long): String {
    val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return "$h:${m.toInt().twoDigits()}:${s.toInt().twoDigits()}"
}

internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun civilToEpochDays(year: Int, month: Int, day: Int): Long {
    var y = year
    y -= if (month <= 2) 1 else 0
    val era = floorDiv(y, 400)
    val yoe = y - era * 400
    val mp = month + if (month > 2) -3 else 9
    val doy = (153 * mp + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097L + doe - 719468L
}

private fun epochDaysToCivil(epochDays: Long): TeachingDate {
    var z = epochDays + 719468L
    val era = floorDiv(z, 146097L)
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    var y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val d = doy - (153L * mp + 2L) / 5L + 1L
    val m = mp + if (mp < 10L) 3L else -9L
    y += if (m <= 2L) 1L else 0L
    return TeachingDate(y.toInt(), m.toInt(), d.toInt())
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun floorDiv(value: Int, divisor: Int): Int {
    var result = value / divisor
    if ((value xor divisor) < 0 && result * divisor != value) result--
    return result
}

private fun floorDiv(value: Long, divisor: Long): Long {
    var result = value / divisor
    if ((value xor divisor) < 0 && result * divisor != value) result--
    return result
}

private fun floorMod(value: Int, divisor: Int): Int {
    val mod = value % divisor
    return if (mod < 0) mod + divisor else mod
}

private const val MILLIS_PER_DAY = 86_400_000L

private val monthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
)
