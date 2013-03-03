
package org.treasurehunter;

public class Waypoint extends GeoObject {
    public static final String ID = "waypointId";

    private final CharSequence mParentCache;

    /**
     * @param id
     * @param name
     * @param latitude
     * @param longitude
     * @param source
     * @param cacheType
     * @param parent May be null if waypoint does not have a natural parent. For
     *            example user entered "My location"
     */
    public Waypoint(CharSequence id, CharSequence name, double latitude, double longitude,
            Source source, GeocacheType cacheType, CharSequence parent) {
        super(id, name, latitude, longitude, source, cacheType);
        mParentCache = parent;
    }

    public CharSequence getParentCache() {
        return mParentCache;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Waypoint))
            return false;
        return this.getId().equals(((Waypoint)o).getId());
    }

    @Override
    public String toString() {
        return this.getId() + " " + this.getName();
    }
}
