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

package org.treasurehunter.database;

import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.GeocacheList;
import org.treasurehunter.activity.compass.Util;

import android.util.Log;

/**
 * Uses a DB to fetch the caches within a defined region, or all caches if no
 * bounds were specified
 */
public class CachesProviderDb implements ICachesProviderArea {

    private DbFrontend mDbFrontend;

    private double mLatLow;

    private double mLonLow;

    private double mLatHigh;

    private double mLonHigh;

    /**
     * The complete SQL query for the current coordinates and settings, except
     * from 'SELECT x'. If null, mSql needs to be re-calculated.
     */
    private String mSql = null;

    /** The SQL query without bounds. Always set */
    private String mAllSql = "";

    private GeocacheFilter mCacheFilter;

    private GeocacheList mCaches;

    private boolean mHasChanged = true;

    private boolean mHasLimits = false;

    /**
     * If greater than zero, this is the max number that mCaches was allowed to
     * contain when loaded. (This limit can change on subsequent loads)
     */
    private int mCachesCappedToCount = 0;

    private int mTotalCount = -1;

    private final boolean mPrecomputeList;

    public CachesProviderDb(DbFrontend dbFrontend) {
        mDbFrontend = dbFrontend;
        mPrecomputeList = false;
    }

    public CachesProviderDb(DbFrontend dbFrontend, boolean precomputeList) {
        mDbFrontend = dbFrontend;
        mPrecomputeList = precomputeList;
    }

    @Override
    public GeocacheList getCaches() {
        // if (mHasLimits && mCacheFilter == null)
        // return GeocacheListPrecomputed.EMPTY;
        if (mCaches == null || mCachesCappedToCount > 0) {
            if (mPrecomputeList)
                mCaches = mDbFrontend.loadCachesRawPrecomputed(getSql());
            else
                mCaches = mDbFrontend.loadCachesRaw("SELECT Id " + getSql());
            if (!mHasLimits)
                mTotalCount = mCaches.size();
        }
        return mCaches;
    }

    @Override
    public GeocacheList getCaches(int maxResults) {
        // if (mHasLimits && mCacheFilter == null)
        // return GeocacheListPrecomputed.EMPTY;
        if (mCaches == null || (mCachesCappedToCount > 0 && mCachesCappedToCount < maxResults)) {
            if (mPrecomputeList)
                mCaches = mDbFrontend
                        .loadCachesRawPrecomputed(getSql() + " LIMIT 0, " + maxResults);
            else
                mCaches = mDbFrontend.loadCachesRaw("SELECT Id " + getSql() + " LIMIT 0, "
                        + maxResults);
            if (mCaches.size() == maxResults) {
                mCachesCappedToCount = maxResults;
            } else {
                // The cap didn't limit the search result
                mCachesCappedToCount = 0;
                if (!mHasLimits)
                    mTotalCount = mCaches.size();
            }
        }
        return mCaches;
    }

    @Override
    public int getCount() {
        return getCaches().size();
        // if (mCaches == null || mCachesCappedToCount > 0) {
        // return mDbFrontend.countRaw("SELECT COUNT(*) " + getSql());
        // }
        // return mCaches.size();
    }

    @Override
    public void clearBounds() {
        if (!mHasLimits)
            return;
        mHasLimits = false;
        mCaches = null; // Flush old caches
        mSql = null;
        mHasChanged = true;
    }

    @Override
    public void setBounds(double latLow, double lonLow, double latHigh, double lonHigh) {
        if (Util.approxEquals(latLow, mLatLow) && Util.approxEquals(latHigh, mLatHigh)
                && Util.approxEquals(lonLow, mLonLow) && Util.approxEquals(lonHigh, mLonHigh)) {
            return;
        }
        mLatLow = latLow;
        mLatHigh = latHigh;
        mLonLow = lonLow;
        mLonHigh = lonHigh;
        mCaches = null; // Flush old caches
        mSql = null;
        mHasChanged = true;
        mHasLimits = true;
    }

    @Override
    public boolean hasChanged() {
        return mHasChanged;
    }

    @Override
    public void resetChanged() {
        mHasChanged = false;
    }

    /**
     * Tells this class that the contents in the database have changed. The
     * cached list isn't reliable any more.
     */
    public void notifyOfDbChange() {
        mCaches = null;
        mHasChanged = true;
        mTotalCount = -1; // Flush the count
    }

    private String getSql() {
        if (mHasLimits) {
            if (mCacheFilter == null) {
                Log.e("TreasureHunter", "CachesProviderDb: Limits but no cacheFilter!");
                return "FROM CACHES"; // Dummy query to avoid crash
            }
            if (mSql == null) {
                mSql = mCacheFilter.getSql(mLatLow, mLonLow, mLatHigh, mLonHigh);
            }
            return mSql;
        } else {
            return mAllSql;
        }
    }

    public void setFilter(GeocacheFilter cacheFilter) {
        String newAllSql = cacheFilter.getSql();
        if (mAllSql.equals(newAllSql)) {
            return;
        }

        mHasChanged = true;
        mCacheFilter = cacheFilter;
        mAllSql = newAllSql;
        mSql = null; // Flush
        mCaches = null; // Flush old caches
        mTotalCount = -1;
    }

    @Override
    public int getTotalCount() {
        if (mTotalCount != -1)
            return mTotalCount;
        if (mAllSql.equals(""))
            mTotalCount = mDbFrontend.countAll();
        else
            mTotalCount = mDbFrontend.countRaw("SELECT COUNT(*) " + mAllSql);
        return mTotalCount;
    }
}
