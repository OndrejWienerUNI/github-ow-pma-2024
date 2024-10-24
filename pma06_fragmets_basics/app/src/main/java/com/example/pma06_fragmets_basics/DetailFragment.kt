package com.example.pma06_fragmets_basics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class DetailFragment : Fragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var textViewAuthor: TextView
    private lateinit var textViewDescription: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        textViewTitle = view.findViewById(R.id.tvTitle)
        textViewAuthor = view.findViewById(R.id.tvAuthor)
        textViewDescription = view.findViewById(R.id.tvDescription)

        arguments?.let {
            val title = it.getString("title") ?: "Unknown Title"
            val author = it.getString("author") ?: "Unknown Author"
            val description = it.getString("description") ?: "No description available"

            textViewTitle.text = title
            textViewAuthor.text = author
            textViewDescription.text = description
        }

        return view
    }
}
