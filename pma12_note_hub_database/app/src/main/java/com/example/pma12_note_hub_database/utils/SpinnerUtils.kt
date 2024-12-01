package com.example.pma12_note_hub_database.utils

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

// Utility class for spinner setup
object SpinnerUtils {

    // Populates a Spinner with the given items and sets the selection listener
    fun populateSpinner(
        context: Context,
        spinner: Spinner,
        items: List<String>,
        defaultSelection: String = "",
        onItemSelected: (String) -> Unit
    ) {
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set default selection if provided
        if (defaultSelection.isNotEmpty()) {
            val position = items.indexOf(defaultSelection)
            if (position >= 0) spinner.setSelection(position)
        }

        // Set item selected listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected(items[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Define behavior for no selection
            }
        }
    }
}
