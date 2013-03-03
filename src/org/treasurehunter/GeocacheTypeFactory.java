
package org.treasurehunter;

import java.util.Hashtable;

public class GeocacheTypeFactory {
    private final Hashtable<Integer, GeocacheType> mCacheTypes = new Hashtable<Integer, GeocacheType>(
            GeocacheType.values().length);

    public GeocacheTypeFactory() {
        for (GeocacheType cacheType : GeocacheType.values())
            mCacheTypes.put(cacheType.toInt(), cacheType);
    }

    public GeocacheType fromInt(int i) {
        if (!mCacheTypes.containsKey(i))
            return GeocacheType.NULL;
        return mCacheTypes.get(i);
    }

    public GeocacheType fromTag(String tag) {
        String tagLower = tag.toLowerCase();
        int longestMatch = 0;

        GeocacheType result = GeocacheType.NULL;
        for (GeocacheType cacheType : mCacheTypes.values()) {
            if (tagLower.contains(cacheType.getTag()) && cacheType.getTag().length() > longestMatch) {
                result = cacheType;
                longestMatch = cacheType.getTag().length();
                // Necessary to continue the search to find mega-events and
                // individual waypoint types.
            }
        }

        return result;
    }

    public static int container(String container) {
        if (container.equals("Micro")) {
            return 1;
        } else if (container.equals("Small")) {
            return 2;
        } else if (container.equals("Regular")) {
            return 3;
        } else if (container.equals("Large")) {
            return 4;
        }
        return 0;
    }

    public static String containerFromInt(int container) {
        switch (container) {
            case 1:
                return "Micro";
            case 2:
                return "Small";
            case 3:
                return "Regular";
            case 4:
                return "Large";
        }
        return "Unknown";
    }

    /** Used to translate difficulty and terrain */
    public static int stars(String stars) {
        try {
            return Math.round(Float.parseFloat(stars) * 2);
        } catch (Exception ex) {
            return 0;
        }
    }
}
