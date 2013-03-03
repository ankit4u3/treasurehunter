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

import org.treasurehunter.R;
import org.treasurehunter.menuactions.MenuAction;

public class MenuActionToggleSatellite implements MenuAction {
    private final MapView mMapView;

    public MenuActionToggleSatellite(MapView mapView) {
        mMapView = mapView;
    }

    @Override
    public void act() {
        mMapView.setSatellite(!mMapView.isSatellite());
    }

    @Override
    public String getLabel() {
        int stringId = mMapView.isSatellite() ? R.string.map_view : R.string.menu_toggle_satellite;
        return mMapView.getResources().getString(stringId);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
