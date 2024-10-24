package com.example.pma05_toast_and_snackbar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pma05_toast_and_snackbar.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShowToast.setOnClickListener {
            showCustomToast("Hungry for a TOAST?", R.drawable.ic_default_star)
        }

        binding.btnShowSnackBar.setOnClickListener {
            showSnackbar()
        }
    }

    private fun showCustomToast(message: String, iconResId: Int) {
        val inflater: LayoutInflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val toastIcon: ImageView = layout.findViewById(R.id.toast_icon)
        val toastMessage: TextView = layout.findViewById(R.id.toast_message)

        toastIcon.setImageResource(iconResId)
        toastMessage.text = message

        val toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        toast.view = layout
        toast.show()
    }

    private fun showSnackbar() {
        Snackbar.make(binding.root, "Welcome to my SNACKBAR!", Snackbar.LENGTH_SHORT)
            .setDuration(7000)
            .setBackgroundTint(Color.parseColor("#6750A4"))
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.WHITE)
            .setAction("Close") {
                Toast.makeText(this, "Closing SNACKBAR", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
