package com.sharvesh.productivityapp

import android.graphics.Color
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.util.Log
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvMonth: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvQuarter: TextView
    private lateinit var timeSlotRecyclerView: RecyclerView
    private var currentCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize views
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        tvMonth = findViewById(R.id.tvMonth)
        tvYear = findViewById(R.id.tvYear)
        tvQuarter = findViewById(R.id.tvQuarter)
        timeSlotRecyclerView = findViewById(R.id.timeSlotRecyclerView)

        // Initialize calendar
        updateCalendarDisplay()
        generateCalendarGrid()

        // Set up time slots
        setupTimeSlots()

        // Set up navigation buttons
        val btnPrevDate = findViewById<ImageButton>(R.id.btnPrevDate)
        val btnNextDate = findViewById<ImageButton>(R.id.btnNextDate)

        btnPrevDate.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1)
            updateCalendarDisplay()
            refreshTimeSlots()
        }

        btnNextDate.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
            updateCalendarDisplay()
            refreshTimeSlots()
        }

        // Today button
        findViewById<View>(R.id.btnToday).setOnClickListener {
            currentCalendar = Calendar.getInstance()
            updateCalendarDisplay()
            refreshTimeSlots()
        }

        // Month navigation
        findViewById<View>(R.id.btnPrevMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendarDisplay()
            generateCalendarGrid()
            refreshTimeSlots()
        }

        findViewById<View>(R.id.btnNextMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendarDisplay()
            generateCalendarGrid()
            refreshTimeSlots()
        }

        // Switch to Tasks view
        findViewById<View>(R.id.btnTimeView).setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnFirstMonth).setOnClickListener {
            currentCalendar.add(Calendar.YEAR, -1)
            updateCalendarDisplay()
            generateCalendarGrid()
            refreshTimeSlots()
        }

        findViewById<View>(R.id.btnLastMonth).setOnClickListener {
            currentCalendar.add(Calendar.YEAR, 1)
            updateCalendarDisplay()
            generateCalendarGrid()
            refreshTimeSlots()
        }
    }

    private fun updateCalendarDisplay() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        tvCurrentDate.text = dateFormat.format(currentCalendar.time)
        tvMonth.text = monthFormat.format(currentCalendar.time)
        tvYear.text = yearFormat.format(currentCalendar.time)

        // Calculate quarter
        val month = currentCalendar.get(Calendar.MONTH)
        val quarter = (month / 3) + 1
        tvQuarter.text = "Q$quarter"
    }

    private fun setupTimeSlots() {
        // Generate time slots from 6 to 23 (6 AM to 11 PM)
        val timeSlots = (6..23).toList()

        // Create adapter
        val adapter = TimeSlotAdapter(this, timeSlots) { hour ->
            showTaskSelectionDialog(hour)
        }

        // Set up RecyclerView
        timeSlotRecyclerView.layoutManager = LinearLayoutManager(this)
        timeSlotRecyclerView.adapter = adapter

        // Load initial tasks
        refreshTimeSlots()
    }

    private fun refreshTimeSlots() {
        // Get all tasks
        val taskRepository = TaskRepository(this)
        val allTasks = taskRepository.getAllTasks()

        // Filter tasks for the current date
        val tasksForCurrentDate = allTasks.filter { task ->
            val taskCal = Calendar.getInstance().apply { time = task.dueDate }
            val currentCal = currentCalendar.clone() as Calendar

            taskCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                    taskCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)
        }

        // Update adapter with filtered tasks
        val adapter = timeSlotRecyclerView.adapter as TimeSlotAdapter
        adapter.updateTasks(tasksForCurrentDate)
    }

    private fun showTaskSelectionDialog(hour: Int) {
        // Get uncompleted tasks
        val taskRepository = TaskRepository(this)
        val uncompletedTasks = taskRepository.getAllTasks().filter { !it.completed }

        if (uncompletedTasks.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No Tasks Available")
                .setMessage("You don't have any uncompleted tasks to schedule.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // Create task titles array for the dialog
        val taskTitles = uncompletedTasks.map { it.title }.toTypedArray()

        // Show dialog with task selection
        AlertDialog.Builder(this)
            .setTitle("Schedule Task at $hour:00")
            .setItems(taskTitles) { _, which ->
                val selectedTask = uncompletedTasks[which]

                // Show dialog to select end time
                showEndTimeSelectionDialog(selectedTask, hour)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEndTimeSelectionDialog(task: Task, startHour: Int) {
        // Create array of possible end times (from start+1 to 24)
        val endTimes = (startHour + 1..24).toList()
        val endTimeStrings = endTimes.map { "$it:00" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select End Time")
            .setItems(endTimeStrings) { _, which ->
                val endHour = endTimes[which]

                // Update task with scheduled time
                task.startHour = startHour
                task.endHour = endHour
                task.scheduled = true

                // Save updated task
                val taskRepository = TaskRepository(this)
                taskRepository.updateTask(task)

                // Refresh time slots to show the scheduled task
                refreshTimeSlots()

                // Show confirmation
                AlertDialog.Builder(this)
                    .setTitle("Task Scheduled")
                    .setMessage("${task.title} scheduled from $startHour:00 to $endHour:00")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun generateCalendarGrid() {
        // Get the TableLayout
        val calendarTable = findViewById<TableLayout>(R.id.calendarTable)

        // Clear existing rows except header
        if (calendarTable.childCount > 1) {
            calendarTable.removeViews(1, calendarTable.childCount - 1)
        }

        // Get current month details
        val calendar = (currentCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1) // First day of month
        }

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Calculate week number of first day of month
        val firstDayWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        var currentWeekNumber = firstDayWeekNumber

        // Calculate offset for first day of month (Monday is 2 in Calendar)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        // Get previous month for filling initial empty cells
        val prevMonthCal = (calendar.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Build calendar grid
        var dayCounter = 1
        var row = TableRow(this)

        // Add week number cell
        val weekNumberCell = TextView(this).apply {
            text = "W$currentWeekNumber"
            setPadding(8, 8, 8, 8)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        row.addView(weekNumberCell)

        // Add empty cells for days before first day of month
        for (i in 0 until offset) {
            val prevMonthDay = daysInPrevMonth - offset + i + 1
            val dayCell = TextView(this).apply {
                text = prevMonthDay.toString()
                setPadding(8, 8, 8, 8)
                setTextColor(Color.GRAY) // Gray out days from previous month
                gravity = Gravity.CENTER

                // Make previous month days clickable
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    // Navigate to previous month with this day
                    val prevMonth = (currentCalendar.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                        set(Calendar.DAY_OF_MONTH, prevMonthDay)
                    }
                    val intent = Intent(this@CalendarActivity, TaskActivity::class.java)
                    intent.putExtra("SELECTED_DATE", prevMonth.timeInMillis)
                    startActivity(intent)
                }
            }
            row.addView(dayCell)
        }

        // Add days of current month
        var columnCounter = offset
        while (dayCounter <= daysInMonth) {
            // If we've reached the end of the row, add it and create a new row
            if (columnCounter == 7) {
                calendarTable.addView(row)
                row = TableRow(this)
                currentWeekNumber++

                // Add week number cell for new row
                val newWeekNumberCell = TextView(this).apply {
                    text = "W$currentWeekNumber"
                    setPadding(8, 8, 8, 8)
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                }
                row.addView(newWeekNumberCell)

                columnCounter = 0
            }

            // Create day cell
            val currentDay = dayCounter  // Capture the current day value
            val dayCell = TextView(this).apply {
                text = currentDay.toString()
                setPadding(8, 8, 8, 8)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER

                // Highlight current day
                if (isCurrentDay(currentDay)) {
                    setBackgroundColor(resources.getColor(R.color.purple_date, null))
                }

                // Make day clickable
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    navigateToDay(currentDay)  // Use the captured value
                }
            }

            row.addView(dayCell)
            dayCounter++
            columnCounter++
        }

        // Add empty cells for days in next month
        var nextMonthDay = 1
        while (columnCounter < 7) {
            val currentNextMonthDay = nextMonthDay  // Capture the current next month day
            val dayCell = TextView(this).apply {
                text = currentNextMonthDay.toString()
                setPadding(8, 8, 8, 8)
                setTextColor(Color.GRAY) // Gray out days from next month
                gravity = Gravity.CENTER

                // Make next month days clickable
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    // Navigate to next month with this day
                    val nextMonth = (currentCalendar.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                        set(Calendar.DAY_OF_MONTH, currentNextMonthDay)
                    }
                    val intent = Intent(this@CalendarActivity, TaskActivity::class.java)
                    intent.putExtra("SELECTED_DATE", nextMonth.timeInMillis)
                    startActivity(intent)
                }
            }
            row.addView(dayCell)
            nextMonthDay++
            columnCounter++
        }

        // Add the last row
        calendarTable.addView(row)
    }

    private fun isCurrentDay(day: Int): Boolean {
        val today = Calendar.getInstance()
        return currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH)
    }

    private fun navigateToDay(day: Int) {
        // Create a NEW calendar instance based on currentCalendar
        val selectedDate = (currentCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, day)
        }

        Log.d("CalendarActivity", "Navigating to: Year=${selectedDate.get(Calendar.YEAR)}, Month=${selectedDate.get(Calendar.MONTH)}, Day=${selectedDate.get(Calendar.DAY_OF_MONTH)}")
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("SELECTED_DATE", selectedDate.timeInMillis)
        startActivity(intent)
    }
}
