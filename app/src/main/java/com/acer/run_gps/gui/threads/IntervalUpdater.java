package com.acer.run_gps.gui.threads;

import android.util.Log;

import com.acer.run_gps.gui.fragments.ActivityFragment;
import com.acer.run_gps.gui.listener.LocationListener;

/**
 * Created by Fabian on 27.03.2016.
 */
public class IntervalUpdater implements Runnable {

    private static final double FACTOR_MS_TO_KMH = 3.6;

    private LocationListener locationListener = null;
    private ActivityFragment activityFragment = null;

    public IntervalUpdater(LocationListener locationListener, ActivityFragment activityFragment) {
        this.locationListener = locationListener;
        this.activityFragment = activityFragment;
    }

    @Override
    public void run() {
        if (activityFragment.getStartButtonEnabled() && !activityFragment.getPauseButtonEnabled()) {
            if (locationListener.getLocation() != null) {
                double speed = locationListener.getLocation().getSpeed();
                speed = locationListener.getLocation().getSpeed() * FACTOR_MS_TO_KMH;
                Log.d("####", "--- speed is " + speed + " ---");
                if (speed < 3) {
                    if (activityFragment.getLocationRequest().getFastestInterval() != 15) {
                        updateInterval(15);
                    }
                }
                if (speed >= 3 && speed < 30) {
                    if (activityFragment.getLocationRequest().getFastestInterval() != 5) {
                        updateInterval(5);
                    }
                }
                if (speed >= 30) {
                    if (activityFragment.getLocationRequest().getFastestInterval() != 2) {
                        updateInterval(2);
                    }
                }
            }
        }
    }

    private void updateInterval(long seconds) {
        if (activityFragment.googleApiClient.isConnected() && activityFragment.getLocationRequest
                () !=
                null) {
            Log.d("####", "--- stopped ---");
            activityFragment.stopLocationUpdates();

            activityFragment.getLocationRequest().setInterval(4000);
            activityFragment.getLocationRequest().setFastestInterval(4000);

            /*Log.d("####", "--- connect/disconnect ---");
            activityFragment.googleApiClient.disconnect();
            activityFragment.googleApiClient.connect();*/

            activityFragment.startLocationUpdates();
            Log.d("####", "--- started ---");
        }
    }
}
