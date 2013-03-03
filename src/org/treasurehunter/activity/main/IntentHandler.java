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

package org.treasurehunter.activity.main;

import org.treasurehunter.Clock;
import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.Source;
import org.treasurehunter.Tags;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;

import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.util.Log;

/** Handles the intents that the application receives */
public class IntentHandler {
    private final GeocacheFactory mGeocacheFactory;

    private final DbFrontend mDbFrontend;

    private final GuiState mGuiState;

    IntentHandler(GuiState guiState, GeocacheFactory geocacheFactory, DbFrontend dbFrontend) {
        mGuiState = guiState;
        mGeocacheFactory = geocacheFactory;
        mDbFrontend = dbFrontend;
    }

    public void handleIntent(Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            Log.d("TreasureHunter", "IntentHandler.handleIntent(): No action");
            return;
        }
        boolean intentHandled = false;
        if (action.equals(Intent.ACTION_VIEW) && intent.getType() == null) {
            Geocache geocache = (Geocache)getGeocacheFromMapsIntent(intent);
            if (geocache != null) {
                Log.d("TreasureHunter",
                        "IntentHandler.handleIntent(): Showing maps intent geocache "
                                + geocache.getId());
                mGuiState.showCompass(geocache);
                intentHandled = true;
            } else {
                Log.e("TreasureHunter",
                        "IntentHandler.handleIntent(): Could not load geocache from "
                                + intent.getData());
            }
        }

        if (!intentHandled) {
            Log.d("TreasureHunter", "IntentHandler.handleIntent(): Unknown intent action=" + action);
        }
    }

    private GeoObject getGeocacheFromMapsIntent(Intent intent) {
        final String query = intent.getData().getQuery();
        if (query == null || query.equals(""))
            return null;
        final CharSequence sanitizedQuery = Util.parseHttpUri(query, new UrlQuerySanitizer(),
                UrlQuerySanitizer.getAllButNulAndAngleBracketsLegal());
        if (sanitizedQuery == null)
            return null;
        final CharSequence[] latlon = Util.splitLatLonDescription(sanitizedQuery);
        CharSequence id = latlon[2];
        if (id.length() < 2) {
            // ID is missing for waypoints imported from the browser; create a
            // new id from the time.
            id = String.format("WP%1$tk%1$tM%1$tS", System.currentTimeMillis());
        }
        double latitude = Util.parseCoordinate(latlon[0]);
        double longitude = Util.parseCoordinate(latlon[1]);

        Log.d("TreasureHunter", "getGeocacheFromMapsIntent id=" + id);
        GeoObject oldGeocache = mDbFrontend.loadCacheFromId(id);
        boolean overwrite = true;
        if (oldGeocache == null) {
            overwrite = true;
        } else if (mDbFrontend.geocacheHasTag(id, Tags.LOCKED_FROM_OVERWRITING)) {
            overwrite = false;
        } else if (Util.approxEquals(latitude, oldGeocache.getLatitude(), 5e-6)
                && Util.approxEquals(longitude, oldGeocache.getLongitude(), 5e-6)) {
            overwrite = false;
        }
        if (overwrite) {
            final Geocache g = mGeocacheFactory.create(id, latlon[3], latitude, longitude,
                    Source.WEB_URL, GeocacheType.NULL, 0, 0, 0);
            CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
            cacheWriter.beginTransaction();
            String now = Clock.getCurrentStringTime();
            cacheWriter.insertAndUpdateCache(g.getId(), g.getName(), g.getLatitude(),
                    g.getLongitude(), g.getSource(), g.getCacheType(), g.getDifficulty(),
                    g.getTerrain(), g.getContainer(), now);
            cacheWriter.endTransaction();

            return g;
        }

        return oldGeocache;
    }

}
