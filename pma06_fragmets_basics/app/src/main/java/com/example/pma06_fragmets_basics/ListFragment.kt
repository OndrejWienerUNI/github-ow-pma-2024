package com.example.pma06_fragmets_basics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView

class ListFragment : Fragment() {

    private val books = listOf(
        Book(
            title = "A Short History of Nearly Everything",
            author = "Bill Bryson",
            description = "A science book that explains complex subjects in a fun and engaging way.",
            imageResId = R.drawable.a_short_history_of_nearly_everything
        ),
        Book(
            title = "Sapiens: A Brief History of Humankind",
            author = "Yuval Noah Harari",
            description = "A sweeping history of humankind, from the Stone Age to the modern era.",
            imageResId = R.drawable.sapiens_a_brief_history_of_humankind
        ),
        Book(
            title = "War and Peace",
            author = "Leo Tolstoy",
            description = "A historical novel that covers the French invasion of Russia and its impact.",
            imageResId = R.drawable.war_and_peace
        )
    )

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        listView = view.findViewById(R.id.lvBooks)

        val bookNumbers = listOf("Book 1", "Book 2", "Book 3")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            bookNumbers
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->

            val selectedBook = books[position]

            (activity as? MainActivity)?.onBookSelected(
                selectedBook.title,
                selectedBook.author,
                selectedBook.description,
                selectedBook.imageResId
            )
        }

        return view
    }
}

data class Book(
    val title: String,
    val author: String,
    val description: String,
    val imageResId: Int
)
