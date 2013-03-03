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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.content.Context;
import android.util.AttributeSet;

public class GeoMapView extends MapView {
    private OverlayManager mOverlayManager;

    private Overlay mEmptyOverlay;

    private Area mArea = new Area();

    public GeoMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Overlay emptyOverlay, Overlay myLocationOverlay, Overlay distanceOverlay) {
        mEmptyOverlay = emptyOverlay;
        getOverlays().clear();
        getOverlays().add(emptyOverlay);
        getOverlays().add(distanceOverlay);
        getOverlays().add(myLocationOverlay);
    }

    Area getVisibleArea() {
        Projection projection = getProjection();
        GeoPoint newTopLeft = projection.fromPixels(0, 0);
        GeoPoint newBottomRight = projection.fromPixels(getRight(), getBottom());
        Area area = new Area();
        area.mLatLow = newBottomRight.getLatitudeE6() / 1.0E6;
        area.mLatHigh = newTopLeft.getLatitudeE6() / 1.0E6;
        area.mLonLow = newTopLeft.getLongitudeE6() / 1.0E6;
        area.mLonHigh = newBottomRight.getLongitudeE6() / 1.0E6;
        return area;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mOverlayManager != null && getVisibility() == VISIBLE) {
            updateIfMoved();
        }
    }

    public void updateIfMoved() {
        Area area = getVisibleArea();
        if (!area.equals(mArea)) {
            mArea = area;
            mOverlayManager.onScrollTo(mArea);
        }
    }

    public void clearOverlay() {
        getOverlays().set(0, mEmptyOverlay);
        postInvalidate();
    }

    public void setOverlay(Overlay overlay) {
        getOverlays().set(0, overlay);
        postInvalidate();
    }

    public void setScrollListener(OverlayManager overlayManager) {
        mOverlayManager = overlayManager;
    }

}
