package com.example.pma07_custom_fragments_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class DetailFragment : Fragment() {

    private lateinit var textViewName: TextView
    private lateinit var textViewFNumber: TextView
    private lateinit var textViewPrice: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        textViewName = view.findViewById(R.id.tvLensName)
        textViewFNumber = view.findViewById(R.id.tvLensFNumber)
        textViewPrice = view.findViewById(R.id.tvLensPrice)

        arguments?.let {
            val name = it.getString("name") ?: "Unknown Lens"
            val fNumber = it.getString("fNumber") ?: "Unknown F-Number"
            val price = it.getString("price") ?: "Unknown Price"

            textViewName.text = name
            textViewFNumber.text = fNumber
            textViewPrice.text = price
        }

        return view
    }
}
