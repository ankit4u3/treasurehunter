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

import com.google.android.maps.ItemizedOverlay;

import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

import android.graphics.drawable.Drawable;

import java.util.List;

public class CachePinsOverlay extends ItemizedOverlay<CacheItem> {

    private final List<CacheItem> mCacheItems;

    private final CacheHint mCacheHint;

    private final OverlayManager mOverlayManager;

    private final GuiState mGuiState;

    private final DbFrontend mDbFrontend;

    public CachePinsOverlay(List<CacheItem> cacheItems, CacheHint cacheHintHolder,
            Drawable defaultMarker, OverlayManager overlayManager, GuiState guiState,
            DbFrontend dbFrontend) {
        super(boundCenterBottom(defaultMarker));
        mCacheItems = cacheItems;
        mCacheHint = cacheHintHolder;
        mOverlayManager = overlayManager;
        mGuiState = guiState;
        mDbFrontend = dbFrontend;
        populate();
    }

    @Override
    protected boolean onTap(int i) {
        final GeoObject geoObject = getItem(i).getGeoObject();
        if (geoObject == null) {
            mCacheHint.hide();
            return false;
        }

        if (mCacheHint.getGeoObject() == geoObject)
            mCacheHint.toggle();
        else
            mCacheHint.showGeoObject(geoObject);

        if (geoObject instanceof Geocache) {
            mOverlayManager.setSelectedGeocache((Geocache)geoObject);
            mGuiState.setCurrentGeocache(geoObject.getId());
        } else {
            Geocache parent = mDbFrontend.loadCacheFromId(((Waypoint)geoObject).getParentCache());
            mOverlayManager.setSelectedWaypoint((Waypoint)geoObject, parent);
            mGuiState.setCurrentWaypoint(geoObject.getId());
        }

        // mGuiState.showCompass(geocache);
        return true;
    }

    @Override
    protected CacheItem createItem(int i) {
        return mCacheItems.get(i);
    }

    @Override
    public int size() {
        return mCacheItems.size();
    }
}
