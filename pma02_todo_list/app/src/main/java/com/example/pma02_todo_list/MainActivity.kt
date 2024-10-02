package com.example.pma02_todo_list

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addTaskButton: Button = findViewById(R.id.btn_add_task)

        val activeTasksLayout: LinearLayout = findViewById(R.id.ll_active_tasks)

        addTaskButton.setOnClickListener {
            addNewTask(activeTasksLayout)
        }
    }

    private fun addNewTask(parentLayout: LinearLayout) {
        // Use LayoutInflater to inflate the todo_item.xml layout
        val inflater = LayoutInflater.from(this)
        val todoItemView = inflater.inflate(R.layout.todo_item, parentLayout, false)
        // Find views within the inflated item
        val taskTextView: TextView = todoItemView.findViewById(R.id.et_task_description)
        val checkmarkIcon: ImageView = todoItemView.findViewById(R.id.iv_checkmark)
        val trashIcon: ImageView = todoItemView.findViewById(R.id.iv_trash)

        taskTextView.text = ""

        // Set click listeners for icons
        checkmarkIcon.setOnClickListener {
            val isCompleted = taskTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG > 0
            if (isCompleted) {
                taskTextView.paintFlags = taskTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            } else {
                taskTextView.paintFlags = taskTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        trashIcon.setOnClickListener {
            parentLayout.removeView(todoItemView)
        }

        // Add the inflated view to the parent layout
        parentLayout.addView(todoItemView)
    }
}
