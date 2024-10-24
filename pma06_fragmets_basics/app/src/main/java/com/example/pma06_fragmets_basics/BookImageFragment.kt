package com.example.pma06_fragmets_basics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class BookImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_image, container, false)

        val bookImageView: ImageView = view.findViewById(R.id.book_image)

        arguments?.let {
            val imageResId = it.getInt("imageResId", R.drawable.sample_book_image)
            bookImageView.setImageResource(imageResId)
        }

        return view
    }
}
