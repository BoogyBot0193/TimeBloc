package com.sharvesh.productivityapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimeSlotAdapter(
    private val context: Context,
    private val timeSlots: List<Int>,
    private val onTimeSlotClick: (Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    private var scheduledTasks: List<Task> = emptyList()

    class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val container: FrameLayout = view.findViewById(R.id.timeSlotContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.time_slot_item, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val hour = timeSlots[position]
        holder.tvHour.text = "${hour}:00"


        // Clear previous views
        holder.container.removeAllViews()

        // Check if there's a task scheduled for this Whour
        val tasksForThisHour = scheduledTasks.filter { task ->
            task.scheduled && task.startHour <= hour && task.endHour > hour
        }

        if (tasksForThisHour.isNotEmpty()) {
            // Display the task in this time slot
            val task = tasksForThisHour // Just show the first task if multiple

            val taskView = LayoutInflater.from(context).inflate(R.layout.scheduled_task_item, holder.container, false)
            val taskTitle = taskView.findViewById<TextView>(R.id.scheduledTaskTitle)
            taskTitle.text = task[0].title

            holder.container.addView(taskView)
        } else {
            // Make empty slot clickable
            holder.container.setOnClickListener {
                onTimeSlotClick(hour)
            }
        }
    }

    override fun getItemCount(): Int {
        return timeSlots.size
    }

    fun updateTasks(tasks: List<Task>) {
        scheduledTasks = tasks
        notifyDataSetChanged()
    }

}