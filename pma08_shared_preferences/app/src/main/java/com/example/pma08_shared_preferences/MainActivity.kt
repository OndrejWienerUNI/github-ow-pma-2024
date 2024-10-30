package com.example.pma08_shared_preferences

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pma08_shared_preferences.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Access SharedPreferences
        val sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Save data with an on-click listener
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val ageString = binding.etAge.text.toString().trim()

            if (ageString.isBlank()) {
                Toast.makeText(this, "Hey, fill in the age!",
                    Toast.LENGTH_SHORT).show()
            } else {
                val age = ageString.toInt()
                val isAdult = binding.cbAdult.isChecked
                if ((age < 18 && isAdult) || (age >= 18 && !isAdult)) {
                    Toast.makeText(this, "That doesn't add up, so I won't save anything.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Alright, saving...",
                        Toast.LENGTH_SHORT).show()
                    editor.apply {
                        putString("name", name)
                        putInt("age", age)
                        putBoolean("isAdult", isAdult)
                        apply()
                    }
                }
            }
        }

        // load data with an on-click listener
        binding.btnLoad.setOnClickListener {
            val name = sharedPref.getString("name", null)
            val age = sharedPref.getInt("age", 0)
            val isAdult = sharedPref.getBoolean("isAdult", false)

            binding.etName.setText(name)
            binding.etAge.setText(age.toString())
            binding.cbAdult.isChecked = isAdult
        }

    }
}
