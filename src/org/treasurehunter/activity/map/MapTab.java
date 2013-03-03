/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package org.treasurehunter.activity.map;

import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;

import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.activity.main.TabBase;
import org.treasurehunter.menuactions.MenuActions;
import org.treasurehunter.task.ITaskRunner;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public class MapTab extends TabBase {

    private static final int DEFAULT_ZOOM_LEVEL = 14;

    private final GuiState mGuiState;

    private final MyLocationOverlay mMyLocationOverlay;

    private final OverlayManager mOverlayManager;

    private final MapController mMapController;

    private final GeoMapView mGeoMapView;

    private final CacheHint mCacheHint;

    private final ITaskRunner mTaskRunner;

    private final GeoFixProvider mGeoFixProvider;

    private final DistanceOverlay mDistanceOverlay;

    public MapTab(GuiState guiState, MenuActions menuActions, MyLocationOverlay myLocationOverlay,
            OverlayManager overlayManager, MapController mapController, View contentView,
            Drawable icon, GeoMapView geoMapView,

            CacheHint cacheHintHolder, ITaskRunner taskRunner, GeoFixProvider geoFixProvider,
            SharedPreferences sharedPreferences, DistanceOverlay distanceOverlay) {
        super("tab_map", getIconLabel(sharedPreferences), contentView, icon, menuActions);
        mGuiState = guiState;
        mMyLocationOverlay = myLocationOverlay;
        mOverlayManager = overlayManager;
        mMapController = mapController;
        mGeoMapView = geoMapView;
        mCacheHint = cacheHintHolder;
        mTaskRunner = taskRunner;
        mGeoFixProvider = geoFixProvider;
        mDistanceOverlay = distanceOverlay;
    }

    private static String getIconLabel(SharedPreferences preferences) {
        return preferences.getBoolean("ui_show_tab_texts", true) ? "Map" : "";
    }

    @Override
    public void onPause() {
        mGeoMapView.setVisibility(View.INVISIBLE);
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();

        if (mMyLocationOverlay instanceof FixedMyLocationOverlay)
            mGeoFixProvider.removeObserver((FixedMyLocationOverlay)mMyLocationOverlay);
        mTaskRunner.abort();

    }

    @Override
    public void onCreate() {
        mMapController.setZoom(DEFAULT_ZOOM_LEVEL);
        mOverlayManager.setCacheFilter(mGuiState.getActiveFilter());
        mOverlayManager.initArea();
    }

    @Override
    public void onResume() {
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableCompass();
        mGeoMapView.setVisibility(View.VISIBLE);
        if (mMyLocationOverlay instanceof FixedMyLocationOverlay)
            mGeoFixProvider.addObserver((FixedMyLocationOverlay)mMyLocationOverlay);

        Geocache prevSelected = mOverlayManager.getSelectedGeocache();
        Geocache selected = mGuiState.getActiveGeocache();
        Waypoint prevWaypoint = mOverlayManager.getSelectedWaypoint();
        Waypoint selectedWaypoint = mGuiState.getActiveWaypoint();
        if (prevSelected != selected) {
            mCacheHint.showGeoObject(selected);
            if (selected != null) {
                // Log.d("TreasureHunter", "Setting selected to " +
                // selected.getId());
                mMapController.animateTo(selected.getGeoPoint());
            }
            mOverlayManager.setSelectedGeocache(selected);
        } else {
            if (prevWaypoint != selectedWaypoint) {
                mCacheHint.showGeoObject(selectedWaypoint);
                if (selectedWaypoint != null) {
                    Log.d("TreasureHunter",
                            "Setting selected Waypoint to " + selectedWaypoint.getId());
                    mMapController.animateTo(selectedWaypoint.getGeoPoint());
                }
                mOverlayManager.setSelectedWaypoint(selectedWaypoint, selected);
            }
        }

        mOverlayManager.refreshIfNeeded();
        mGeoMapView.updateIfMoved();
        mDistanceOverlay.updatePreferences();
    }

    @Override
    public void onDataViewChanged(GeocacheFilter filter, boolean isTabActive) {
        mOverlayManager.databaseChanged();
        mOverlayManager.setCacheFilter(filter);
        mOverlayManager.invalidate();
        if (isTabActive) {
            mOverlayManager.refreshIfNeeded();
        }
    }

    public DistanceOverlay getDistanceOverlay() {
        return mDistanceOverlay;
    }
}
