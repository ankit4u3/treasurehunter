
package org.treasurehunter.menuactions;

import org.treasurehunter.GeoFix;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.R;
import org.treasurehunter.activity.map.MapTabDI;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;

/** Show the map, centered around the current location */
public class MenuActionMap extends StaticLabelMenu implements MenuAction {
    private final GeoFixProvider mLocationControl;

    private final Activity mActivity;

    public MenuActionMap(Activity activity, GeoFixProvider locationControl, Resources resources) {
        super(resources, R.string.menu_map);
        mActivity = activity;
        mLocationControl = locationControl;
    }

    @Override
    public void act() {
        GeoFix location = mLocationControl.getLocation();

        final Intent intent = new Intent(mActivity, MapTabDI.class);
        intent.putExtra("latitude", (float)location.getLatitude());
        intent.putExtra("longitude", (float)location.getLongitude());

        mActivity.startActivity(intent);

    };

    @Override
    public boolean isEnabled() {
        return true;
    }
}
