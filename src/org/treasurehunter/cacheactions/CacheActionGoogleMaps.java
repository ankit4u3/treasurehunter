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

import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import java.net.URLEncoder;
import java.util.Locale;

public class CacheActionGoogleMaps extends StaticLabelCache implements CacheAction {
    private final Resources mResources;

    private final Context mContext;

    public CacheActionGoogleMaps(Resources resources, Context context) {
        super(resources, R.string.menu_google_maps);
        mResources = resources;
        mContext = context;
    }

    @Override
    public void act(Geocache cache, Waypoint waypoint) {
        String uri;
        if (waypoint != null)
            uri = convert(waypoint);
        else
            uri = convert(cache);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mContext.startActivity(intent);
    }

    private String convert(GeoObject geocache) {
        // "geo:%1$.5f,%2$.5f?name=cachename"

        String idAndName;
        if (geocache.getId().equals(""))
            idAndName = geocache.getName().toString();
        else if (geocache.getName().equals(""))
            idAndName = geocache.getId().toString();
        else
            idAndName = geocache.getId().toString() + ": " + geocache.getName().toString();

        idAndName = idAndName.replace("(", "[");
        idAndName = idAndName.replace(")", "]");
        idAndName = URLEncoder.encode(idAndName);
        final String format = mResources.getString(R.string.map_intent);
        final String uri = String.format(Locale.US, format, geocache.getLatitude(),
                geocache.getLongitude(), idAndName);
        return uri;
    }

}
