package com.acer.run_gps.gui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.acer.run_gps.gui.fragments.ActivityFragment;
import com.acer.run_gps.gui.fragments.TracksFragment;

/**
 * Created by Fabian on 28.12.2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int NUMBER_TABS = 2;
    private CharSequence titles[];

    private ActivityFragment activityFragment = null;

    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[]) {
        super(fm);
        this.titles = titles;
    }

    // Return a fragment object for the tabs
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            // First Tab (Activity)
            ActivityFragment activityFragment = new ActivityFragment();
            this.activityFragment = activityFragment;
            return activityFragment;
        } else {
            // Second Tab (Tracks)
            TracksFragment tracksFragment = new TracksFragment();
            return tracksFragment;
        }
    }

    // Return the title for the tab at a specific position
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    // Return the count of tabs
    @Override
    public int getCount() {
        return NUMBER_TABS;
    }

    public ActivityFragment getActivityFragment() {
        return activityFragment;
    }

    public void setActivityFragment(ActivityFragment activityFragment) {
        this.activityFragment = activityFragment;
    }
}