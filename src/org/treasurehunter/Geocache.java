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

import org.treasurehunter.GeocacheFactory.Provider;
import org.treasurehunter.database.DbFrontend;

import java.util.List;

/**
 * Geocache or letterbox description, id, and coordinates.
 */
public class Geocache extends GeoObject {
    public static final String TreasureHunter_DIR = "/sdcard/TreasureHunter";

    public static final String ID = "geocacheId";

    public static final String WAYPOINTID = "waypointId";

    public static final String NAVIGATE_TO_NEW_CACHE = "navToNewCache";

    private final int mContainer;

    /** Difficulty rating * 2 (difficulty=1.5 => mDifficulty=3) */
    private final int mDifficulty;

    private final int mTerrain;

    private List<Integer> mTags = null;

    // private List<GeoObject> mRelatedWaypoints = null;

    public Geocache(CharSequence id, CharSequence name, double latitude, double longitude,
            Source source, GeocacheType cacheType, int difficulty, int terrain, int container) {
        super(id, name, latitude, longitude, source, cacheType);
        mDifficulty = difficulty;
        mTerrain = terrain;
        mContainer = container;
    }

    public List<Integer> getTags(DbFrontend dbFrontend) {
        if (mTags == null)
            mTags = dbFrontend.getGeocacheTags(mId);
        return mTags;
    }

    public void flushTags() {
        mTags = null;
    }

    public void updateCachedTag(int tag, boolean set) {
        if (mTags == null)
            return;
        if (set)
            mTags.add(tag);
        else
            mTags.remove(Integer.valueOf(tag));
    }

    /*
     * public float[] calculateDistanceAndBearing(Location here) { if (here !=
     * null) { Location.distanceBetween(here.getLatitude(), here.getLongitude(),
     * getLatitude(), getLongitude(), mDistanceAndBearing); return
     * mDistanceAndBearing; } mDistanceAndBearing[0] = -1;
     * mDistanceAndBearing[1] = -1; return mDistanceAndBearing; }
     */

    public int getContainer() {
        return mContainer;
    }

    public GeocacheFactory.Provider getContentProvider() {
        // Must use toString() rather than mId.subSequence(0,2).equals("GC"),
        // because editing the text in android produces a SpannableString rather
        // than a String, so the CharSequences won't be equal.
        String prefix = mId.subSequence(0, 2).toString();
        for (Provider provider : GeocacheFactory.ALL_PROVIDERS) {
            if (prefix.equals(provider.getPrefix()))
                return provider;
        }
        return Provider.GROUNDSPEAK;
    }

    public int getDifficulty() {
        return mDifficulty;
    }

    public CharSequence getFormattedAttributes() {
        if (mDifficulty == 0 && mTerrain == 0)
            return "";
        if (mDifficulty == 0)
            return "? / " + (mTerrain / 2.0);
        if (mTerrain == 0)
            return (mDifficulty / 2.0) + " / ?";
        return (mDifficulty / 2.0) + " / " + (mTerrain / 2.0);
    }

    public int getTerrain() {
        return mTerrain;
    }

    public boolean hasTag(int tag, DbFrontend dbFrontend) {
        List<Integer> tags = getTags(dbFrontend);
        return tags.contains(tag);
    }
}
