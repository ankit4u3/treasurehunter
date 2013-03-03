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

package org.treasurehunter.activity.compass;

import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheFactory.Provider;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.R;
import org.treasurehunter.Refresher;
import org.treasurehunter.Source;
import org.treasurehunter.Tags;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.activity.main.TabBase;
import org.treasurehunter.database.DatabaseLocator;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuActions;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

/** The tab that shows a compass arrow pointing towards the active geocache */
public class CompassTab extends TabBase {

    static class RadarViewRefresher implements Refresher {
        private final RadarView mRadarView;

        private final GeoFixProvider mGeoFixProvider;

        public RadarViewRefresher(RadarView radarView, GeoFixProvider geoFixProvider) {
            mRadarView = radarView;
            mGeoFixProvider = geoFixProvider;
        }

        @Override
        public void forceRefresh() {
            refresh();
        }

        @Override
        public void refresh() {
            if (mGeoFixProvider.isProviderEnabled())
                mRadarView.setLocation(mGeoFixProvider.getLocation(), mGeoFixProvider.getAzimuth());
            else
                mRadarView.handleUnknownLocation();
        }
    }

    public static int ACTIVITY_REQUEST_TAKE_PICTURE = 1;

    private Geocache mGeocache;

    private Waypoint mWaypoint;

    private final GuiState mGuiState;

    private final GeocacheViewer mGeocacheViewer;

    private final DbFrontend mDbFrontend;

    private final Activity mActivity;

    private final RadarView mRadarView;

    private final SharedPreferences mSharedPreferences;

    private final ImageView mFavoriteView;

    private final RadarViewRefresher mRadarViewRefresher;

    private final GeoFixProvider mGeoFixProvider;

    private final View mDetailsButton;

    private final View mWebPageButton;

    private final PowerManager.WakeLock mWakeLock;

    private final View mWaypointsButton;

    public CompassTab(Activity activity, GeocacheViewer geocacheViewer, DbFrontend dbFrontend,
            RadarView radarView, SharedPreferences sharedPreferences, ImageView favoriteView,
            GuiState guiState, View contentView, View detailsButton, View webPageButton,
            View waypointsButton, Drawable icon, RadarViewRefresher radarViewRefresher,
            GeoFixProvider geoFixProvider, MenuActions menuActions, PowerManager.WakeLock waleLock) {
        super("tab_compass", getIconLabel(sharedPreferences), contentView, icon, menuActions);
        mActivity = activity;
        mSharedPreferences = sharedPreferences;
        mRadarView = radarView;
        mGeocacheViewer = geocacheViewer;
        mDbFrontend = dbFrontend;
        mFavoriteView = favoriteView;
        mFavoriteView.setOnClickListener(new OnFavoriteClick());
        mGuiState = guiState;
        mDetailsButton = detailsButton;
        mWebPageButton = webPageButton;
        mWaypointsButton = waypointsButton;
        mRadarViewRefresher = radarViewRefresher;
        mGeoFixProvider = geoFixProvider;
        mWakeLock = waleLock;

    }

    private static String getIconLabel(SharedPreferences preferences) {
        return preferences.getBoolean("ui_show_tab_texts", true) ? "Compass" : "";
    }

    public Geocache getActiveGeocache() {
        return mGeocache;
    }

    public Waypoint getActiveWaypoint() {
        return mWaypoint;
    }

    private void onCameraStart() {
        String path = DatabaseLocator.getStoragePath();
        String filename = path + "/" + mGeocache.getId()
                + DateFormat.format(" yyyy-MM-dd kk.mm.ss.jpg", System.currentTimeMillis());
        Log.d("TreasureHunter", "capturing image to " + filename);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filename)));
        mActivity.startActivityForResult(intent, CompassTab.ACTIVITY_REQUEST_TAKE_PICTURE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA && event.getRepeatCount() == 0) {
            onCameraStart();
            return true;
        }
        return false;
    }

    @Override
    public void onCreate() {
        // Log.d("TreasureHunter", "CompassDelegate.onCreate");
    }

    @Override
    public void onPause() {
        // Log.d("TreasureHunter", "CompassDelegate onPause");
        mGeoFixProvider.removeObserver(mRadarViewRefresher);
        if (mWakeLock.isHeld())
            mWakeLock.release();
    }

    @Override
    public void onResume() {
        if (mSharedPreferences.getBoolean("ui_compass_wakelock", false))
            mWakeLock.acquire();
        // Log.d("TreasureHunter", "CompassDelegate onResume");
        mGeocache = mGuiState.getActiveGeocache();
        // mRadarView.handleUnknownLocation();
        mWaypoint = mGuiState.getActiveWaypoint();

        mRadarView.setUseImperial(mSharedPreferences.getBoolean("imperial", false));

        if (mWaypoint != null) {
            mGeocacheViewer.set(mWaypoint);
            // These are useless for waypoints?
            // updateFavoriteImage();
            // mDetailsButton.setEnabled(getDetailsButtonEnabled());
            // mWebPageButton.setEnabled(getWebpageButtonEnabled());
        } else if (mGeocache != null) {
            mGeocacheViewer.set(mGeocache);
            updateFavoriteImage();
            mDetailsButton.setEnabled(getDetailsButtonEnabled());
            mWebPageButton.setEnabled(getWebpageButtonEnabled());
        } else {
            mGeocacheViewer.clear();
            mDetailsButton.setEnabled(false);
            mWebPageButton.setEnabled(false);
        }

        mWaypointsButton.setEnabled(getWaypointsButtonEnabled());
        if (mWaypointsButton.isEnabled()) {
            mWaypointsButton.setVisibility(View.VISIBLE);
        } else {
            mWaypointsButton.setVisibility(View.INVISIBLE);
        }

        mRadarViewRefresher.refresh();
        mGeoFixProvider.addObserver(mRadarViewRefresher);
    }

    private boolean getDetailsButtonEnabled() {
        // Guard for NullPointerException caused here for unknown reason (issue
        // #36)
        if (mGeocache.getSource() == null)
            return false;
        return (mGeocache.getSource().isGpx() || mGeocache.getSource() == Source.BCACHING);
    }

    private boolean getWebpageButtonEnabled() {
        final Provider contentProvider = mGeocache.getContentProvider();
        return (contentProvider == Provider.GROUNDSPEAK
                || contentProvider == GeocacheFactory.Provider.ATLAS_QUEST
                || contentProvider == GeocacheFactory.Provider.OPENCACHING
                || contentProvider == GeocacheFactory.Provider.TERRACACHING
                || contentProvider == GeocacheFactory.Provider.GEOCACHINGAU_GA || contentProvider == GeocacheFactory.Provider.GEOCACHINGAU_TP);
    }

    private boolean getWaypointsButtonEnabled() {
        CharSequence id = null;
        if (mGeocache != null) {
            id = mGeocache.getId();
        } else if (mWaypoint != null) {
            id = mWaypoint.getParentCache();
        }

        if (id != null && !id.equals("")) {
            List<Waypoint> waypoints = mDbFrontend.getRelatedWaypoints(id);

            if (waypoints != null && !waypoints.isEmpty()) {
                return true;
            }
        }

        return false;

    }

    private void updateFavoriteImage() {
        boolean isFavorite = false;
        if (mGeocache != null)
            isFavorite = mGeocache.hasTag(Tags.FAVORITE, mDbFrontend);
        mFavoriteView.setImageResource(isFavorite ? R.drawable.btn_rating_star_on_normal
                : R.drawable.btn_rating_star_off_normal);
    }

    class OnFavoriteClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mGeocache == null)
                return;
            CharSequence id = mGeocache.getId();
            boolean wasFavorite = mGeocache.hasTag(Tags.FAVORITE, mDbFrontend);
            mDbFrontend.setGeocacheTag(id, Tags.FAVORITE, !wasFavorite);
            updateFavoriteImage();
        }
    }

    @Override
    public void onDataViewChanged(GeocacheFilter filter, boolean isTabActive) {
        if (isTabActive)
            onResume();
    }
}
