package com.denizcan.astrosea.notifications

object DailyReminderPolicy {
    val hours = listOf(10, 13, 16, 19, 22)

    // A delayed alarm must not deliver yesterday's reminder or a burst of old slots.
    fun isCurrentSlot(index: Int, scheduledDate: String, today: String, hour: Int): Boolean =
        index in hours.indices && scheduledDate == today && hours.indexOfLast { it <= hour } == index

    fun completed(lastDrawDate: String?, today: String, revealed: List<Boolean>): Boolean =
        lastDrawDate == today && revealed.size == 3 && revealed.all { it }
}
