package com.acer.run_gps;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ApplicationTestCase;

/**
 * Created by Fabian on 28.12.2015.
 */
public class SettingsTest extends ApplicationTestCase<Application> {

    private SharedPreferences sp = null;

    public SettingsTest() {
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public void testUnit() {
        assertEquals("km", sp.getString("unit", "km"));
    }

    public void testDateEU() {
        assertEquals("dd.MM.yyyy HH:mm:ss", sp.getString("date", "dd.MM.yyyy HH:mm:ss"));
    }

    public void testInterval() {
        // Get a string because ListPreference cannot support int values
        int unit = Integer.valueOf(sp.getString("interval", "0"));
        assertEquals(5, unit);
    }

    public void testType() {
        // Get a string because ListPreference cannot support int values
        int unit = Integer.valueOf(sp.getString("type", "0"));
        assertEquals(0, unit);
    }
}