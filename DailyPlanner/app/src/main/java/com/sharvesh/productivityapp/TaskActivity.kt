package com.sharvesh.productivityapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Log

class TaskActivity : AppCompatActivity() {

    private lateinit var tvCurrentFullDate: TextView
    private lateinit var tvPrevDate: TextView
    private lateinit var tvCurrentDay: TextView
    private lateinit var tvNextDate: TextView
    private lateinit var tvPrevWeek: TextView
    private lateinit var tvCurrentWeek: TextView
    private lateinit var tvNextWeek: TextView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvPrevYear: TextView
    private lateinit var tvCurrentYear: TextView
    private lateinit var tvNextYear: TextView

    private var currentCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        // Initialize views
        tvCurrentFullDate = findViewById(R.id.tvCurrentFullDate)
        tvPrevDate = findViewById(R.id.tvPrevDate)
        tvCurrentDay = findViewById(R.id.tvCurrentDay)
        tvNextDate = findViewById(R.id.tvNextDate)
        tvPrevWeek = findViewById(R.id.tvPrevWeek)
        tvCurrentWeek = findViewById(R.id.tvCurrentWeek)
        tvNextWeek = findViewById(R.id.tvNextWeek)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        tvPrevYear = findViewById(R.id.tvPrevYear)
        tvCurrentYear = findViewById(R.id.tvCurrentYear)
        tvNextYear = findViewById(R.id.tvNextYear)

        // Set up task creation buttons
        findViewById<Button>(R.id.btnAddOverdueTask).setOnClickListener {
            showAddTaskDialog("overdue")
        }

        findViewById<Button>(R.id.btnAddTodayTask).setOnClickListener {
            showAddTaskDialog("today")
        }

        findViewById<Button>(R.id.btnAddUpcomingTask).setOnClickListener {
            showAddTaskDialog("upcoming")
        }



        // Get selected date from intent if available
        val selectedDateMillis = intent.getLongExtra("SELECTED_DATE", -1L)
        if (selectedDateMillis != -1L) {
            Log.d("TaskActivity", "Received timestamp: $selectedDateMillis")
            currentCalendar = Calendar.getInstance()
            currentCalendar.timeInMillis = selectedDateMillis
            Log.d("TaskActivity", "Set to: Year=${currentCalendar.get(Calendar.YEAR)}, Month=${currentCalendar.get(Calendar.MONTH)}, Day=${currentCalendar.get(Calendar.DAY_OF_MONTH)}")
        }

        // Initialize calendar
        updateDateDisplay()
        updateTaskCategories()

        // Set up navigation buttons
        findViewById<View>(R.id.btnPrevDay).setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDateDisplay()
            updateTaskCategories()
        }

        findViewById<View>(R.id.btnNextDay).setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDateDisplay()
            updateTaskCategories()
        }

        // Today button
        findViewById<View>(R.id.btnToday).setOnClickListener {
            currentCalendar = Calendar.getInstance()
            updateDateDisplay()
            updateTaskCategories()
        }
    }

    private fun updateDateDisplay() {
        // Format for full date
        val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        tvCurrentFullDate.text = fullDateFormat.format(currentCalendar.time)

        // Get previous, current, and next day
        val prevCal = currentCalendar.clone() as Calendar
        prevCal.add(Calendar.DAY_OF_MONTH, -1)

        val nextCal = currentCalendar.clone() as Calendar
        nextCal.add(Calendar.DAY_OF_MONTH, 1)

        // Update day numbers
        tvPrevDate.text = prevCal.get(Calendar.DAY_OF_MONTH).toString()
        tvCurrentDay.text = currentCalendar.get(Calendar.DAY_OF_MONTH).toString()
        tvNextDate.text = nextCal.get(Calendar.DAY_OF_MONTH).toString()

        // Update week numbers
        val prevWeek = prevCal.get(Calendar.WEEK_OF_YEAR)
        val currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR)
        val nextWeek = nextCal.get(Calendar.WEEK_OF_YEAR)

        tvPrevWeek.text = "W$prevWeek"
        tvCurrentWeek.text = "W$currentWeek"
        tvNextWeek.text = "W$nextWeek"

        // Update month
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        tvCurrentMonth.text = monthFormat.format(currentCalendar.time)

        // Update years
        tvPrevYear.text = prevCal.get(Calendar.YEAR).toString()
        tvCurrentYear.text = currentCalendar.get(Calendar.YEAR).toString()
        tvNextYear.text = nextCal.get(Calendar.YEAR).toString()
    }

    private fun showAddTaskDialog(category: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskTitleEditText = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val dueDatePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)

        // Set default date based on category
        val calendar = Calendar.getInstance()
        when (category) {
            "overdue" -> calendar.add(Calendar.DAY_OF_MONTH, -1) // Yesterday
            "today" -> {} // Today (default)
            "upcoming" -> calendar.add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
            "done" -> {} // Today (default)
        }

        dueDatePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, which ->
                val taskTitle = taskTitleEditText.text.toString()
                if (taskTitle.isNotEmpty()) {
                    // Create date from DatePicker
                    val taskCalendar = Calendar.getInstance()
                    taskCalendar.set(
                        dueDatePicker.year,
                        dueDatePicker.month,
                        dueDatePicker.dayOfMonth
                    )

                    // Create and save the new task
                    val newTask = Task(taskTitle, taskCalendar.time)
                    if (category == "done") {
                        newTask.completed = true
                    }

                    saveTask(newTask)
                    updateTaskCategories() // Refresh the task lists
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun saveTask(task: Task) {
        val taskRepository = TaskRepository(this)
        taskRepository.addTask(task)
    }

    private fun updateTaskCategories() {
        val taskRepository = TaskRepository(this)
        val allTasks = taskRepository.getAllTasks()

        // Filter tasks by category based on current date
        val currentDate = currentCalendar.time

        val overdueTasks = allTasks.filter {
            !it.completed && it.dueDate.before(currentDate) && !isSameDay(it.dueDate, currentDate)
        }

        val todayTasks = allTasks.filter {
            !it.completed && isSameDay(it.dueDate, currentDate)
        }

        val upcomingTasks = allTasks.filter {
            !it.completed && it.dueDate.after(currentDate) && !isSameDay(it.dueDate, currentDate)
        }

        val doneTasks = allTasks.filter {
            it.completed && isSameDay(it.dueDate, currentDate)
        }

        // Update UI with task counts
        findViewById<TextView>(R.id.tvOverdueCount).text = "${overdueTasks.size} tasks"
        findViewById<TextView>(R.id.tvTodayCount).text = "${todayTasks.size} tasks"
        findViewById<TextView>(R.id.tvUpcomingCount).text = "${upcomingTasks.size} tasks"
        findViewById<TextView>(R.id.tvDoneCount).text = "${doneTasks.size} tasks"

        // Update task lists in each category
        try {
            val overdueContainer = findViewById<ViewGroup>(R.id.overdueTasksContainer)
            val todayContainer = findViewById<ViewGroup>(R.id.todayTasksContainer)
            val upcomingContainer = findViewById<ViewGroup>(R.id.upcomingTasksContainer)
            val doneContainer = findViewById<ViewGroup>(R.id.doneTasksContainer)

            if (overdueContainer != null) updateTaskList(overdueTasks, overdueContainer)
            if (todayContainer != null) updateTaskList(todayTasks, todayContainer)
            if (upcomingContainer != null) updateTaskList(upcomingTasks, upcomingContainer)
            if (doneContainer != null) updateTaskList(doneTasks, doneContainer)
        } catch (e: Exception) {
            // Handle case where containers aren't defined yet
        }
    }

    private fun updateTaskList(tasks: List<Task>, container: ViewGroup) {
        container.removeAllViews()

        for (task in tasks) {
            val taskView = layoutInflater.inflate(R.layout.task_item, container, false)

            val checkBox = taskView.findViewById<CheckBox>(R.id.taskCheckbox)
            val taskTitle = taskView.findViewById<TextView>(R.id.taskTitle)

            checkBox.isChecked = task.completed
            taskTitle.text = task.title

            // Set up checkbox listener to mark task as complete
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                task.completed = isChecked
                val taskRepository = TaskRepository(this)
                taskRepository.updateTask(task)
                // Refresh the task lists
                updateTaskCategories()
            }

            container.addView(taskView)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
