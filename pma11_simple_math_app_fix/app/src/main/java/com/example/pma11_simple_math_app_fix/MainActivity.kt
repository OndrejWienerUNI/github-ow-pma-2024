package com.example.pma11_simple_math_app_fix

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_play)

        val addition = findViewById<ImageView>(R.id.addition)
        val sub = findViewById<ImageView>(R.id.sub)
        val multi = findViewById<ImageView>(R.id.multi)
        val division = findViewById<ImageView>(R.id.division)

        addition.setOnClickListener {
            val calInt = Intent(this@MainActivity,PlayActivity::class.java)
            calInt.putExtra("cals", "+")
            startActivity(calInt)
        }

        sub.setOnClickListener {
            val calInt = Intent(this@MainActivity,PlayActivity::class.java)
            calInt.putExtra("cals", "-")
            startActivity(calInt)
        }

        multi.setOnClickListener {
            val calInt = Intent(this@MainActivity,PlayActivity::class.java)
            calInt.putExtra("cals", "*")
            startActivity(calInt)
        }

        division.setOnClickListener {
            val calInt = Intent(this@MainActivity,PlayActivity::class.java)
            calInt.putExtra("cals", "/")
            startActivity(calInt)
        }


    }
}