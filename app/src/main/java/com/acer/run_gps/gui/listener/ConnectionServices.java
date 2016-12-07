package com.acer.run_gps.gui.listener;

import android.os.Bundle;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.fragments.ActivityFragment;
import com.acer.run_gps.gui.message.ToastFactory;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Fabian on 31.12.2015.
 */
public class ConnectionServices implements GoogleApiClient.ConnectionCallbacks {

    private ActivityFragment context = null;

    public ConnectionServices(ActivityFragment context) {
        this.context = context;
    }

    /**
     * Called when a GoogleApiClient object successfully connects
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // If user pressed the Start button before GoogleApiClient connects, we start getting
        // location updates
        /*if (context.startButtonEnabled) {
            context.startLocationUpdates();
        }*/
    }

    /**
     * The connection to Google Play services was lost for some reason
     *
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // Call connect() to attempt to re-establish the connection
        ToastFactory.makeToast(context.getContext(), context.getResources()
                .getString(R.string.toast_connection_lost) + cause);
        context.googleApiClient.connect();
    }
}