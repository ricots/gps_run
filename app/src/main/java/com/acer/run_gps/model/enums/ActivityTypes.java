package com.acer.run_gps.model.enums;

import android.content.Context;

import com.acer.run_gps.R;

/**
 * Created by Fabian on 29.12.2015.
 */
public class ActivityTypes {
    public enum Type {
        RUNNING(R.string.running),
        WALKING(R.string.walking),
        TREKKING(R.string.trekking),
        CYCLING(R.string.cycling),
        SKIING(R.string.skiing),
        OTHER(R.string.other);

        private int id = -1;

        Type(int id) {
            this.id = id;
        }

        public String toString(Context context) {
            return context.getResources().getString(id);
        }
    }
}