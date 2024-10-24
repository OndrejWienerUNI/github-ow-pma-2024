package com.example.pma06_fragmets_basics

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

    fun onBookSelected(title: String, author: String, description: String, imageResId: Int) {
        val detailFragment = DetailFragment()
        val detailBundle = Bundle().apply {
            putString("title", title)
            putString("author", author)
            putString("description", description)
        }
        detailFragment.arguments = detailBundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_detail, detailFragment)
            .commit()

        val bookImageFragment = BookImageFragment()
        val imageBundle = Bundle().apply {
            putInt("imageResId", imageResId)
        }
        bookImageFragment.arguments = imageBundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_book_image, bookImageFragment)
            .commit()
    }
}
