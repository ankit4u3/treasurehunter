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

package org.treasurehunter;

import android.text.format.DateFormat;
import android.util.Log;

public class Clock {
    /**
     * Returns the current system time in milliseconds since January 1, 1970
     * 00:00:00 UTC
     */
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static String getCurrentStringTime() {
        String time = (String)DateFormat.format("yyyy-MM-dd kk:mm:ss", System.currentTimeMillis());
        return time;
    }

    public static String timeToString(long time) {
        return (String)DateFormat.format("yyyy-MM-dd kk:mm:ss", time);
    }

    private long mStartTime;

    public void reset() {
        mStartTime = getCurrentTime();
    }

    public long getElapsed() {
        return System.currentTimeMillis() - mStartTime;
    }

    /** Prints the time elapsed since last reset() - use a %l in format */
    public void printElapsed(String format) {
        long elapsed = getCurrentTime() - mStartTime;
        Log.d("TreasureHunter", String.format(format, elapsed));
    }
}
