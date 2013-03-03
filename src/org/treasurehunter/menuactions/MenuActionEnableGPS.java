
package org.treasurehunter.menuactions;

import org.treasurehunter.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.LocationManager;
import android.provider.Settings;

public class MenuActionEnableGPS implements MenuAction {
    private final Activity mActivity;

    private final String mTurnOffLabel;

    private final String mTurnOnLabel;

    public MenuActionEnableGPS(Activity activity, Resources resources) {
        mTurnOffLabel = resources.getString(R.string.turn_off_gps);
        mTurnOnLabel = resources.getString(R.string.turn_on_gps);
        mActivity = activity;
    }

    @Override
    public void act() {
        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mActivity.startActivity(myIntent);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getLabel() {
        LocationManager locationManager = (LocationManager)mActivity
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        if (gpsEnabled) {
            return mTurnOffLabel;
        } else {
            return mTurnOnLabel;
        }
    }
}
