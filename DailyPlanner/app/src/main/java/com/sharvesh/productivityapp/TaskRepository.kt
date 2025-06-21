package com.sharvesh.productivityapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class TaskRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addTask(task: Task) {
        val tasks = getAllTasks().toMutableList()
        tasks.add(task)
        saveTasks(tasks)
    }

    fun getAllTasks(): List<Task> {
        val tasksJson = sharedPreferences.getString("tasks", "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(tasksJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun updateTask(task: Task) {
        val tasks = getAllTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            saveTasks(tasks)
        }
    }

    private fun saveTasks(tasks: List<Task>) {
        val editor = sharedPreferences.edit()
        val tasksJson = gson.toJson(tasks)
        editor.putString("tasks", tasksJson)
        editor.apply()
    }
}
