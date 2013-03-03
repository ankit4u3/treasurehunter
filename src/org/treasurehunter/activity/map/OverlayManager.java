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

import com.google.android.maps.Overlay;

import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.GeocacheList;
import org.treasurehunter.GeocacheListPrecomputed;
import org.treasurehunter.Toaster;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.map.DensityMatrix.DensityPatch;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.task.DelayingTaskRunner;

import android.util.Log;

import java.util.List;

public class OverlayManager {
    // Environment
    private final DensityOverlay mDensityOverlay;

    private final DistanceOverlay mDistanceOverlay;

    private final GeoMapView mGeoMapView;

    private final CachesProviderDb mCachesProviderDb;

    private final Toaster mTooManyCachesToaster;

    private final Toaster mDensityViewToaster;

    private final CachePinsOverlayFactory mPinOverlayFactory;

    private final CacheHint mCacheHint;

    // Internal state
    private GeocacheList mPinList = GeocacheListPrecomputed.EMPTY;

    private boolean mUsesDensityMap;

    public boolean mIsTooManyCaches = false;

    private Geocache mSelected;

    private Waypoint mSelectedWaypoint;

    public Area mArea;

    public GeocacheFilter mCacheFilter;

    private boolean mNeedsRefreshing = true;

    private final DelayingTaskRunner mOverlayCreatorRunner;

    public OverlayManager(GeoMapView geoMapView, DensityOverlay densityOverlay,
            boolean usesDensityMap, CachesProviderDb cachesProviderArea,
            GeocacheFilter cacheFilter, Toaster tooManyCachesToaster, Toaster densityViewToaster,
            CachePinsOverlayFactory pinOverlayFactory, CacheHint cacheHint,
            DelayingTaskRunner overlayCreatorRunner, DistanceOverlay distanceOverlay) {
        mGeoMapView = geoMapView;
        mDensityOverlay = densityOverlay;
        mDistanceOverlay = distanceOverlay;
        mUsesDensityMap = usesDensityMap;
        mCachesProviderDb = cachesProviderArea;
        mCacheFilter = cacheFilter;
        mTooManyCachesToaster = tooManyCachesToaster;
        mPinOverlayFactory = pinOverlayFactory;
        mDensityViewToaster = densityViewToaster;
        mCacheHint = cacheHint;
        mOverlayCreatorRunner = overlayCreatorRunner;
    }

    public void initArea() {
        mArea = mGeoMapView.getVisibleArea();
        mNeedsRefreshing = true;
    }

    private OverlayCreatorTask makeWorker(Area area) {
        return new OverlayCreatorTask(this, mCachesProviderDb, mPinOverlayFactory, area,
                mCacheFilter, mSelected);
    }

    public Geocache getSelectedGeocache() {
        return mSelected;
    }

    /** Returns null if no waypoint is selected */
    public Waypoint getSelectedWaypoint() {
        return mSelectedWaypoint;
    }

    public void setSelectedGeocache(Geocache geocache) {
        if (geocache == mSelected && mSelectedWaypoint == null)
            return;
        mSelected = geocache;
        mSelectedWaypoint = null;
        if (mUsesDensityMap)
            return;
        Overlay overlay = mPinOverlayFactory.makeOverlay(geocache, mPinList, this);
        mGeoMapView.setOverlay(overlay);
        mDistanceOverlay.setGeoObject(geocache);
    }

    public void setSelectedWaypoint(Waypoint waypoint, Geocache parent) {
        if (parent == mSelected && waypoint == mSelectedWaypoint)
            return;
        mSelected = parent;
        mSelectedWaypoint = waypoint;
        if (mUsesDensityMap)
            return;
        Overlay overlay = mPinOverlayFactory.makeOverlay(parent, waypoint, mPinList, this);
        mGeoMapView.setOverlay(overlay);
        mDistanceOverlay.setGeoObject(waypoint);
    }

    public void refreshIfNeeded() {
        if (!mNeedsRefreshing)
            return;
        mNeedsRefreshing = false;
        mOverlayCreatorRunner.abort();
        mGeoMapView.clearOverlay();
        mOverlayCreatorRunner.runTask(makeWorker(mArea));
    }

    public void invalidate() {
        mNeedsRefreshing = true;
    }

    public void setCacheFilter(GeocacheFilter cacheFilter) {
        mNeedsRefreshing = true;
        mCacheFilter = cacheFilter;
        mOverlayCreatorRunner.abort();
    }

    public void databaseChanged() {
        mNeedsRefreshing = true;
        mCachesProviderDb.notifyOfDbChange();
        mOverlayCreatorRunner.abort();
    }

    public void onScrollTo(Area area) {
        if (area.equals(mArea))
            return;

        if (!mIsTooManyCaches && !mUsesDensityMap && mArea.contains(area)) {
            // This is a zoomed in area, the old result is good
            return;
        }

        mOverlayCreatorRunner.runTask(makeWorker(area));
    }

    class PinOverlayResult implements Runnable {
        Overlay mOverlay;

        Area mNewArea;

        GeocacheList mList;

        @Override
        public void run() {
            mGeoMapView.setOverlay(mOverlay);
            mUsesDensityMap = false;
            mIsTooManyCaches = false;
            mPinList = mList;
            mArea = mNewArea;
        }
    }

    class DensityPatchesResult implements Runnable {
        Area mNewArea;

        public List<DensityPatch> mDensityPatches;

        @Override
        public void run() {
            mCacheHint.hide();
            mDensityOverlay.setPatches(mDensityPatches);
            mGeoMapView.setOverlay(mDensityOverlay);
            if (!mUsesDensityMap) {
                Log.d("TreasureHunter", "Switching to density view");
                mDensityViewToaster.showToast();
            }
            mUsesDensityMap = true;
            mIsTooManyCaches = false;
            mArea = mNewArea;
        }
    }

    class TooManyCachesResult implements Runnable {
        Area mNewArea;

        @Override
        public void run() {
            mCacheHint.hide();
            // mGeoMapView.clearOverlay();
            if (!mIsTooManyCaches) {
                mTooManyCachesToaster.showToast();
            }
            mIsTooManyCaches = true;
            // mArea = mNewArea;
        }
    }
}
