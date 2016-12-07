package com.acer.run_gps.gui.listener;

import android.content.Context;
import android.location.LocationManager;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.message.ToastFactory;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Fabian on 07.02.2016.
 */
public class MyLocationButtonListener implements GoogleMap.OnMyLocationButtonClickListener {

    private Context context = null;

    public MyLocationButtonListener(Context context) {
        this.context = context;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context
                .LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled) {
            ToastFactory.makeToast(context, context.getResources().getString(R.string
                    .toast_enable_gps));
        }
        return false;
    }
}
