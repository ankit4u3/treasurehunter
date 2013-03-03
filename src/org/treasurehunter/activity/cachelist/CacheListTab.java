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

package org.treasurehunter.activity.cachelist;

import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.activity.main.TabBase;
import org.treasurehunter.cacheactions.CacheAction;
import org.treasurehunter.cacheactions.CacheContextMenu;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.menuactions.MenuActions;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class CacheListTab extends TabBase implements OnItemClickListener {
    private final CacheContextMenu mContextMenu;

    private final Context mContext;

    private final DistanceFormatter mDistanceFormatter;

    private final CacheListRowInflater mGeocacheSummaryRowInflater;

    private final CacheListAdapter mCacheListAdapter;

    private final CacheAction mOnClickCacheAction;

    private final GeoFixProvider mGeoFixProvider;

    private final CacheListUpdater mCacheListPositionUpdater;

    private final GpsStatusWidgetDelegate mGpsStatusWidgetDelegate;

    private final GpsWidgetUpdater mUpdateGpsWidgetRunnable;

    // private final ITaskRunner mTaskRunner;
    private final CachesProviderDb mCachesProviderDbClosest;

    private final CachesProviderDb mCachesProviderDbAll;

    public CacheListTab(CacheContextMenu menuCreator,
            CacheListRowInflater geocacheSummaryRowInflater, Context context,
            DistanceFormatter distanceFormatterManager, MenuActions menuActions, View contentView,
            Drawable icon, CacheAction onClickCacheAction, CacheListAdapter cacheListAdapter,
            GeoFixProvider geoFixProvider, CacheListUpdater cacheListPositionUpdater,
            GpsStatusWidgetDelegate gpsStatusWidgetDelegate,
            GpsWidgetUpdater updateGpsWidgetRunnable,
            // ITaskRunner taskRunner,
            CachesProviderDb cachesProviderDbClosest, CachesProviderDb cachesProviderDbAll,
            SharedPreferences sharedPreferences) {
        super("tab_list", getIconLabel(sharedPreferences), contentView, icon, menuActions);
        mContextMenu = menuCreator;
        mGeocacheSummaryRowInflater = geocacheSummaryRowInflater;
        mContext = context;
        mDistanceFormatter = distanceFormatterManager;
        mOnClickCacheAction = onClickCacheAction;
        mCacheListAdapter = cacheListAdapter;
        mGeoFixProvider = geoFixProvider;
        mCacheListPositionUpdater = cacheListPositionUpdater;
        mGpsStatusWidgetDelegate = gpsStatusWidgetDelegate;
        mUpdateGpsWidgetRunnable = updateGpsWidgetRunnable;
        // mTaskRunner = taskRunner;
        mCachesProviderDbClosest = cachesProviderDbClosest;
        mCachesProviderDbAll = cachesProviderDbAll;
    }

    private static String getIconLabel(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("ui_show_tab_texts", true) ? "List" : "";
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        return mContextMenu.onContextItemSelected(menuItem);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        if (position > 0) {
            mOnClickCacheAction.act(mCacheListAdapter.getGeocacheAt(position - 1), null);
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onPause() {
        // Log.d("TreasureHunter", "CacheListDelegate.onPause()");
        mCacheListPositionUpdater.onPause();
        mGeoFixProvider.removeObserver(mCacheListPositionUpdater);
        mGeoFixProvider.removeObserver(mGpsStatusWidgetDelegate);
        mUpdateGpsWidgetRunnable.abort();
        // mTaskRunner.abort();
    }

    @Override
    public void onResume() {
        // Log.d("TreasureHunter", "CacheListDelegate.onResume()");
        mDistanceFormatter.updateFormatter();
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        final boolean absoluteBearing = sharedPreferences.getBoolean("absolute-bearing", false);
        mGeocacheSummaryRowInflater.setBearingFormatter(absoluteBearing);

        mCacheListPositionUpdater.onResume();

        mUpdateGpsWidgetRunnable.resume();
        mGeoFixProvider.addObserver(mCacheListPositionUpdater);
        mGeoFixProvider.addObserver(mGpsStatusWidgetDelegate);
    }

    @Override
    public void onDataViewChanged(GeocacheFilter filter, boolean isTabActive) {
        mCachesProviderDbClosest.setFilter(filter);
        mCachesProviderDbAll.setFilter(filter);
        mCachesProviderDbClosest.notifyOfDbChange();
        mCachesProviderDbAll.notifyOfDbChange();
        mCacheListPositionUpdater.onDataViewChanged();
    }

    public CacheListAdapter getCacheListAdapter() {
        return mCacheListAdapter;
    }
}
