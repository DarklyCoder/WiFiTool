package com.darklycoder.wifitool;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

public class TipDialog {

    public static void showTipDialog(Context context, String title, String content, final View.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title)
                .setMessage(content)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();

                    if (null != listener) {
                        listener.onClick(null);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.parseColor("#191F25"));

        Button btnPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositive.setTextColor(Color.parseColor("#F92833"));
    }

}
