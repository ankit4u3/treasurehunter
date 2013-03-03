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

import android.database.Cursor;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

//TODO: GeocacheFactory is used by multiple threads but mGeocaches is not thread-safe
/**
 * Creates new Geocache objects and caches all created instances so there always
 * is at most one object for each geocache. Waypoints are not cached.
 */
public class GeocacheFactory {
    public static enum Provider {
        ATLAS_QUEST(0, "LB"), GROUNDSPEAK(1, "GC"), MY_LOCATION(-1, "ML"), OPENCACHING(2, "OC"), TERRACACHING(
                3, "TC"), GEOCACHINGAU_GA(4, "GA"), GEOCACHINGAU_TP(5, "TP");

        private final int mIx;

        private final String mPrefix;

        Provider(int ix, String prefix) {
            mIx = ix;
            mPrefix = prefix;
        }

        public int toInt() {
            return mIx;
        }

        public String getPrefix() {
            return mPrefix;
        }
    }

    public static Provider ALL_PROVIDERS[] = {
            Provider.ATLAS_QUEST, Provider.GROUNDSPEAK, Provider.MY_LOCATION, Provider.OPENCACHING,
            Provider.TERRACACHING, Provider.GEOCACHINGAU_GA, Provider.GEOCACHINGAU_TP
    };

    private static GeocacheTypeFactory mCacheTypeFactory;

    private static SourceFactory mSourceFactory;

    public GeocacheFactory() {
        mSourceFactory = new SourceFactory();
        mCacheTypeFactory = new GeocacheTypeFactory();
    }

    public GeocacheType cacheTypeFromInt(int cacheTypeIx) {
        return mCacheTypeFactory.fromInt(cacheTypeIx);
    }

    public Source sourceFromString(String uniqueSource) {
        return mSourceFactory.fromString(uniqueSource);
    }

    public SourceFactory getSourceFactory() {
        return mSourceFactory;
    }

    /** Mapping from cacheId to Geocache for all loaded geocaches */
    private HashMap<CharSequence, Geocache> mGeocaches = new HashMap<CharSequence, Geocache>();

    /** @return the geocache if it is already loaded, otherwise null */
    public Geocache getFromId(CharSequence id) {
        return mGeocaches.get(id);
    }

    /**
     * Creates a new Geocache and caches the object. Assumes an object with this
     * geocache id isn't already cached.
     */
    public Geocache create(CharSequence id, CharSequence name, double latitude, double longitude,
            Source source, GeocacheType cacheType, int difficulty, int terrain, int container) {
        if (name == null)
            name = "";

        Geocache geocache = new Geocache(id, name, latitude, longitude, source, cacheType,
                difficulty, terrain, container);
        mGeocaches.put(id, geocache);
        return geocache;
    }

    Calendar cal;

    public Geocache createshort(CharSequence id, CharSequence name, double latitude,
            double longitude) {
        if (name == null)
            name = "Imported";
        if (id == null)
            cal = Calendar.getInstance();
        Date time = cal.getTime();
        id = String.format("ML%1$tk%1$tM%1$tS", time);

        Geocache geocache = new Geocache(id, name, latitude, longitude, Source.MY_LOCATION,
                GeocacheType.EARTHCACHE, 0, 0, 0);
        mGeocaches.put(id, geocache);
        return geocache;
    }

    /**
     * Remove all cached geocache instances. Future references will reload from
     * the database.
     */
    public void flushCache() {
        mGeocaches.clear();
    }

    public void removeTagFromAllCaches(int tag) {
        for (Geocache geocache : mGeocaches.values()) {
            geocache.updateCachedTag(tag, false);
            geocache.flushIcons();
        }
    }

    public void flushCacheTagsAndIcons() {
        for (Geocache geocache : mGeocaches.values()) {
            geocache.flushIcons();
            geocache.flushTags();
        }
    }

    /**
     * Forces the geocache to be reloaded from the database the next time it is
     * needed.
     */
    public void flushGeocache(CharSequence geocacheId) {
        mGeocaches.remove(geocacheId.toString());
    }

    public Geocache fromCursor(Cursor cursor) {
        String id = cursor.getString(2);
        Geocache geocache = mGeocaches.get(id);
        if (geocache != null)
            return geocache;

        Source source = mSourceFactory.fromString(cursor.getString(4));
        GeocacheType cacheType = cacheTypeFromInt(Integer.parseInt(cursor.getString(5)));
        int difficulty = Integer.parseInt(cursor.getString(6));
        int terrain = Integer.parseInt(cursor.getString(7));
        int container = Integer.parseInt(cursor.getString(8));
        return create(id, cursor.getString(3), cursor.getDouble(0), cursor.getDouble(1), source,
                cacheType, difficulty, terrain, container);
    }

    private String getString(String colName, Cursor c) {
        // Log.d("TreasureHunter", "Getting column "+ colName);
        return c.getString(c.getColumnIndex(colName));
    }

    private double getDouble(String colName, Cursor c) {
        return c.getDouble(c.getColumnIndex(colName));
    }

    public Waypoint waypointFromCursor(Cursor cursor) {
        Waypoint waypoint = new Waypoint(getString("Id", cursor), getString("Name", cursor),
                getDouble("Latitude", cursor), getDouble("Longitude", cursor),
                sourceFromString(getString("Source", cursor)),
                cacheTypeFromInt(Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex("CacheType")))), getString("ParentId", cursor));
        return waypoint;
    }

    public Waypoint createWaypoint(CharSequence id, CharSequence name, double latitude,
            double longitude, Source source, GeocacheType cacheType, CharSequence parent) {
        Waypoint waypoint = new Waypoint(id, name, latitude, longitude, source, cacheType, parent);
        return waypoint;
    }

    public void flushGeocacheTags(CharSequence id) {
        Geocache cached = mGeocaches.get(id);
        if (cached == null)
            return;
        cached.flushTags();
    }
}
