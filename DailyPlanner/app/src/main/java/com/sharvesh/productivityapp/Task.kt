package com.sharvesh.productivityapp

import java.util.Calendar
import java.util.Date
import java.util.UUID

class Task {
    // Properties (variables)
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var dueDate: Date = Date()
    var completed: Boolean = false
    var category: String = "" // "overdue", "today", "upcoming", "done"
    var startHour: Int = -1
    var endHour: Int = -1
    var scheduled: Boolean = false
    // Constructors
    constructor() {
        this.id = UUID.randomUUID().toString()
        this.completed = false
    }

    constructor(title: String, dueDate: Date) {
        this.id = UUID.randomUUID().toString()
        this.title = title
        this.dueDate = dueDate
        this.completed = false
        updateCategory()
    }

    // Method to update category based on due date and completion status
    fun updateCategory() {
        val now = Date()
        when {
            completed -> this.category = "done"
            dueDate.before(now) -> this.category = "overdue"
            isSameDay(dueDate, now) -> this.category = "today"
            else -> this.category = "upcoming"
        }
    }

    // Helper method to check if two dates are on the same day
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
