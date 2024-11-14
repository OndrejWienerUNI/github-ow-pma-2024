package com.example.pma10_image_from_gallery

import android.animation.ObjectAnimator
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.pma10_image_from_gallery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var originalBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    binding.ivImage.setImageURI(it)
                    originalBitmap = (binding.ivImage.drawable as BitmapDrawable).bitmap
                }
            }

        binding.btnPickImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnApply.setOnClickListener {
            applyBrightnessAndContrast()
        }

        binding.seekBarBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val brightnessPercent = progress - 100
                val brightnessText = "Brightness: $brightnessPercent%"
                binding.tvBrightnessValue.text = brightnessText
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekBarContrast.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val contrastText = "Contrast: $progress%"
                binding.tvContrastValue.text = contrastText
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnApply.setOnClickListener {
            applyBrightnessAndContrast()
        }
    }


    private fun applyBrightnessAndContrast() {
        originalBitmap?.let { bitmap ->
            val brightness = (binding.seekBarBrightness.progress - 100) / 100f
            val contrast = binding.seekBarContrast.progress / 100f

            val adjustedBitmap = adjustBitmapBrightnessContrast(bitmap, brightness, contrast)

            val fadeIn = ObjectAnimator.ofFloat(binding.ivImage, "alpha", 0f, 1f)
            fadeIn.duration = 500
            fadeIn.start()

            binding.ivImage.setImageBitmap(adjustedBitmap)
        }
    }


    private fun adjustBitmapBrightnessContrast(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float
    ): Bitmap {
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
        val canvas = Canvas(adjustedBitmap)
        val paint = Paint()

        val contrastMatrix = ColorMatrix().apply {
            setScale(contrast, contrast, contrast, 1f)
        }

        contrastMatrix.set(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness * 255,
                0f, contrast, 0f, 0f, brightness * 255,
                0f, 0f, contrast, 0f, brightness * 255,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return adjustedBitmap
    }
}