package com.acer.run_gps.gui.message;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Fabian on 01.01.2016.
 * <p/>
 * This class makes toasts for example Toasts Hawaii. I like toasts.
 */
public class ToastFactory {

    public static void makeToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}