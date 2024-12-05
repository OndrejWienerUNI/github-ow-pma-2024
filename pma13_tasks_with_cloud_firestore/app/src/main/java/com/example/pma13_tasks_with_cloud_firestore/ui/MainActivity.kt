package com.example.pma13_tasks_with_cloud_firestore.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pma13_tasks_with_cloud_firestore.data.Task
import com.example.pma13_tasks_with_cloud_firestore.data.TaskAdapter
import com.example.pma13_tasks_with_cloud_firestore.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tasks = mutableListOf<Task>() // Lokální seznam úkolů
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializace Firebase
        FirebaseApp.initializeApp(this)
        println("Firebase initialized successfully")
        firestore = FirebaseFirestore.getInstance()
        loadTasksFromFirestore()
        listenToTaskUpdates()

        // Inicializace RecyclerView
        taskAdapter = TaskAdapter(tasks) { task ->
            updateTask(task) // Callback pro změnu úkolu
        }
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTasks.adapter = taskAdapter

        // Nastavení logiky pro FloatingActionButton
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Simulace načtení dat
        // loadTasks()

        // Načtení úkolů z firestore db

    }

    @Suppress("unused")
    private fun loadTasks() {
        tasks.add(Task("1", "Buy groceries", isCompleted = false, assignedTo = "Alice"))
        tasks.add(Task("2", "Clean the house", isCompleted = false, assignedTo = ""))
        tasks.add(Task("3", "Prepare presentation", isCompleted = true, assignedTo = "Bob"))
        taskAdapter.notifyDataSetChanged()
    }

    private fun updateTask(task: Task) {
        // Update task in Firestore
        firestore.collection("tasks").document(task.id!!)
            .set(task) // Replace the entire task document
            .addOnSuccessListener {
                println("Task updated in Firestore: ${task.name}, completed: ${task.isCompleted}")
            }
            .addOnFailureListener { e ->
                println("Error updating task in Firestore: ${e.message}")
            }
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Task")

        // Vytvoření vstupního pole
        val input = EditText(this)
        input.hint = "Task name"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Tlačítka dialogu
        builder.setPositiveButton("Add") { _, _ ->
            val taskName = input.text.toString()
            if (taskName.isNotBlank()) {
                addTask(taskName)
            } else {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addTask(name: String) {
        val newTask = Task(
            id = firestore.collection("tasks").document().id, // Vygenerujeme ID
            name = name,
            isCompleted = false,
            assignedTo = ""
        )

        // Uložíme úkol do Firestore
        firestore.collection("tasks").document(newTask.id).set(newTask)
            .addOnSuccessListener {
                // tasks.add(newTask)
                // taskAdapter.notifyItemInserted(tasks.size - 1)
                loadTasksFromFirestore()
                println("Task added to Firestore: $name")
            }
            .addOnFailureListener { e ->
                println("Error adding task: ${e.message}")
            }
    }

    private fun loadTasksFromFirestore() {
        firestore.collection("tasks")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tasks.clear()
                    for (document in task.result) {
                        val task = document.toObject(Task::class.java)
                        tasks.add(task)
                    }
                    taskAdapter.notifyDataSetChanged()
                    println("Tasks loaded successfully from Firestore")
                } else {
                    println("Error loading tasks: ${task.exception?.message}")
                }
            }
    }

    private fun listenToTaskUpdates() {
        firestore.collection("tasks").addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Listen failed: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshots != null) {
                tasks.clear()
                for (document in snapshots.documents) {
                    val task = document.toObject(Task::class.java)
                    if (task != null) {
                        tasks.add(task)
                    }
                }
                taskAdapter.notifyDataSetChanged()
                println("Task updates received")
            }
        }
    }

}