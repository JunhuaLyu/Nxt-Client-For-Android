package com.other.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.google.zxing.WriterException;

public class QRCode {
    public static void showQRCode(Activity context, String text){
        try {
            DisplayMetrics dm = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = 3 * dm.widthPixels / 4;

            Bitmap qrBmp = org.Zxing.encoding.Encoder.createQRCode(text, width);
            TextView view = new TextView(context);
            view.setText(text);
            view.setPadding(20, 20, 20, 20);
            BitmapDrawable drawable = new BitmapDrawable(qrBmp);
            drawable.setBounds(0, 0, width, width);
            view.setCompoundDrawables(null, null, null, drawable);
            new AlertDialog.Builder(context)
                    .setView(view)
                    .show();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
