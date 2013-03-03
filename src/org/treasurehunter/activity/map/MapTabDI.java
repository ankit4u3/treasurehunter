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
import com.google.android.maps.Overlay;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.GraphicsGenerator;
import org.treasurehunter.R;
import org.treasurehunter.Toaster;
import org.treasurehunter.activity.cachelist.DistanceFormatter;
import org.treasurehunter.activity.filterlist.FilterTypeCollection;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuActionAbout;
import org.treasurehunter.menuactions.MenuActionEditFilter;
import org.treasurehunter.menuactions.MenuActionEnableGPS;
import org.treasurehunter.menuactions.MenuActionFilterListPopup;
import org.treasurehunter.menuactions.MenuActions;
import org.treasurehunter.task.DelayingTaskRunner;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MapTabDI {
    public static Context mContext;

    private static class NullOverlay extends Overlay {
    }

    public static DensityOverlayDelegate createDensityOverlayDelegate() {
        final Rect patchRect = new Rect();
        final Paint paint = new Paint();
        paint.setARGB(128, 255, 0, 0);
        final Point screenLow = new Point();
        final Point screenHigh = new Point();
        return new DensityOverlayDelegate(patchRect, paint, screenLow, screenHigh);
    }

    public static MapTab create(final Activity activity, GuiState guiState,
            GeocacheFactory geocacheFactory, DbFrontend dbFrontend,
            FilterTypeCollection filterTypeCollection, GeoFixProvider geoFixProvider,
            BcachingConfig bcachingConfig, SharedPreferences sharedPreferences) {
        // Set member variables first, in case anyone after this needs them.
        final LayoutInflater layoutInflater = activity.getLayoutInflater();
        View layout = layoutInflater.inflate(R.layout.map, null);
        GeoMapView mapView = (GeoMapView)layout.findViewById(R.id.mapview);
        FixedMyLocationOverlay myLocationOverlay = new FixedMyLocationOverlay(activity, mapView,
                geoFixProvider, sharedPreferences);

        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);

        GeocacheFilter cacheFilter = guiState.getActiveFilter();
        final Resources resources = activity.getResources();
        final Drawable defaultMarker = resources.getDrawable(R.drawable.pin_default);
        final GraphicsGenerator graphicsGenerator = new GraphicsGenerator(resources);

        final DistanceFormatter formatter = new DistanceFormatter(sharedPreferences);
        final DistanceOverlay distanceOverlay = new DistanceOverlay(geoFixProvider, formatter);

        final Overlay nullOverlay = new MapTabDI.NullOverlay();
        mapView.init(nullOverlay, myLocationOverlay, distanceOverlay);

        final CachesProviderDb cachesProviderArea = new CachesProviderDb(dbFrontend, true);
        cachesProviderArea.setFilter(cacheFilter);
        final DensityOverlayDelegate densityOverlayDelegate = createDensityOverlayDelegate();
        final DensityOverlay densityOverlay = new DensityOverlay(densityOverlayDelegate);

        CachesProviderDb cachesProviderAreaPins = new CachesProviderDb(dbFrontend, true);
        final Toaster tooManyCachesToaster = new Toaster(activity, R.string.too_many_caches, false);
        final Toaster densityViewToaster = new Toaster(activity, R.string.density_view_switch,
                false);
        final CacheHint cacheHint = new CacheHint(mapView);
        CachePinsOverlayFactory overlayFactory = new CachePinsOverlayFactory(graphicsGenerator,
                dbFrontend, defaultMarker, cacheHint, guiState);
        DelayingTaskRunner delayingTaskRunner = new DelayingTaskRunner();
        OverlayManager overlayManager = new OverlayManager(mapView, densityOverlay, false,
                cachesProviderArea, cacheFilter, tooManyCachesToaster, densityViewToaster,
                overlayFactory, cacheHint, delayingTaskRunner, distanceOverlay);
        mapView.setScrollListener(overlayManager);

        final OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        };
        final ErrorDisplayer errorDisplayer = new ErrorDisplayer(activity, mOnClickListener);
        final List<CachesProviderDb> providers = new ArrayList<CachesProviderDb>();
        providers.add(cachesProviderArea);
        providers.add(cachesProviderAreaPins);

        final PowerManager powerManager = (PowerManager)activity
                .getSystemService(Context.POWER_SERVICE);
        final WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "Importing");

        MenuActionGetAreaBcaching syncArea = new MenuActionGetAreaBcaching(activity, resources,
                dbFrontend, errorDisplayer, geocacheFactory, mapView, guiState, wakeLock,
                bcachingConfig, delayingTaskRunner);

        // *** BUILD MENU ***
        final MenuActions menuActions = new MenuActions();
        menuActions.add(new MenuActionToggleSatellite(mapView));
        MapController mapController = mapView.getController();
        menuActions.add(new MenuActionCenterLocation(resources, mapController, myLocationOverlay));
        menuActions.add(syncArea);
        menuActions.add(new MenuActionEditFilter(activity, guiState, resources));
        menuActions.add(new MenuActionFilterListPopup(activity, filterTypeCollection, resources,
                guiState));
        menuActions.add(new MenuActionAbout(activity));
        menuActions.add(new MenuActionEnableGPS(activity, resources));
        menuActions.add(new MenuActionToggleDistanceOverlay(distanceOverlay, mapView,
                sharedPreferences));
        Drawable icon = resources.getDrawable(R.drawable.ic_menu_mapmode);

        return new MapTab(guiState, menuActions, myLocationOverlay, overlayManager, mapController,
                layout, icon, mapView, cacheHint, delayingTaskRunner, geoFixProvider,
                sharedPreferences, distanceOverlay);
    }

}
