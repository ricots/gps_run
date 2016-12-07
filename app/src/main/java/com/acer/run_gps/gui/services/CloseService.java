package com.acer.run_gps.gui.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Fabian on 26.03.2016.
 */
public class CloseService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
