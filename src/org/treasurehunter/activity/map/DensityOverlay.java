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

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import org.treasurehunter.activity.map.DensityMatrix.DensityPatch;

import android.graphics.Canvas;

import java.util.List;

public class DensityOverlay extends Overlay {
    // Create delegate because it's not possible to test classes that extend
    // Android classes.

    private DensityOverlayDelegate mDelegate;

    public DensityOverlay(DensityOverlayDelegate densityOverlayDelegate) {
        mDelegate = densityOverlayDelegate;
    }

    public void setPatches(List<DensityPatch> patches) {
        mDelegate.setPatches(patches);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
        mDelegate.draw(canvas, mapView, shadow);
    }
}
