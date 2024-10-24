package com.example.pma07_custom_fragments_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val listFragment = ListFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_list, listFragment)
                .commit()
        }
    }

    fun onLensSelected(name: String, fNumber: String, price: String, imageResId: Int) {
        val detailFragment = DetailFragment()
        val detailBundle = Bundle().apply {
            putString("name", name)
            putString("fNumber", fNumber)
            putString("price", price)
        }
        detailFragment.arguments = detailBundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_detail, detailFragment)
            .commit()

        val lensImageFragment = LensImageFragment()
        val imageBundle = Bundle().apply {
            putInt("imageResId", imageResId)
        }
        lensImageFragment.arguments = imageBundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_book_image, lensImageFragment)
            .commit()
    }
}
