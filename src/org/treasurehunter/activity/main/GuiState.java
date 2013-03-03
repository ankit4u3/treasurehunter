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

package org.treasurehunter.activity.main;

import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.filterlist.FilterTypeCollection;
import org.treasurehunter.database.DbFrontend;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.List;

/** The global GUI state of the TreasureHunter application. Mutable. */
public class GuiState {
    private final DbFrontend mDbFrontend;

    private final TabHostWrapper mTabHostWrapper;

    private final FilterTypeCollection mFilterTypeCollection;

    private final GeocacheFactory mGeocacheFactory;

    private final Handler mUiUpdater;

    private String mCurrentGeocacheId = "";

    private String mCurrentWaypointId = "";

    public GuiState(DbFrontend dbFrontend, TabHostWrapper tabHostWrapper,
            FilterTypeCollection filterTypeCollection, GeocacheFactory geocacheFactory) {
        mDbFrontend = dbFrontend;
        mTabHostWrapper = tabHostWrapper;
        mFilterTypeCollection = filterTypeCollection;
        mGeocacheFactory = geocacheFactory;
        mUiUpdater = new Handler();
    }

    /** Returns the active id or "" if no geocache is active. */
    public String getActiveGeocacheId() {
        return mCurrentGeocacheId;
    }

    /** Returns the id of active waypoint or "" if no waypoint is active. */
    public String getActiveWaypointId() {
        return mCurrentWaypointId;
    }

    public Handler getUiUpdater() {
        return mUiUpdater;
    }

    public Geocache getActiveGeocache() {
        // geocache = mGeocacheFactory.create("", "", 0, 0, Source.MY_LOCATION,
        // CacheType.NULL, 0, 0, 0);
        if (mCurrentGeocacheId.equals(""))
            return null;
        return mDbFrontend.loadCacheFromId(mCurrentGeocacheId);
    }

    public Waypoint getActiveWaypoint() {
        // geocache = mGeocacheFactory.create("", "", 0, 0, Source.MY_LOCATION,
        // CacheType.NULL, 0, 0, 0);
        if (mCurrentWaypointId.equals(""))
            return null;
        return mDbFrontend.loadWaypointFromId(mCurrentWaypointId);
    }

    public void setCurrentGeocache(CharSequence id) {
        mCurrentGeocacheId = (String)id;
        mCurrentWaypointId = "";
    }

    public void setCurrentWaypoint(CharSequence id) {
        mCurrentWaypointId = (String)id;
    }

    public GeocacheFilter getActiveFilter() {
        return mFilterTypeCollection.getActiveFilter();
    }

    // This method could be more discriminating in what flushing needs to
    // be done depending on which operation that was done.
    // Types of changes that could be distinguished between:
    // tags, filter, database contents, total count
    /** Call this to notify everyone that the database has changed in some way */
    public void notifyDataViewChanged() {
        mGeocacheFactory.flushCache();

        mDbFrontend.flushTotalCount();

        if (!mCurrentGeocacheId.equals("")) {
            if (mDbFrontend.loadCacheFromId(mCurrentGeocacheId) == null)
                mCurrentGeocacheId = "";
        }

        GeocacheFilter filter = mFilterTypeCollection.getActiveFilter();
        List<TabBase> tabs = mTabHostWrapper.getTabs();
        for (TabBase tab : tabs) {
            tab.onDataViewChanged(filter, IsTabActive(tab));
        }
    }

    private boolean IsTabActive(TabBase tab) {
        return (tab == mTabHostWrapper.getCurrentTab());
    }

    public void setActiveFilter(GeocacheFilter filter) {
        mFilterTypeCollection.setActiveFilter(filter);
        notifyDataViewChanged();
    }

    public void showCacheList() {
        mTabHostWrapper.switchToTab(TabHostWrapper.LIST_TAB);
    }

    public void showCompass(Geocache geocache) {
        // Log.d("TreasureHunter", "Showing compass for GC: "+geocache);
        setCurrentGeocache(geocache.getId());
        mTabHostWrapper.switchToTab(TabHostWrapper.COMPASS_TAB);
    }

    public void showCompass(Waypoint waypoint) {
        // Log.d("TreasureHunter", "Showing compass for WP: "+waypoint);
        setCurrentWaypoint(waypoint.getId());
        mTabHostWrapper.switchToTab(TabHostWrapper.COMPASS_TAB);
    }

    public void showMap(GeoObject geocache) {
        setCurrentGeocache(geocache.getId());
        mTabHostWrapper.switchToTab(TabHostWrapper.MAP_TAB);
    }

    void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentGeocacheId = savedInstanceState.getString(Geocache.ID);
            Log.d("TreasureHunter", "GuiState.onCreate using geocache " + mCurrentGeocacheId);
        } else {
            mCurrentGeocacheId = "";
        }
    }

    void onSaveInstanceState(Bundle outState) {
        Log.d("TreasureHunter", "GuiState.onSaveInstanceState saving " + mCurrentGeocacheId);
        outState.putString(Geocache.ID, mCurrentGeocacheId);
    }
}
