package com.example.pma09_data_store

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.pma09_data_store.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Extension property to create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Keys for DataStore entries
    companion object {
        val NAME_KEY = stringPreferencesKey("name")
        val AGE_KEY = intPreferencesKey("age")
        val IS_ADULT_KEY = booleanPreferencesKey("isAdult")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Save data with custom toast on click
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val ageString = binding.etAge.text.toString().trim()

            if (name.isBlank()) {
                showCustomToast("Hey, fill in the name!", R.drawable.ic_default_triangle)
            } else if (ageString.isBlank()) {
                showCustomToast("Hey, fill in the age!", R.drawable.ic_default_triangle)
            } else {
                val age = ageString.toInt()
                val isAdult = binding.cbAdult.isChecked
                if ((age < 18 && isAdult) || (age >= 18 && !isAdult)) {
                    showCustomToast("That doesn't add up, so I won't save anything.",
                        R.drawable.ic_default_triangle)
                } else {
                    showCustomToast("Alright, saving...", R.drawable.ic_default_triangle)
                    saveData(name, age, isAdult)
                }
            }
        }

        // Load data with custom toast on click
        binding.btnLoad.setOnClickListener {
            loadData()
        }
    }

    private fun saveData(name: String, age: Int, isAdult: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[NAME_KEY] = name
                preferences[AGE_KEY] = age
                preferences[IS_ADULT_KEY] = isAdult
            }
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val name: String?
            val age: Int
            val isAdult: Boolean

            // Blocking read (for simplicity in this example) to retrieve the stored data
            runBlocking {
                val preferences = dataStore.data.first()
                name = preferences[NAME_KEY]
                age = preferences[AGE_KEY] ?: 0
                isAdult = preferences[IS_ADULT_KEY] ?: false
            }

            // Update the UI on the main thread
            runOnUiThread {
                binding.etName.setText(name)
                binding.etAge.setText(age.toString())
                binding.cbAdult.isChecked = isAdult
                showCustomToast("Data loaded successfully!", R.drawable.ic_default_triangle)
            }
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
