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

package org.treasurehunter.activity.cachelist;

import android.content.SharedPreferences;

public class DistanceFormatter {
    private CharSequence formatDistanceMetric(float distance) {
        if (distance == -1) {
            return "";
        }
        if (distance >= 1000) {
            return String.format("%1$1.2fkm", distance / 1000.0);
        }
        return String.format("%1$dm", (int)distance);
    }

    private CharSequence formatDistanceImperial(float distance) {
        if (distance == -1) {
            return "";
        }
        final float miles = distance / 1609.344f;
        if (miles > 0.1)
            return String.format("%1$1.2fmi", miles);
        final int feet = (int)(miles * 5280);
        return String.format("%1$1dft", feet);
    }

    private final SharedPreferences mDefaultSharedPreferences;

    private boolean mImperial = false;

    public DistanceFormatter(SharedPreferences defaultSharedPreferences) {
        mDefaultSharedPreferences = defaultSharedPreferences;
    }

    public CharSequence formatDistance(float distance) {
        if (mImperial) {
            return formatDistanceImperial(distance);
        } else {
            return formatDistanceMetric(distance);
        }
    }

    public void updateFormatter() {
        mImperial = mDefaultSharedPreferences.getBoolean("imperial", false);
    }
}
