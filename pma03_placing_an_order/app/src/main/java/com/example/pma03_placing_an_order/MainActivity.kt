package com.example.pma03_placing_an_order

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.pma03_placing_an_order.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager(binding.vpLensViewPager)
        setupRadioButtons(binding.vpLensViewPager)
        setupCheckBoxes(binding)
        setupOrderButton(binding)
        updateOrderSummary(binding)
    }

    private fun setupRadioButtons(viewPager: ViewPager) {
        val radioButtons = listOf(
            binding.rbLens1,
            binding.rbLens2,
            binding.rbLens3,
            binding.rbLens4
        )

        radioButtons.forEachIndexed { index, radioButton ->
            radioButton.setOnClickListener {
                radioButtons.forEach { rb ->
                    rb.isChecked = rb == radioButton
                }

                viewPager.currentItem = index
                updateOrderSummary(binding)
            }
        }
    }

    private fun setupCheckBoxes(binding: ActivityMainBinding) {
        binding.cbUvFilter.setOnCheckedChangeListener { _, _ -> updateOrderSummary(binding) }
        binding.cbNdFilter.setOnCheckedChangeListener { _, _ -> updateOrderSummary(binding) }
        binding.cbLensCase.setOnCheckedChangeListener { _, _ -> updateOrderSummary(binding) }
        binding.cbWarranty.setOnCheckedChangeListener { _, _ -> updateOrderSummary(binding) }
    }

    private fun updateOrderSummary(binding: ActivityMainBinding) {
        val selectedLens = when {
            binding.rbLens1.isChecked -> "RF 24-105mm"
            binding.rbLens2.isChecked -> "RF 28-70mm"
            binding.rbLens3.isChecked -> "RF 100-400mm"
            binding.rbLens4.isChecked -> "RF 100-500mm"
            else -> "No lens selected"
        }

        val selectedOptions = mutableListOf<String>()
        if (binding.cbUvFilter.isChecked) {
            selectedOptions.add(binding.cbUvFilter.text.toString())
        }
        if (binding.cbNdFilter.isChecked) {
            selectedOptions.add(binding.cbNdFilter.text.toString())
        }
        if (binding.cbLensCase.isChecked) {
            selectedOptions.add(binding.cbLensCase.text.toString())
        }
        if (binding.cbWarranty.isChecked) {
            selectedOptions.add(binding.cbWarranty.text.toString())
        }

        val finalSummary = if (selectedOptions.isEmpty()) {
            selectedLens
        } else {
            "$selectedLens, ${selectedOptions.joinToString(", ")}"
        }

        binding.tvOrderSummary.text = buildString {
            append("Order Summary: ")
            append(finalSummary)
        }
    }

    private fun setupOrderButton(binding: ActivityMainBinding) {
        binding.btnPlaceOrder.setOnClickListener {
            val orderSummary = binding.tvOrderSummary.text.removePrefix("Order Summary: ")
            val dialog = OrderConfirmationDialog(this)
            dialog.show(orderSummary.toString())
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val images = listOf(
            R.drawable.rf_24_105,
            R.drawable.rf_28_70,
            R.drawable.rf_100_400,
            R.drawable.rf_100_500
        )

        val adapter = object : PagerAdapter() {
            override fun getCount(): Int = images.size

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view == `object`
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = layoutInflater.inflate(R.layout.image_slider_item, container, false)
                val imageView: ImageView = view.findViewById(R.id.image_view)
                imageView.setImageResource(images[position])

                container.addView(view)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }
        }

        viewPager.adapter = adapter
        viewPager.currentItem = 0
    }
}
