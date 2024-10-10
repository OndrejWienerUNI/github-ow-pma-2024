package com.example.pma03_placing_an_order

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.example.pma03_placing_an_order.databinding.DialogOrderConfirmationBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderConfirmationDialog(private val context: Context) {

    fun show(orderSummary: String) {
        val dialog = Dialog(context)
        val dialogBinding = DialogOrderConfirmationBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val currentTime = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(Date())
        val orderId = "Order ID:\n$currentTime"

        dialogBinding.tvOrderSummary.text = orderSummary
        dialogBinding.tvOrderId.text = orderId

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
