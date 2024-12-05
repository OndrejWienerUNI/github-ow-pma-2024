package com.example.pma13_tasks_with_cloud_firestore.data

data class Task(
    var id: String = "",
    var name: String = "",
    var isCompleted: Boolean = false,
    var assignedTo: String = ""
)
