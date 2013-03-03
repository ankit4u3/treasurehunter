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

package org.treasurehunter.activity.compass;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.Geocache;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;
import org.treasurehunter.cacheactions.CacheAction;

import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;

public class CacheButtonOnClickListener implements OnClickListener {
    private final CacheAction mCacheAction;

    private final ErrorDisplayer mErrorDisplayer;

    private final String mActivityNotFoundErrorMessage;

    private final CompassTab mCompassDelegate;

    private final SharedPreferences mSharedPreferences;

    public CacheButtonOnClickListener(CacheAction cacheAction, CompassTab compassDelegate,
            String errorMessage, ErrorDisplayer errorDisplayer, SharedPreferences sharedPreferences) {
        mCacheAction = cacheAction;
        mCompassDelegate = compassDelegate;
        mErrorDisplayer = errorDisplayer;
        mActivityNotFoundErrorMessage = errorMessage;
        mSharedPreferences = sharedPreferences;
    }

    @Override
    public void onClick(View view) {
        if (!mSharedPreferences.getBoolean("ui_tap_opens_radar", true))
            return;
        Geocache geocache = mCompassDelegate.getActiveGeocache();
        Waypoint waypoint = mCompassDelegate.getActiveWaypoint();
        try {
            mCacheAction.act(geocache, waypoint);
        } catch (final ActivityNotFoundException e) {
            mErrorDisplayer.displayError(R.string.error2, e.getMessage(),
                    mActivityNotFoundErrorMessage);
        } catch (final Exception e) {
            mErrorDisplayer.displayError(R.string.error1, e.getMessage());
        }
    }
}
