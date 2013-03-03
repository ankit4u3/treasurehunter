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

import org.treasurehunter.GeocacheList;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.database.ICachesProviderCenter;
import org.treasurehunter.task.Task;

import android.os.Handler;

/**
 * Finds the closest caches and sorts them before sending the list to
 * CacheListAdapter
 */
public class CacheListTask extends Task {
    private final CacheListUpdater mCacheListUpdater;

    private final ICachesProviderCenter mSearchCenter;

    private final ICachesProviderCenter mSortCenter;

    private final double mLatitude;

    private final double mLongitude;

    private final boolean mForce;

    public CacheListTask(CacheListUpdater cacheListUpdater, ICachesProviderCenter searchCenter,
            ICachesProviderCenter sortCenter, double latitude, double longitude, boolean force) {
        mCacheListUpdater = cacheListUpdater;
        mSearchCenter = searchCenter;
        mSortCenter = sortCenter;
        mLatitude = latitude;
        mLongitude = longitude;
        mForce = force;
    }

    protected void doInBackground(Handler handler) throws IllegalStateException {
        if (!mForce && Util.approxEquals(mLatitude, mCacheListUpdater.getCurrentLatitude())
                && Util.approxEquals(mLongitude, mCacheListUpdater.getCurrentLongitude()))
            return;

        if (isAborted())
            return;

        // Log.d("TreasureHunter",
        // "CacheListTask "+this.hashCode()+" starting...");
        mSearchCenter.setCenter(mLatitude, mLongitude);
        mSortCenter.setCenter(mLatitude, mLongitude);
        if (isAborted())
            return;
        final GeocacheList result = mSortCenter.getCaches();
        if (isAborted())
            return;
        // If this is the first calculation, getCurrentList() will return null
        // and equals() will be false
        boolean equals = result.equals(mCacheListUpdater.getCurrentList());
        if (equals) {
            return;
        }
        if (isAborted())
            return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                mCacheListUpdater.setCacheListState(mLatitude, mLongitude, result);
            }
        });
        // Log.d("TreasureHunter",
        // "CacheListTask "+this.hashCode()+" finished");
    }
}
