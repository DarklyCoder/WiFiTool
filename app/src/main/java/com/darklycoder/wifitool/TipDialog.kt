package com.darklycoder.wifitool

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AlertDialog

object TipDialog {

    fun showTipDialog(context: Context, title: String, content: String, listener: View.OnClickListener?) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(title)
                .setMessage(content)
                .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("确定") { dialog, _ ->
                    dialog.dismiss()
                    listener?.onClick(null)
                }

        val dialog = builder.create()
        dialog.show()

        val btnNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        btnNegative.setTextColor(Color.parseColor("#191F25"))

        val btnPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        btnPositive.setTextColor(Color.parseColor("#F92833"))
    }

}
