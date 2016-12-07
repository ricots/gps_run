package com.acer.run_gps.gui.listener;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.TextView;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.activities.MapsActivity;
import com.acer.run_gps.model.Activity;
import com.acer.run_gps.model.Coordinate;
import com.acer.run_gps.model.DBAccessHelper;
import com.acer.run_gps.model.utils.StringFormatter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Fabian on 09.02.2016.
 */
public class LocationListenerCompare implements com.google.android.gms.location.LocationListener {
    // Zoom Level in Map
    private static final int ZOOM_LEVEL = 18;
    // ID of the Notification
    public static int notifyID = 2;
    // Actual Activity
    private Activity activity = null;
    // Current location
    private LatLng actualLatLng = null;
    // Previous location
    private LatLng previousLatLng = null;
    // UI Widgets
    private TextView text = null;
    private GoogleMap map = null;
    // Used for getRessources()
    private Context context = null;
    // Holds the duration of the Activity
    private long time = 0;
    // Notification Manager
    private NotificationManager notificationManager = null;
    // Notification Builder
    private NotificationCompat.Builder notificationBuilder = null;
    // Used for Notification
    private boolean showNotification = false;

    public LocationListenerCompare(GoogleMap map, Context context, Activity activity, TextView
            text) {
        this.map = map;
        this.context = context;
        this.activity = activity;
        this.text = text;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (previousLatLng == null) {
            time = System.currentTimeMillis() / 1000;
        }
        this.previousLatLng = this.actualLatLng;
        this.actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateMap();
        int id = DBAccessHelper.getInstance(context).getIDOfClosestCoordinateInActivity(location
                .getLongitude(), location.getLatitude(), activity.getId());
        Coordinate c = DBAccessHelper.getInstance(context).getCoordinate(id);
        long difference = c.getTimeFromStart() - ((System.currentTimeMillis() / 1000) - time);
        String textToSet;
        if (difference < 0) {
            textToSet = StringFormatter.getFormattedDuration((int) -difference) + context
                    .getResources().getString(R.string.faster);
        } else {
            if (text.getVisibility() != View.VISIBLE) {
                text.setVisibility(View.VISIBLE);
            }
            if (difference > 0) {
                textToSet = StringFormatter.getFormattedDuration((int) difference) + context
                        .getResources().getString(R.string.slower);
            } else {
                textToSet = context.getResources().getString(R.string.equally);
            }
        }
        text.setText(textToSet);
        if (showNotification) {
            updateNotification(textToSet);
        }
    }

    /**
     * Puts a Polyline or a Marker into the map
     */
    private void updateMap() {
        if (previousLatLng == null) {
            addStartMarker();
        } else {
            addPolyline();
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(actualLatLng, ZOOM_LEVEL));
    }

    private void addStartMarker() {
        map.addMarker(new MarkerOptions()
                .position(actualLatLng)
                .title(context.getResources().getString(R.string.marker_start))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void addPolyline() {
        // Instantiates a new Polyline object and adds points
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(actualLatLng)
                .add(previousLatLng);
        // Get back the mutable Polyline
        polylineOptions.color(Color.RED);
        map.addPolyline(polylineOptions);
    }

    public void setUpNotification() {
        notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MapsActivity.class), PendingIntent
                                .FLAG_UPDATE_CURRENT));
        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(String text) {
        notificationBuilder.setContentText(text);
        notificationManager.notify(notifyID, notificationBuilder.build());
    }

    public void cancelNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(notifyID);
        }
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }
}