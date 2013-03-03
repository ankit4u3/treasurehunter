
package org.treasurehunter;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * this service handles real time and scheduled track recording as well as
 * compass updates
 */
public class AppService extends Service {

    private static boolean running = false;

    // private App app;

    private LocationManager locationManager;

    /**
     * current device location
     */
    private Location currentLocation;

    private Location lastRecordedLocation = null;

    /**
     * is GPS in use?
     */
    private boolean gpsInUse;

    /**
     * listening for location updates flag
     */
    private boolean listening;

    /**
     * sets to true once first location update received
     */
    private boolean schedulerListening;

    /**
     * listening getter
     */
    public boolean isListening() {
        return listening;
    }

    // private TrackRecorder trackRecorder;

    // private ScheduledTrackRecorder scheduledTrackRecorder;

    /**
     * gpsInUse setter
     */
    public void setGpsInUse(boolean gpsInUse) {
        this.gpsInUse = gpsInUse;
    }

    public boolean isGpsInUse() {
        return this.gpsInUse;
    }

    /**
     * Defines a listener that responds to location updates
     */
    private LocationListener locationListener = new LocationListener() {

        /**
         * Called when a new location is found by the network location provider.
         */
        @Override
        public void onLocationChanged(Location location) {

            listening = true;

            currentLocation = location;
            Log.d("Location Received",
                    currentLocation.getLatitude() + " " + currentLocation.getLongitude());

        }

        /**
         * Called when the provider status changes. This method is called when a
         * provider is unable to fetch a location or if the provider has
         * recently become available after a period of unavailability.
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                listening = false;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    /**
     * Defines a listener that responds to location updates
     */
    private LocationListener scheduledLocationListener = new LocationListener() {

        // Called when a new location is found by the network location provider.
        @Override
        public void onLocationChanged(Location location) {

            Log.i(Constants.TAG, "scheduledLocationListener: " + location.getAccuracy());

            // first location update received
            schedulerListening = true;

            currentLocation = location;
            currentLocation = location;
            Log.d("Location Received Scheduled", currentLocation.getLatitude() + " "
                    + currentLocation.getLongitude());
            // app.setCurrentLocation(location);

            // check minimum accuracy required for recording
            if (location.hasAccuracy() && location.getAccuracy() <= 50) {

                float distance = 0;

                // scheduledTrackRecorder.recordTrackPoint(location, distance);

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    /**
     * Broadcasting location update
     */

    // //////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This is the object that receives interactions from clients
     */
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {

        Log.d(Constants.TAG, "AppService: BOUND " + this.toString());

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.d(Constants.TAG, "AppService: UNBOUND " + this.toString());

        return true;
    }

    public class LocalBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize service
     */
    @Override
    public void onCreate() {

        super.onCreate();

        Log.i(Constants.TAG, "AppService: onCreate");

        // location sensor
        // first time we call startLocationUpdates from MainActivity
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        AppService.running = true;

        this.requestLastKnownLocation();

    }

    /**
     * Service destructor
     */
    @Override
    public void onDestroy() {

        Log.i(Constants.TAG, "AppService: onDestroy");

        AppService.running = false;

        // stop listener without delay
        this.locationManager.removeUpdates(locationListener);

        this.locationManager = null;

        super.onDestroy();

    }

    /**
     * is service running?
     */
    public static boolean isRunning() {
        return running;
    }

    /**
     * Requesting last location from GPS or Network provider
     */
    public void requestLastKnownLocation() {

        Location location;
        int locationProvider;

        // get last known location from gps provider
        location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            locationProvider = Constants.GPS_PROVIDER_LAST;

        } else {
            // let's try network provider
            location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }

        if (location != null) {
            //

        }

        currentLocation = location;

        Log.d("Location Received inside CUrrent location()", currentLocation.getLatitude() + " "
                + currentLocation.getLongitude());
        // this.app.setCurrentLocation(location);

    }

    /**
	 * 
	 */
    public void startLocationUpdates() {

        this.listening = false;

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                locationListener);

        // setting gpsInUse to true, but listening is still false at this point
        // listening is set to true with first location update in
        // LocationListener.onLocationChanged
        gpsInUse = true;
    }

    /**
     * Stopping location updates with delay, leaving a chance for new activity
     * not to restart location listener
     */
    public void stopLocationUpdates() {

        gpsInUse = false;

        (new stopLocationUpdatesThread()).start();

    }

    /**
	 * 
	 */
    public void stopLocationUpdatesNow() {

        locationManager.removeUpdates(locationListener);

        listening = false;

        gpsInUse = false;

    }

    /**
     * start waiting for scheduler location updates
     */
    public void startScheduledLocationUpdates() {

        // app.logd("AppService.startScheduledLocationUpdates");

        this.schedulerListening = false;

        // control the time of location request before any updates received
        this.scheduleNextRequestTimeLimitCheck();

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                scheduledLocationListener);
    }

    /**
     * stop scheduler location updates
     */
    public void stopScheduledLocationUpdates() {
        this.locationManager.removeUpdates(scheduledLocationListener);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
	 * 
	 */
    private PendingIntent nextLocationRequestSender;

    /**
	 * 
	 */
    private void scheduleNextLocationRequest(int interval) {

        // app.logd("AppService.scheduleNextLocationRequest interval:" +
        // interval);

        Intent intent = new Intent(Constants.ACTION_NEXT_LOCATION_REQUEST);
        nextLocationRequestSender = PendingIntent.getBroadcast(AppService.this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, interval);

        // schedule single alarm
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                nextLocationRequestSender);

    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
	 * 
	 */
    private PendingIntent nextTimeLimitCheckSender;

    /**
	 * 
	 */
    private void scheduleNextRequestTimeLimitCheck() {

        // app.logd("AppService.scheduleNextRequestTimeLimitCheck");

        Intent intent = new Intent(Constants.ACTION_NEXT_TIME_LIMIT_CHECK);
        nextTimeLimitCheckSender = PendingIntent.getBroadcast(AppService.this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);

        // schedule single alarm
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                nextTimeLimitCheckSender);
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    /**
     * stopping location updates with small delay giving us a chance not to
     * restart listener if other activity requires GPS sensor too. new activity
     * has to bind to AppService and set gpsInUse to true
     */
    private class stopLocationUpdatesThread extends Thread {

        @Override
        public void run() {

            try {
                // wait for other activities to grab location updates
                sleep(2500);
            } catch (Exception e) {
            }

            // if no activities require location updates - stop them and save
            // battery
            if (gpsInUse == false) {
                locationManager.removeUpdates(locationListener);
                listening = false;
            }

        }
    }

}
