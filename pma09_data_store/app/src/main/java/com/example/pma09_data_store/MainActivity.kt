package com.example.pma09_data_store

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pma09_data_store.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Access SharedPreferences
        val sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Save data with custom toast on click
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val ageString = binding.etAge.text.toString().trim()

            if (ageString.isBlank()) {
                showCustomToast("Hey, fill in the age!", R.drawable.ic_default_triangle)
            } else {
                val age = ageString.toInt()
                val isAdult = binding.cbAdult.isChecked
                if ((age < 18 && isAdult) || (age >= 18 && !isAdult)) {
                    showCustomToast("That doesn't add up, so I won't save anything.",
                        R.drawable.ic_default_triangle)
                } else {
                    showCustomToast("Alright, saving...", R.drawable.ic_default_triangle)
                    editor.apply {
                        putString("name", name)
                        putInt("age", age)
                        putBoolean("isAdult", isAdult)
                        apply()
                    }
                }
            }
        }

        // Load data with custom toast on click
        binding.btnLoad.setOnClickListener {
            val name = sharedPref.getString("name", null)
            val age = sharedPref.getInt("age", 0)
            val isAdult = sharedPref.getBoolean("isAdult", false)

            binding.etName.setText(name)
            binding.etAge.setText(age.toString())
            binding.cbAdult.isChecked = isAdult

            showCustomToast("Data loaded successfully!", R.drawable.ic_default_triangle)
        }
    }

    private fun showCustomToast(message: String, iconResId: Int) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast,
            findViewById(R.id.custom_toast_container))

        val toastIcon: ImageView = layout.findViewById(R.id.toast_icon)
        val toastMessage: TextView = layout.findViewById(R.id.toast_message)

        toastIcon.setImageResource(iconResId)
        toastMessage.text = message

        // using the deprecated view option to enable the custom toast layout
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
