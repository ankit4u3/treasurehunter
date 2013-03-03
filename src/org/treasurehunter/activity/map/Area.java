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

import org.treasurehunter.activity.compass.Util;

public class Area {
    public double mLatLow;

    public double mLonLow;

    public double mLatHigh;

    public double mLonHigh;

    boolean contains(Area area) {
        return (mLatLow <= area.mLatLow && mLonLow <= area.mLonLow && mLatHigh >= area.mLatHigh && mLonHigh >= area.mLonHigh);
    }

    public Area expand(float expandRatio) {
        Area newArea = new Area();
        double latExpand = (mLatHigh - mLatLow) * expandRatio / 2.0;
        double lonExpand = (mLonHigh - mLonLow) * expandRatio / 2.0;
        newArea.mLatLow = mLatLow - latExpand;
        newArea.mLonLow = mLonLow - lonExpand;
        newArea.mLatHigh = mLatHigh + latExpand;
        newArea.mLonHigh = mLonHigh + lonExpand;
        return newArea;
    }

    public boolean equals(Area area) {
        return (Util.approxEquals(mLatLow, area.mLatLow)
                && Util.approxEquals(mLonLow, area.mLonLow)
                && Util.approxEquals(mLatHigh, area.mLatHigh) && Util.approxEquals(mLonHigh,
                area.mLonHigh));
    }
}
