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

import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.GeocacheList;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.task.Task;

import android.os.Handler;

public class OverlayCreatorTask extends Task {

    /** Maximum number of caches to show as individual pins */
    public static final int MAX_PIN_COUNT = 90;

    /** Maximum number of caches to process for the density overlay */
    public static final int MAX_DENSITY_COUNT = 1500;

    /** Get geocaches in an area with sides this ratio bigger than the visible */
    public static final float EXPAND_RATIO = 1.0f;

    public static final double RESOLUTION_LATITUDE = 0.01;

    public static final double RESOLUTION_LONGITUDE = 0.02;

    private final OverlayManager mOverlayManager;

    private final CachesProviderDb mCachesProvider;

    private final CachePinsOverlayFactory mPinOverlayFactory;

    // private Clock mClock = new Clock();
    private final Area mArea;

    private final GeocacheFilter mCacheFilter;

    private final Geocache mSelected;

    public OverlayCreatorTask(OverlayManager overlayManager, CachesProviderDb cachesProviderDb,
            CachePinsOverlayFactory pinOverlayFactory, Area area, GeocacheFilter cacheFilter,
            Geocache selected) {
        mOverlayManager = overlayManager;
        mCachesProvider = cachesProviderDb;
        mPinOverlayFactory = pinOverlayFactory;
        mArea = area;
        mCacheFilter = cacheFilter;
        mSelected = selected;
    }

    protected void doInBackground(Handler handler) {
        // Log.d("TreasureHunter", "doInBackground starting " +
        // this.hashCode());
        // Expand slightly to include pins that are just outside the screen:
        Area area = mArea.expand(0.1f);
        // double start = mClock.getCurrentTime();

        mCachesProvider.setBounds(area.mLatLow, area.mLonLow, area.mLatHigh, area.mLonHigh);
        if (isAborted())
            return;
        mCachesProvider.setFilter(mCacheFilter);

        // Reading one extra cache to see if there are too many
        GeocacheList list = mCachesProvider.getCaches(MAX_DENSITY_COUNT + 1);
        // Log.d("TreasureHunter", "doInBackground got DB list " +
        // this.hashCode());
        if (isAborted())
            return;

        if (list.size() <= MAX_PIN_COUNT) {
            // Compare with the previous list to determine if we should make a
            // new overlay?
            OverlayManager.PinOverlayResult result = mOverlayManager.new PinOverlayResult();
            result.mOverlay = mPinOverlayFactory.makeOverlay(mSelected, list, mOverlayManager);
            if (isAborted())
                return;
            result.mNewArea = area;
            result.mList = list;
            // Log.d("TreasureHunter",
            // "doInBackground "+this.hashCode()+" took " +
            // (mClock.getCurrentTime() - start) + " ms for " + list.size() +
            // " pins");
            handler.post(result);
        } else if (list.size() <= MAX_DENSITY_COUNT) {
            OverlayManager.DensityPatchesResult result = mOverlayManager.new DensityPatchesResult();
            DensityMatrix densityMatrix = new DensityMatrix(RESOLUTION_LATITUDE,
                    RESOLUTION_LONGITUDE);
            densityMatrix.addCaches(list);
            if (isAborted())
                return;
            result.mDensityPatches = densityMatrix.getDensityPatches();
            result.mNewArea = area;
            // Log.d("TreasureHunter",
            // "doInBackground "+this.hashCode()+" took " +
            // (mClock.getCurrentTime() - start) + " ms for " + list.size() +
            // " density");
            handler.post(result);
        } else {
            OverlayManager.TooManyCachesResult result = mOverlayManager.new TooManyCachesResult();
            result.mNewArea = area;
            // Log.d("TreasureHunter",
            // "doInBackground "+this.hashCode()+" took " +
            // (mClock.getCurrentTime() - start) + " ms for too many caches");
            handler.post(result);
        }
    }
}
