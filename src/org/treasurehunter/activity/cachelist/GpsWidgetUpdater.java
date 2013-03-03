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

import org.treasurehunter.Clock;

import android.os.Handler;

public class GpsWidgetUpdater implements Runnable {
    private final GpsStatusWidgetDelegate mGpsStatusWidgetDelegate;

    private final Clock mClock;

    private final Handler mHandler = new Handler();

    private boolean mIsAborted = false;

    public GpsWidgetUpdater(GpsStatusWidgetDelegate gpsStatusWidgetDelegate, Clock clock) {
        mGpsStatusWidgetDelegate = gpsStatusWidgetDelegate;
        mClock = clock;
    }

    public void resume() {
        mIsAborted = false;
        run();
    }

    @Override
    public void run() {
        if (mIsAborted) {
            return;
        }
        // Update the lag time and the orientation.
        long systemTime = mClock.getCurrentTime();
        mGpsStatusWidgetDelegate.updateLagText(systemTime);
        mHandler.postDelayed(this, 500);
    }

    public void abort() {
        mIsAborted = true;
    }
}
