
package org.treasurehunter.activity.map;

import com.google.android.maps.MapView;

import org.treasurehunter.R;
import org.treasurehunter.menuactions.MenuAction;

import android.content.SharedPreferences;

public class MenuActionToggleDistanceOverlay implements MenuAction {
    private static final String PREFERENCE_KEY = "map_view_show_distance_overlay";

    private final DistanceOverlay mDistanceOverlay;

    private final MapView mMapView;

    private final SharedPreferences mPreferences;

    public MenuActionToggleDistanceOverlay(DistanceOverlay distanceOverlay, MapView mapView,
            SharedPreferences sharedPreferences) {
        mDistanceOverlay = distanceOverlay;
        mMapView = mapView;
        mPreferences = sharedPreferences;
        mDistanceOverlay.setActive(sharedPreferences.getBoolean(PREFERENCE_KEY, true));
    }

    @Override
    public void act() {
        boolean newState = !mDistanceOverlay.isActive();
        mDistanceOverlay.setActive(newState);
        mMapView.invalidate();
        mPreferences.edit().putBoolean(PREFERENCE_KEY, newState).commit();
    }

    @Override
    public String getLabel() {
        int stringId = !mDistanceOverlay.isActive() ? R.string.map_menu_toggle_distanceview_on
                : R.string.map_menu_toggle_distanceview_off;
        return mMapView.getResources().getString(stringId);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
