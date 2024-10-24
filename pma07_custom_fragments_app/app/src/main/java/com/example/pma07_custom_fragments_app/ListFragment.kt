package com.example.pma07_custom_fragments_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView

data class Lens(
    val name: String,
    val fNumber: String,
    val price: String,
    val imageResId: Int
)

class ListFragment : Fragment() {

    private val lenses = listOf(
        Lens(
            name = "Canon RF 100-400mm F5.6-8 IS USM",
            fNumber = "F5.6-8",
            price = "$649.99",
            imageResId = R.drawable.rf_100_400
        ),
        Lens(
            name = "Canon RF 100-500mm F4.5-7.1 L IS USM",
            fNumber = "F4.5-7.1",
            price = "$2,699.00",
            imageResId = R.drawable.rf_100_500
        ),
        Lens(
            name = "Canon RF 24-105mm F4 L IS USM",
            fNumber = "F4",
            price = "$1,099.00",
            imageResId = R.drawable.rf_24_105
        ),
        Lens(
            name = "Canon RF 28-70mm F2 L USM",
            fNumber = "F2",
            price = "$3,099.00",
            imageResId = R.drawable.rf_28_70
        ),
        Lens(
            name = "Canon RF 600mm F11 IS STM",
            fNumber = "F11",
            price = "$699.99",
            imageResId = R.drawable.rf_600
        )
    )

    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        listView = view.findViewById(R.id.lvLenses)

        val lensNumbers = listOf("Lens 1", "Lens 2", "Lens 3", "Lens 4", "Lens 5")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            lensNumbers
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedLens = lenses[position]

            (activity as? MainActivity)?.onLensSelected(
                selectedLens.name,
                selectedLens.fNumber,
                selectedLens.price,
                selectedLens.imageResId
            )
        }

        return view
    }
}