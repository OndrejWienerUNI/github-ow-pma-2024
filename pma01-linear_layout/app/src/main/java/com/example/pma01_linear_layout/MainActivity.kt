package com.example.pma01_linear_layout

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

        val etName = findViewById<EditText>(R.id.etName)
        val etSurname = findViewById<EditText>(R.id.etSurname)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etAge = findViewById<EditText>(R.id.etAge)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val twResult = findViewById<TextView>(R.id.twResult)

        // btnSend listener
        btnSend.setOnClickListener{
            val name = etName.text.toString()
            val surname = etSurname.text.toString()
            val address = etAddress.text.toString()
            val age = etAge.text.toString()

            // View in text field
            val formattedText = "My name is $name $surname, i live in $address."

            twResult.text = formattedText
        }

        // btnClear
        btnClear.setOnClickListener{
            etName.text.clear()
            etSurname.text.clear()
            etAddress.text.clear()
            etAge.text.clear()
            twResult.text = ""
        }
    }
}