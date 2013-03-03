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

public enum GeocacheType {
    NULL(0, R.drawable.cache_default, R.drawable.cache_default_big, R.drawable.pin_default, "null",
            "Generic"), MULTI(2, R.drawable.multi_clean_32, R.drawable.multi_clean_80,
            R.drawable.multi_pin, "multi", "Multi-stage"), TRADITIONAL(1,
            R.drawable.traditional_clean_32, R.drawable.traditional_clean_80,
            R.drawable.traditional_pin, "traditional", "Traditional"), UNKNOWN(3,
            R.drawable.unknown_clean_32, R.drawable.unknown_clean_80, R.drawable.unknown_pin,
            "unknown", "Mystery"), MY_LOCATION(4, R.drawable.blue_dot, R.drawable.blue_dot,
            R.drawable.blue_dot, "my location", "My location"),

    EARTHCACHE(5, R.drawable.earthcache_clean_32, R.drawable.earthcache_clean_80,
            R.drawable.earthcache_pin, "earth", "Earthcache"), VIRTUAL(6,
            R.drawable.virtual_clean_32, R.drawable.virtual_clean_80, R.drawable.virtual_pin,
            "virtual", "Virtual"), LETTERBOX_HYBRID(7, R.drawable.letter_clean_32,
            R.drawable.letter_clean_80, R.drawable.letter_pin, "letterbox", "Letterbox"), EVENT(8,
            R.drawable.event_clean_32, R.drawable.event_clean_80, R.drawable.event_pin, "event",
            "Event"), WEBCAM(9, R.drawable.webcam_clean_32, R.drawable.webcam_clean_80,
            R.drawable.webcam_pin, "webcam", "Webcam"),

    CITO(10, R.drawable.cito_clean_32, R.drawable.cito_clean_80, R.drawable.pin_default,
            "cache in trash out", "Cache In Trash Out"), LOCATIONLESS(11, R.drawable.cache_default,
            R.drawable.cache_default_big, R.drawable.pin_default, "reverse", "Reverse"), APE(12,
            R.drawable.cache_default, R.drawable.cache_default_big, R.drawable.pin_default,
            "project ape", "Project Ape"), MEGA(13, R.drawable.mega_clean_32,
            R.drawable.mega_clean_80, R.drawable.mega_pin, "mega-event", "Mega-event"), WHERIGO(14,
            R.drawable.cache_default, R.drawable.cache_default_big, R.drawable.pin_default,
            "wherigo", "Wherigo"),

    // Waypoint types
    WAYPOINT(20, R.drawable.cache_default, R.drawable.cache_default_big, R.drawable.blue_dot,
            "waypoint", "Waypoint", true), // Not actually seen in GPX...
    WAYPOINT_PARKING(21, R.drawable.cache_waypoint_p, R.drawable.cache_waypoint_p_big,
            R.drawable.map_pin2_wp_p, "waypoint|parking area", "Parking area", true), WAYPOINT_REFERENCE(
            22, R.drawable.cache_waypoint_r, R.drawable.cache_waypoint_r_big,
            R.drawable.map_pin2_wp_r, "waypoint|reference point", "Reference point", true), WAYPOINT_STAGES(
            23, R.drawable.cache_waypoint_s, R.drawable.cache_waypoint_s_big,
            R.drawable.map_pin2_wp_s, "waypoint|stages of a multicache", "Multi-cache stage", true), WAYPOINT_TRAILHEAD(
            24, R.drawable.cache_waypoint_t, R.drawable.cache_waypoint_t_big,
            R.drawable.map_pin2_wp_t, "waypoint|trailhead", "Trailhead", true), WAYPOINT_FINAL(25,
            R.drawable.cache_waypoint_r, R.drawable.cache_waypoint_r_big, R.drawable.map_pin2_wp_r,
            "waypoint|final location", "Final location", true); // TODO: Doesn't
                                                                // have unique
                                                                // graphics yet

    private final int mIconId;

    private final int mIconIdBig;

    private final int mIx;

    private final int mIconIdMap;

    private final String mTag;

    /** The text to describe the cache type to the user */
    private final String mLabel;

    private final boolean mIsWaypoint;

    GeocacheType(int ix, int drawableId, int drawableIdBig, int drawableIdMap, String tag,
            String label) {
        this(ix, drawableId, drawableIdBig, drawableIdMap, tag, label, false);

    }

    GeocacheType(int ix, int drawableId, int drawableIdBig, int drawableIdMap, String tag,
            String label, boolean isWaypoint) {
        mIx = ix;
        mIconId = drawableId;
        mIconIdBig = drawableIdBig;
        mIconIdMap = drawableIdMap;
        mTag = tag;
        mLabel = label;
        mIsWaypoint = isWaypoint;
    }

    public int icon() {
        return mIconId;
    }

    public int iconBig() {
        return mIconIdBig;
    }

    public int toInt() {
        return mIx;
    }

    public int iconMap() {
        return mIconIdMap;
    }

    public String getTag() {
        return mTag;
    }

    public String getLabel() {
        return mLabel;
    }

    public boolean isWaypoint() {
        return mIsWaypoint;
    }
}
