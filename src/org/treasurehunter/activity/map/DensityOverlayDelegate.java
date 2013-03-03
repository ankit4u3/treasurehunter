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
import com.google.android.maps.Projection;

import org.treasurehunter.activity.map.DensityMatrix.DensityPatch;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class DensityOverlayDelegate {
    public static final double RESOLUTION_LATITUDE = 0.01;

    public static final double RESOLUTION_LONGITUDE = 0.02;

    public static final int RESOLUTION_LATITUDE_E6 = (int)(RESOLUTION_LATITUDE * 1E6);

    public static final int RESOLUTION_LONGITUDE_E6 = (int)(RESOLUTION_LONGITUDE * 1E6);

    private final Paint mPaint;

    private final Rect mPatchRect;

    private final Point mScreenBottomRight;

    private final Point mScreenTopLeft;

    private List<DensityPatch> mPatches = new ArrayList<DensityPatch>(0);

    public DensityOverlayDelegate(Rect patchRect, Paint paint, Point screenLow, Point screenHigh) {
        mPatchRect = patchRect;
        mPaint = paint;
        mScreenTopLeft = screenLow;
        mScreenBottomRight = screenHigh;
    }

    public void setPatches(List<DensityPatch> patches) {
        mPatches = patches;
    }

    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow)
            return; // No shadow layer
        // Log.d("TreasureHunter", ">>>>>>>>>>Starting draw");

        final Projection projection = mapView.getProjection();
        final GeoPoint newGeoTopLeft = projection.fromPixels(0, 0);
        final GeoPoint newGeoBottomRight = projection.fromPixels(mapView.getWidth(),
                mapView.getHeight());

        projection.toPixels(newGeoTopLeft, mScreenTopLeft);
        projection.toPixels(newGeoBottomRight, mScreenBottomRight);
        final int topLatitudeE6 = newGeoTopLeft.getLatitudeE6();
        final int leftLongitudeE6 = newGeoTopLeft.getLongitudeE6();
        final int bottomLatitudeE6 = newGeoBottomRight.getLatitudeE6();
        final int rightLongitudeE6 = newGeoBottomRight.getLongitudeE6();
        final double pixelsPerLatE6Degrees = (double)(mScreenBottomRight.y - mScreenTopLeft.y)
                / (double)(bottomLatitudeE6 - topLatitudeE6);
        final double pixelsPerLonE6Degrees = (double)(mScreenBottomRight.x - mScreenTopLeft.x)
                / (double)(rightLongitudeE6 - leftLongitudeE6);

        int patchCount = 0;
        for (DensityPatch patch : mPatches) {
            final int patchLatLowE6 = patch.getLatLowE6();
            final int patchLonLowE6 = patch.getLonLowE6();
            int xOffset = (int)((patchLonLowE6 - leftLongitudeE6) * pixelsPerLonE6Degrees);
            int xEnd = (int)((patchLonLowE6 + RESOLUTION_LONGITUDE_E6 - leftLongitudeE6) * pixelsPerLonE6Degrees);
            int yOffset = (int)((patchLatLowE6 - topLatitudeE6) * pixelsPerLatE6Degrees);
            int yEnd = (int)((patchLatLowE6 + RESOLUTION_LATITUDE_E6 - topLatitudeE6) * pixelsPerLatE6Degrees);
            mPatchRect.set(xOffset, yEnd, xEnd, yOffset);
            mPaint.setAlpha(patch.getAlpha());
            canvas.drawRect(mPatchRect, mPaint);
            patchCount++;
        }
        // mTiming.lap("Done drawing");
        // Log.d("TreasureHunter", "patchcount: " + patchCount);
    }

}
