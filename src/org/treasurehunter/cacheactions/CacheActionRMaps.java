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

package org.treasurehunter.cacheactions;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import java.util.ArrayList;

/** Open with application "RMaps", that can be used with offline maps */
public class CacheActionRMaps extends StaticLabelCache implements CacheAction {
    private final Context mContext;

    private final ErrorDisplayer mErrorDisplayer;

    public CacheActionRMaps(Resources resources, Context context, ErrorDisplayer errorDisplayer) {
        super(resources, R.string.cache_action_rmaps);
        mContext = context;
        mErrorDisplayer = errorDisplayer;
    }

    @Override
    public void act(Geocache geocache, Waypoint waypoint) {
        Intent intent = new Intent();
        intent.setAction("com.robert.maps.action.SHOW_POINTS");
        GeoObject geoObject = (waypoint != null ? waypoint : geocache);
        String encoded = geoObject.getLatitude() + "," + geoObject.getLongitude() + ";"
                + geoObject.getId() + ";" + geoObject.getName();
        ArrayList<String> locations = new ArrayList<String>();
        locations.add(encoded);
        intent.putStringArrayListExtra("locations", locations);
        try {
            mContext.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            mErrorDisplayer.displayError(R.string.error2, e.getMessage(),
                    mContext.getString(R.string.error_activity_not_found));
        }
    }
}
