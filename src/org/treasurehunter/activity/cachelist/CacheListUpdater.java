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

import org.treasurehunter.GeoFix;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheList;
import org.treasurehunter.Refresher;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.database.ICachesProviderCenter;
import org.treasurehunter.task.DelayingTaskRunner;

/**
 * Sends location and azimuth updates to CacheList when the user position
 * changes.
 */
public class CacheListUpdater implements Refresher {
    private final DelayingTaskRunner mListTaskRunner;

    private final CacheListAdapter mCacheListAdapter;

    private final GeoFixProvider mGeoFixProvider;

    private final ICachesProviderCenter mSearchCenter;

    private final ICachesProviderCenter mSortCenter;

    private double mCurrentLatitude;

    private double mCurrentLongitude;

    private Geocache mCacheCenterpoint;

    public CacheListUpdater(GeoFixProvider geoFixProvider, CacheListAdapter cacheListAdapter,
            ICachesProviderCenter searchCenter, ICachesProviderCenter sortCenter,
            DelayingTaskRunner listTaskRunner) {
        mListTaskRunner = listTaskRunner;
        mGeoFixProvider = geoFixProvider;
        mCacheListAdapter = cacheListAdapter;
        mSearchCenter = searchCenter;
        mSortCenter = sortCenter;
        mCacheCenterpoint = null;
    }

    @Override
    public void refresh() {
        double latitude = 0.0, longitude = 0.0;
        if (null == mCacheCenterpoint) {
            final GeoFix location = mGeoFixProvider.getLocation();
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } else {
            latitude = mCacheCenterpoint.getLatitude();
            longitude = mCacheCenterpoint.getLongitude();
        }
        setCenter(latitude, longitude);
    }

    @Override
    public void forceRefresh() {
        refresh();
    }

    /** Clear the list and do a new calculation */
    public void onDataViewChanged() {
        mCacheListAdapter.setGeocacheList(null); // Clears the list
        mListTaskRunner.abort();
        mListTaskRunner.runTask(new CacheListTask(this, mSearchCenter, mSortCenter,
                mCurrentLatitude, mCurrentLongitude, true));
    }

    public void setCacheListState(double latitude, double longitude, GeocacheList geocacheList) {
        mCurrentLatitude = latitude;
        mCurrentLongitude = longitude;
        mCacheListAdapter.setGeocacheList(geocacheList);
    }

    public void setCenter(double latitude, double longitude) {
        if (Util.approxEquals(mCurrentLatitude, latitude)
                && Util.approxEquals(mCurrentLongitude, longitude))
            return;
        mListTaskRunner.runTask(new CacheListTask(this, mSearchCenter, mSortCenter, latitude,
                longitude, false));
    }

    public void setCacheAsCenter(Geocache geocache) {
        mCacheCenterpoint = geocache;
        refresh();
    }

    public boolean isCenterpointEnabled() {
        return (null != mCacheCenterpoint);
    }

    public void onPause() {
        mListTaskRunner.pause();
    }

    public void onResume() {
        refresh();
        mListTaskRunner.resume();
    }

    public GeocacheList getCurrentList() {
        return mCacheListAdapter.getListData();
    }

    public double getCurrentLatitude() {
        return mCurrentLatitude;
    }

    public double getCurrentLongitude() {
        return mCurrentLongitude;
    }
}
