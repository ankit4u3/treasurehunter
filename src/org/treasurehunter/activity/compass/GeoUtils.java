/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treasurehunter.activity.compass;

/**
 * Library for some use useful latitude/longitude math
 */
public class GeoUtils {
    private static int EARTH_RADIUS_KM = 6371;

    public static int MILLION = 1000000;

    // Constants for distance calculation
    public static final int UNIT_METERS = 0;

    public static final int UNIT_KILOMETERS = 1;

    public static final int UNIT_FEET = 2;

    public static final int UNIT_YARDS = 3;

    public static final int UNIT_MILES = 4;

    public static final int UNIT_NAUTICAL_MILES = 5;

    public static final int UNIT_FURLONGS = 6;

    public static final int UNIT_LEAGUES = 7;

    public static final double FEET_IN_METERS = 0.30480; // http://en.wikipedia.org/wiki/Foot_(length)

    public static final double YARDS_IN_METERS = 0.91440; // http://en.wikipedia.org/wiki/Yard

    public static final double MILES_IN_KILOMETERS = 1.609344; // http://en.wikipedia.org/wiki/Mile

    public static final double NAUTICAL_MILES_IN_KILOMETERS = 1.852; // http://en.wikipedia.org/wiki/Nautical_mile

    public static final double FURLONGS_IN_KILOMETERS = MILES_IN_KILOMETERS / 8; // http://en.wikipedia.org/wiki/Furlong

    public static final double LEAGUES_IN_KILOMETERS = MILES_IN_KILOMETERS * 3; // http://en.wikipedia.org/wiki/League_(unit)

    /**
     * Converts a distance to kilometers.
     * 
     * @param distance the distance to convert
     * @param unit constant indicating the unit of the distance
     * @return Converted distance in kilometers.
     */
    public static double convertToKm(double distance, int unit) {

        switch (unit) {

            case UNIT_METERS:
                return distance / 1000;
            case UNIT_KILOMETERS:
                return distance;
            case UNIT_FEET:
                return (distance * FEET_IN_METERS) / 1000;
            case UNIT_YARDS:
                return (distance * YARDS_IN_METERS) / 1000;
            case UNIT_MILES:
                return distance * MILES_IN_KILOMETERS;
            case UNIT_NAUTICAL_MILES:
                return distance * NAUTICAL_MILES_IN_KILOMETERS;
            case UNIT_FURLONGS:
                return distance * FURLONGS_IN_KILOMETERS;
            case UNIT_LEAGUES:
                return distance * LEAGUES_IN_KILOMETERS;

        }

        return 0; // Error state for undefined units

    }

    /**
     * Computes the distance in kilometers between two points on Earth.
     * 
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return Distance between the two points in kilometers.
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad)
                * Math.cos(lat2Rad) * Math.cos(deltaLonRad))
                * EARTH_RADIUS_KM;
    }

    /**
     * Computes the distance in kilometers between two points on Earth.
     * 
     * @param p1 First point
     * @param p2 Second point
     * @return Distance between the two points in kilometers.
     */
    //
    // public static double distanceKm(GeoPoint p1, GeoPoint p2) {
    // double lat1 = p1.getLatitudeE6() / (double)MILLION;
    // double lon1 = p1.getLongitudeE6() / (double)MILLION;
    // double lat2 = p2.getLatitudeE6() / (double)MILLION;
    // double lon2 = p2.getLongitudeE6() / (double)MILLION;
    //
    // return distanceKm(lat1, lon1, lat2, lon2);
    // }
    /**
     * Computes the bearing in degrees between two points on Earth.
     * 
     * @param p1 First point
     * @param p2 Second point
     * @return Bearing between the two points in degrees. A value of 0 means due
     *         north.
     */
    // public static double bearing(GeoPoint p1, GeoPoint p2) {
    // double lat1 = p1.getLatitudeE6() / (double) MILLION;
    // double lon1 = p1.getLongitudeE6() / (double) MILLION;
    // double lat2 = p2.getLatitudeE6() / (double) MILLION;
    // double lon2 = p2.getLongitudeE6() / (double) MILLION;
    //
    // return bearing(lat1, lon1, lat2, lon2);
    // }
    /**
     * Computes the bearing in degrees between two points on Earth.
     * 
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return Bearing between the two points in degrees. A value of 0 means due
     *         north.
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)
                * Math.cos(deltaLonRad);
        return radToBearing(Math.atan2(y, x));
    }

    /**
     * Waypoint projection using haversine formula
     * http://en.wikipedia.org/wiki/Haversine_formula See discussion here for
     * further information: http://www.movable-type.co.uk/scripts/latlong.html
     */
    public static double[] project(double distance, double bearing, double startLat, double startLon) {

        double distanceRad = distance / EARTH_RADIUS_KM;
        double bearingRad = Math.toRadians(bearing);
        double startLatRad = Math.toRadians(startLat);
        double startLonRad = Math.toRadians(startLon);

        double endLat = Math.asin(Math.sin(startLatRad) * Math.cos(distanceRad)
                + Math.cos(startLatRad) * Math.sin(distanceRad) * Math.cos(bearingRad));

        double endLon = startLonRad
                + Math.atan2(Math.sin(bearingRad) * Math.sin(distanceRad) * Math.cos(startLatRad),
                        Math.cos(distanceRad) - Math.sin(startLatRad) * Math.sin(endLat));

        // Adjust projections crossing the 180th meridian:
        double endLonDeg = Math.toDegrees(endLon);

        if (endLonDeg > 180 || endLonDeg < -180) {
            endLonDeg = endLonDeg % 360; // Just in case we circle the earth
                                         // more than once.
            if (endLonDeg > 180) {
                endLonDeg = endLonDeg - 360;
            } else if (endLonDeg < -180) {
                endLonDeg = endLonDeg + 360;
            }
        }

        double[] endCoords = new double[] {
                Math.toDegrees(endLat), endLonDeg
        };

        return endCoords;

    }

    /**
     * Converts an angle in radians to degrees
     */
    public static double radToBearing(double rad) {
        return (Math.toDegrees(rad) + 360) % 360;
    }
}
