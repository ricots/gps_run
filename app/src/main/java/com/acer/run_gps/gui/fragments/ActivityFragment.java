package com.acer.run_gps.gui.fragments;

/**
 * Created by Fabian on 28.12.2015.
 */

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.activities.MainActivity;
import com.acer.run_gps.gui.activities.MapsActivity;
import com.acer.run_gps.gui.activities.SaveActivityActivity;
import com.acer.run_gps.gui.listener.ConnectionFailed;
import com.acer.run_gps.gui.listener.ConnectionServices;
import com.acer.run_gps.gui.listener.LocationListener;
import com.acer.run_gps.gui.listener.MyLocationButtonListener;
import com.acer.run_gps.gui.message.ToastFactory;
import com.acer.run_gps.gui.services.CloseService;
import com.acer.run_gps.gui.threads.IntervalUpdater;
import com.acer.run_gps.model.Activity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hp1 on 21-01-2015.
 */
public class ActivityFragment extends Fragment implements OnMapReadyCallback {
    // Used for LocationUpdates
    public static final int FACTOR_BETWEEN_INTERVALS = 1 / 3;
    // ID of the Notification
    public static int notifyID = 1;
    // Minimum duration of an Activity
    private final int MIN_DURATION_OF_ACTIVITY_IN_SECONDS = 5;
    // Minimum distance of an Activity
    private final int MIN_DISTANCE_OF_ACTIVITY_IN_METERS = 10;
    private final float FACTOR_DISPLACEMENT = 1 / 4;
    // Used for the Notification
    private final int GOING_ON = 0;
    private final int PAUSE = 1;
    // Interval for location updates. Inexact. Updates may be more or less frequent
    public long updateIntervalInMilliseconds = -1;
    // Fastest rate for location updates. Exact. Updates will never be more frequent than this value
    public long fastestUpdateIntervalInMilliseconds = -1;
    // Minimum displacement between location updates in meters
    public float smallestDisplacementInMeter = -1;
    // Entry point to Google Play services
    public GoogleApiClient googleApiClient = null;
    // Value changes when the user presses the Start and Stop Button
    public Boolean startButtonEnabled = false;
    // Value changes when the user presses the Pause and Resume Button
    public Boolean pauseButtonEnabled = false;
    // Request to the FusedLocationProviderApi
    protected LocationRequest locationRequest = null;
    // Listener which is called when the location changes
    protected LocationListener locationListener = null;
    // Listener which handles the states of the connection to the Play Services
    protected GoogleApiClient.ConnectionCallbacks connectionCallbacks = null;
    // Listener which is called when the connection to the Play Services failed
    protected GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = null;
    ScheduledExecutorService scheduledExecutorService = null;
    // UI Widgets
    private Button startStopButton = null;
    private Button pauseResumeButton = null;
    // Google Map
    private GoogleMap map = null;
    // Date as long when the user presses the Pause Activity Button
    private long dateOnPaused = -1;
    // Total duration of pause in milliseconds
    private long durationPausedInMilliseconds = -1;
    // NotificationManager
    private NotificationManager notificationManager = null;
    private NotificationCompat.Builder notificationBuilder = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment, container, false);

        // Locate UI Widgets
        startStopButton = (Button) v.findViewById(R.id.activityStartStop);
        pauseResumeButton = (Button) v.findViewById(R.id.activityPauseResume);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.activityGoogleMap);
        mapFragment.getMapAsync(this);

        setMapType();

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!startButtonEnabled) {
                    LocationManager locationManager = (LocationManager) getContext()
                            .getSystemService(Context.LOCATION_SERVICE);
                    boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager
                            .GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager
                            .NETWORK_PROVIDER);
                    if (gpsEnabled) {
                        Location actualPosition = getActualPosition();
                        if (actualPosition != null) {
                            if (actualPosition.getAccuracy() > MapsActivity.MIN_ACCURACY) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle(getResources().getString(R.string
                                                .dialog_bad_accuracy_title))
                                        .setMessage(getResources().getString(R.string
                                                .dialog_bad_accuracy_message))
                                        .setPositiveButton(android.R.string.yes, new DialogInterface
                                                .OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                // Close the dialog
                                                dialog.dismiss();
                                                start();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, null)
                                        .create().show();
                            } else {
                                start();
                            }
                        } else {
                            new AlertDialog.Builder(getContext())
                                    .setTitle(getResources().getString(R.string
                                            .dialog_no_location_title))
                                    .setMessage(getResources().getString(R.string
                                            .dialog_no_location_message))
                                    .setPositiveButton(android.R.string.ok, null)
                                    .create().show();
                        }
                    } else {
                        ToastFactory.makeToast(getContext(), getResources().getString(R.string
                                .toast_enable_gps));
                    }
                } else {
                    startButtonEnabled = false;
                    // Stop location updates
                    stopLocationUpdates();
                    if (scheduledExecutorService != null) {
                        scheduledExecutorService.shutdownNow();
                        scheduledExecutorService = null;
                    }
                    locationListener.setLastCoordinateIsPause(true);
                    // User pressed the Stop Button after he pressed the Pause Button. He never
                    // pressed the Resume Button
                    if (pauseButtonEnabled) {
                        durationPausedInMilliseconds += System.currentTimeMillis() - dateOnPaused;
                    }
                    // Get the Activity object from the listener...
                    Activity activity = locationListener.getActivity();
                    // ... and set the duration minus the time where the Activity was paused
                    activity.setDuration((int) ((System.currentTimeMillis() - activity.getDate()
                            - durationPausedInMilliseconds) / 1000));

                    if (activity.getCoordinates() == null || activity.getCoordinates().size() ==
                            0) {
                        ToastFactory.makeToast(getContext(), getResources().getString(R.string
                                .activity_fragment_no_coordinates));
                        startStopButton.setText(getResources().getString(R.string
                                .activity_fragment_start));
                        pauseResumeButton.setVisibility(View.GONE);
                    } else {
                        if (activity.getDuration() < MIN_DURATION_OF_ACTIVITY_IN_SECONDS) {
                            ToastFactory.makeToast(getContext(), getResources().getString(R
                                    .string.activity_fragment_duration_too_short));
                            map.clear();
                            startStopButton.setText(getResources().getString(R.string
                                    .activity_fragment_start));
                            pauseResumeButton.setVisibility(View.GONE);
                        } else {
                            if (activity.getDistance() > MIN_DISTANCE_OF_ACTIVITY_IN_METERS) {
                                // Set the last coordinate as end point
                                activity.getCoordinates().get(activity.getCoordinates().size() - 1)
                                        .setEnd(true);
                                // Start Activity where the user can save the Activity
                                Intent intent = new Intent(getActivity(), SaveActivityActivity
                                        .class);

                                intent.putExtra("activity", locationListener.getActivity());
                                startActivityForResult(intent, 1);
                            } else {
                                ToastFactory.makeToast(getContext(), getResources().getString(R
                                        .string.activity_fragment_distance_too_short));
                                map.clear();
                                startStopButton.setText(getResources().getString(R.string
                                        .activity_fragment_start));
                                pauseResumeButton.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        });

        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pauseButtonEnabled) {
                    pauseButtonEnabled = true;
                    pauseResumeButton.setText(getResources().getString(R.string
                            .activity_fragment_resume));

                    // Save the actual date to calculate the duration of the pause
                    dateOnPaused = System.currentTimeMillis();
                    locationListener.setLastCoordinateIsPause(false);

                    stopLocationUpdates();
                } else {
                    pauseButtonEnabled = false;
                    pauseResumeButton.setText(getResources().getString(R.string
                            .activity_fragment_pause));

                    // Increase the duration where the user paused the Activity
                    durationPausedInMilliseconds += System.currentTimeMillis() - dateOnPaused;
                    locationListener.setLastCoordinateIsPause(true);
                    startLocationUpdates();
                }
            }
        });

        // Initialize
        durationPausedInMilliseconds = 0;

        // Set up the Listener for the FusedLocationApi
        connectionCallbacks = new ConnectionServices(this);
        onConnectionFailedListener = new ConnectionFailed(getContext());

        setUpdateIntervalsAndDisplacement();

        buildGoogleApiClient();
        return v;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMapType();
        startStopButton.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new MyLocationButtonListener(getContext()));
        map.getUiSettings().setMapToolbarEnabled(false);
    }

    public void setMapType() {
        if (map != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            int value = Integer.valueOf(sp.getString("type", "1"));
            int type;
            switch (value) {
                case 0:
                    type = GoogleMap.MAP_TYPE_NORMAL;
                    break;
                case 1:
                    type = GoogleMap.MAP_TYPE_HYBRID;
                    break;
                case 2:
                    type = GoogleMap.MAP_TYPE_SATELLITE;
                    break;
                case 3:
                    type = GoogleMap.MAP_TYPE_TERRAIN;
                    break;
                case 4:
                    type = GoogleMap.MAP_TYPE_NONE;
                    break;
                default:
                    type = GoogleMap.MAP_TYPE_NORMAL;
                    break;
            }
            map.setMapType(type);
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION
     * These settings control the accuracy of the current location. This sample uses
     * ACCESS_FINE_LOCATION, as defined in the AndroidManifest.xml.
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet
     */
    protected void createLocationRequest() {
        locationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.setInterval(1000);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.setFastestInterval(1000);

        // Sets the minimum displacement between location updates in meters.
        // If the displacement is too small the updates will be suppressed
        locationRequest.setSmallestDisplacement(smallestDisplacementInMeter);

        // Accuracy must be high for tracking a route
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi
     */
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        Log.d("####", "--- started 1 ---");
        // LocationListener is fired every x seconds
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, locationListener);
        Log.d("####", "--- started 2 ---");
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
    }

    /**
     * Sets up the two intervals and the displacement for the LocationRequest
     */
    private void setUpdateIntervalsAndDisplacement() {
        // ???
        // Never get more updates than this interval?
        fastestUpdateIntervalInMilliseconds = getIntervalFromSettingsInMilliseconds();
        // ???
        updateIntervalInMilliseconds = fastestUpdateIntervalInMilliseconds *
                FACTOR_BETWEEN_INTERVALS;
        // Example: If the user wants location updates every 10 seconds, he gets them only if
        // he moves at least 2,5 meters in 10 seconds
        smallestDisplacementInMeter = fastestUpdateIntervalInMilliseconds / 1000 *
                FACTOR_DISPLACEMENT;
    }

    /**
     * Gets the interval from Settings in milliseconds
     *
     * @return
     */
    private int getIntervalFromSettingsInMilliseconds() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return Integer.valueOf(sp.getString("interval", "1")) * 1000;

    }

    /**
     * Called when the Activity starts
     */
    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /**
     * Called when user resumes to display the Activity
     */
    @Override
    public void onResume() {
        super.onResume();
        // Set again the type of the map because he could have changed
        setMapType();
        // Set the interval of the location requests to one second if the MainActivity
        // (and thus the map) is visible.
        // This is because setMyLocationEnabled() is true and the blue point updates every second,
        // then we should draw every second a Polyline into the map
        if (googleApiClient.isConnected() && locationRequest != null && startButtonEnabled &&
                !pauseButtonEnabled) {
            // Stop the LocationUpdates to modify the interval
            stopLocationUpdates();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            // Apply the changes on the interval
            startLocationUpdates();
        }
        if (notificationManager != null) {
            notificationManager.cancel(notifyID);
        }
        setUpdateIntervalsAndDisplacement();
    }

    /**
     * Called when user doesn't see the Activity
     */
    @Override
    public void onPause() {
        super.onPause();
        // Set the interval of the location requests to the set value if the MainActivity
        // (and thus the map) is not visible, for example when the user locks his device
        if (googleApiClient.isConnected() && locationRequest != null && startButtonEnabled) {
            if (getIntervalFromSettingsInMilliseconds() == 0) {
                if (scheduledExecutorService == null) {
                    Log.d("####", "--- new scheduler ---");
                    scheduledExecutorService = Executors
                            .newSingleThreadScheduledExecutor();
                    scheduledExecutorService.scheduleAtFixedRate(new IntervalUpdater
                            (locationListener, this)
                            , 1, 60, TimeUnit.SECONDS);
                }
            } else {
                // Stop the LocationUpdates to modify the interval
                stopLocationUpdates();
                locationRequest.setInterval(updateIntervalInMilliseconds);
                locationRequest.setFastestInterval(fastestUpdateIntervalInMilliseconds);
                // Apply the changes on the interval
                startLocationUpdates();
            }
        }

        if (startButtonEnabled && !pauseButtonEnabled) {
            makeNotification(GOING_ON);
        } else if (startButtonEnabled && pauseButtonEnabled) {
            makeNotification(PAUSE);
        }
        getActivity().startService(new Intent(getActivity(), CloseService.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        map.clear();
        startStopButton.setText(getResources().getString(R.string.activity_fragment_start));
        pauseResumeButton.setVisibility(View.GONE);
    }

    private void start() {
        startButtonEnabled = true;
        startStopButton.setText(getResources().getString(R.string
                .activity_fragment_stop));
        pauseResumeButton.setVisibility(View.VISIBLE);
        // Get a new LocationListener
        locationListener = new LocationListener(map, getContext());
        // Start location updates
        startLocationUpdates();
    }

    private Location getActualPosition() {
        Location ret = null;
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
                    .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(getActivity(), Manifest.permission
                            .ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            ret = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
        }
        return ret;
    }

    private void makeNotification(int type) {
        notificationBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(getActivity(), 0,
                        new Intent(getActivity(), MainActivity.class), PendingIntent
                                .FLAG_UPDATE_CURRENT));

        switch (type) {
            case GOING_ON:
                notificationBuilder.setContentText(getResources().getString(R.string
                        .activity_fragment_notification_going_on));
                break;
            case PAUSE:
                notificationBuilder.setContentText(getResources().getString(R.string
                        .activity_fragment_notification_pause));
                break;
        }

        notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyID, notificationBuilder.build());
    }

    public Boolean getStartButtonEnabled() {
        return startButtonEnabled;
    }

    public void setStartButtonEnabled(Boolean startButtonEnabled) {
        this.startButtonEnabled = startButtonEnabled;
    }

    public Boolean getPauseButtonEnabled() {
        return pauseButtonEnabled;
    }

    public LocationRequest getLocationRequest() {
        return locationRequest;
    }
}