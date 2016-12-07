package com.acer.run_gps;

import com.acer.run_gps.model.Activity;

import junit.framework.TestCase;

/**
 * Created by Fabian on 31.12.2015.
 */
public class Duration extends TestCase {

    private Activity activity = null;

    public void setUp() throws Exception {
        activity = new Activity();
        activity.setDate(System.currentTimeMillis());
    }

    public void testDuration() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(5, (int) ((System.currentTimeMillis() - activity.getDate()) / 1000));
    }
}