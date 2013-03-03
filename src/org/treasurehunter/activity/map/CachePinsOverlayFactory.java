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
import org.treasurehunter.GeocacheList;
import org.treasurehunter.GraphicsGenerator;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class CachePinsOverlayFactory {
    private final Drawable mDefaultMarker;

    private final GraphicsGenerator mGraphicsGenerator;

    private final DbFrontend mDbFrontend;

    private final CacheHint mCacheHintHolder;

    private final GuiState mGuiState;

    public CachePinsOverlayFactory(GraphicsGenerator graphicsGenerator, DbFrontend dbFrontend,
            Drawable defaultMarker, CacheHint cacheHintHolder, GuiState guiState) {
        mDefaultMarker = defaultMarker;
        mGraphicsGenerator = graphicsGenerator;
        mDbFrontend = dbFrontend;
        mCacheHintHolder = cacheHintHolder;
        mGuiState = guiState;
    }

    private CacheItem createCacheItem(Geocache geocache, boolean selected) {
        final CacheItem cacheItem = new CacheItem(geocache.getGeoPoint(), geocache);
        Drawable marker = mGraphicsGenerator.getMapIcon(geocache, selected, mDbFrontend);
        cacheItem.setMarker(marker);
        return cacheItem;
    }

    private CacheItem createWaypointItem(Waypoint waypoint, boolean selected) {
        final CacheItem cacheItem = new CacheItem(waypoint.getGeoPoint(), waypoint);
        Drawable marker = mGraphicsGenerator.getMapIcon(waypoint, selected, mDbFrontend);
        cacheItem.setMarker(marker);
        return cacheItem;
    }

    public Overlay makeOverlay(Geocache selected, GeocacheList list, OverlayManager overlayManager) {
        return makeOverlay(selected, null, list, overlayManager);
    }

    public Overlay makeOverlay(Geocache selected, Waypoint selectedWaypoint, GeocacheList list,
            OverlayManager overlayManager) {
        List<CacheItem> cacheItems = new ArrayList<CacheItem>(list.size());
        for (Geocache geocache : list) {
            boolean isSelected = (geocache == selected);
            cacheItems.add(createCacheItem(geocache, isSelected));
        }
        if (selected != null) {
            List<Waypoint> waypoints = mDbFrontend.getRelatedWaypoints(selected.getId());
            for (Waypoint wp : waypoints) {
                boolean isSelected = (wp.equals(selectedWaypoint));
                cacheItems.add(createWaypointItem(wp, isSelected));
            }
        }
        return new CachePinsOverlay(cacheItems, mCacheHintHolder, mDefaultMarker, overlayManager,
                mGuiState, mDbFrontend);
    }
}
