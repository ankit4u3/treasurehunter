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

import java.util.HashMap;
import java.util.Map;

public class Tags {
    /** This id must never occur in the database */
    public final static int NULL = 0;

    public final static int FOUND = 1;

    public final static int DNF = 2;

    public final static int FAVORITE = 3;

    // These attributes are actually not related to the specific user
    public final static int UNAVAILABLE = 4;

    public final static int ARCHIVED = 5;

    /** The cache is newly added */
    public final static int NEW = 6;

    /** The user placed this cache */
    public final static int MINE = 7;

    /**
     * Indicates that the user has edited the cache. Don't overwrite when
     * importing a newer GPX.
     */
    public final static int LOCKED_FROM_OVERWRITING = 9;

    /**
     * Indicates that the geocache is registered to contain one or more
     * travelbugs or geocoins.
     * 
     * @deprecated The total list of travelbugs is saved instead from database v
     *             15
     */
    @Deprecated
    public final static int CONTAINS_TRAVELBUG = 10;

    // This method will be refactored into a more elegant solution
    public static Map<Integer, String> GetAllTags() {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        map.put(FOUND, "Found");
        map.put(DNF, "Did Not Find");
        map.put(FAVORITE, "Favorite");
        map.put(NEW, "New");
        map.put(MINE, "Mine");
        map.put(UNAVAILABLE, "Unavailable");
        map.put(LOCKED_FROM_OVERWRITING, "Locked from overwriting");
        return map;
    }
}
