package com.acer.run_gps.gui.activities;


import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.acer.run_gps.R;
import com.acer.run_gps.gui.adapter.ViewPagerAdapter;
import com.acer.run_gps.gui.fragments.ActivityFragment;
import com.acer.run_gps.gui.message.ToastFactory;
import com.acer.run_gps.gui.tabs.SlidingTabLayout;


/**
 * Created by Fabian on 28.12.2015.
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 0;

    // UI Widgets
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private SlidingTabLayout slidingTabLayout;

    // Titles for the tabs
    private CharSequence titles[] = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            buildNormalLayout();
        }
    }

    // Build the menu in the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Called when an Item in the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                ret = true;
                break;
            case R.id.menu_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                ret = true;
                break;
            default:
                ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }

    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check if we should show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.dialog_title))
                        .setMessage(getResources().getString(R.string.dialog_message))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            } else {
                // No explanation needed, let's request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[]
            grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildNormalLayout();
                } else {
                    setContentView(R.layout.request_permission_activity);
                    Button request = (Button) findViewById(R.id.requestPermission);
                    request.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_CODE_ASK_PERMISSIONS);
                        }
                    });
                }
                return;
            }
        }
    }

    public void buildNormalLayout() {
        setContentView(R.layout.main_activity);
        // Set color of the tabs
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Titles of the tabs
        titles = this.getResources().getStringArray(R.array.tabs_titles);

        // Use a Toolbar instead of ActionBar (only in this activity)
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // Set the adapter class for the ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        // Get the SlidingTabLayoutView
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);

        // Create the FragmentStatePagerAdapter
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles);

        // Set the adapter
        viewPager.setAdapter(viewPagerAdapter);

        slidingTabLayout.setDistributeEvenly(true);
        // Set the ViewPager for the SlidingTabLayout
        slidingTabLayout.setViewPager(viewPager);

        // Set color for the line under the tabs
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getApplication(), R.color.colorTabUnderline);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService
                (Context
                        .NOTIFICATION_SERVICE);
        notificationManager.cancel(ActivityFragment.notifyID);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean ret = false;
        if (!viewPagerAdapter.getActivityFragment().getStartButtonEnabled()) {
            ret = super.onKeyDown(keyCode, event);
        } else {
            ToastFactory.makeToast(this, getResources().getString(R.string.toast_shutdown));
        }
        return ret;
    }
}