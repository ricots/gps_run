package com.acer.run_gps;

import com.acer.run_gps.model.Activity;
import com.acer.run_gps.model.Coordinate;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by Fabian on 29.12.2015.
 */
public class FormatStrings extends TestCase {

    private Activity a = null;

    public void setUp() throws Exception {
        super.setUp();
        a = new Activity();
        a.setDate(1447115888179l);

        Coordinate c1 = new Coordinate();
        c1.setDistanceFromPrevious(0);
        Coordinate c2 = new Coordinate();
        c2.setDistanceFromPrevious(11);
        Coordinate c3 = new Coordinate();
        c3.setDistanceFromPrevious(14);
        Coordinate c4 = new Coordinate();
        c4.setDistanceFromPrevious(9);

        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(c1);
        coordinates.add(c2);
        coordinates.add(c3);
        coordinates.add(c4);

        a.setCoordinates(coordinates);
    }

    @Test
    public void testDistanceKm() throws Exception {
        assertEquals("34 m", a.getFormattedDistance("km"));
    }

    @Test
    public void testDistanceLongKm() throws Exception {
        Coordinate c1 = new Coordinate();
        c1.setDistanceFromPrevious(0);
        Coordinate c2 = new Coordinate();
        c2.setDistanceFromPrevious(11);
        Coordinate c3 = new Coordinate();
        c3.setDistanceFromPrevious(14);
        Coordinate c4 = new Coordinate();
        c4.setDistanceFromPrevious(9000);

        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(c1);
        coordinates.add(c2);
        coordinates.add(c3);
        coordinates.add(c4);

        a.setCoordinates(coordinates);
        assertEquals("9.03 km", a.getFormattedDistance("km"));
    }

    @Test
    public void testDistanceMi() throws Exception {
        assertEquals("0.02 mi", a.getFormattedDistance("mi"));
    }

    @Test
    public void testAvgInKm() throws Exception {
        a.setDuration(660);
        assertEquals("0.19 km/h", a.getFormattedAvg("km"));
    }

    @Test
    public void testAvgInMi() throws Exception {
        a.setDuration(660);
        assertEquals("0.12 mph", a.getFormattedAvg("mi"));
    }

    @Test
    public void testDuration0Sec() throws Exception {
        a.setDuration(0);
        assertEquals("0sec", a.getFormattedDuration());
    }

    @Test
    public void testDurationMin() throws Exception {
        a.setDuration(660);
        assertEquals("11min", a.getFormattedDuration());
    }

    @Test
    public void testDurationHour() throws Exception {
        a.setDuration(4525);
        assertEquals("1h, 15min, 25sec", a.getFormattedDuration());
    }

    @Test
    public void testDurationDay() throws Exception {
        a.setDuration(90999);
        assertEquals("1d, 1h, 16min, 39sec", a.getFormattedDuration());
    }

    @Test
    public void testDateWithPoints() throws Exception {
        assertEquals("10.11.2015", a.getFormattedDate("dd.MM.yyyy"));
    }

    @Test
    public void testWithHyphen() throws Exception {
        assertEquals("11-10-2015", a.getFormattedDate("MM-dd-yyyy"));
    }
}