package com.example.pma07_custom_fragments_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class LensImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lens_image, container, false)

        val imageViewLens: ImageView = view.findViewById(R.id.lens_image_view)

        arguments?.let {
            val imageResId = it.getInt("imageResId", R.drawable.default_lens_image)
            imageViewLens.setImageResource(imageResId)
        }

        return view
    }
}
