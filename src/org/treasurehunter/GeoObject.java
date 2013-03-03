
package org.treasurehunter;

import com.google.android.maps.GeoPoint;

import org.treasurehunter.activity.compass.GeoUtils;

import android.graphics.drawable.Drawable;
import android.util.FloatMath;

/** Base class of Geocache and Waypoint */
public abstract class GeoObject {

    public GeoObject(CharSequence id, CharSequence name, double latitude, double longitude,
            Source source, GeocacheType cacheType) {
        mId = id;
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mSource = source;
        mCacheType = cacheType;
    }

    protected final GeocacheType mCacheType;

    private GeoPoint mGeoPoint;

    protected final CharSequence mId;

    protected final double mLatitude;

    protected final double mLongitude;

    protected final CharSequence mName;

    protected final Source mSource;

    private Drawable mIcon = null;

    private Drawable mIconMap = null;

    public GeocacheType getCacheType() {
        return mCacheType;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public Drawable getIconMap() {
        return mIconMap;
    }

    public void setIconMap(Drawable iconMap) {
        mIconMap = iconMap;
    }

    public float getDistanceTo(double latitude, double longitude) {
        double dLat = Math.toRadians(latitude - mLatitude);
        double dLon = Math.toRadians(longitude - mLongitude);
        final float sinDLat = FloatMath.sin((float)(dLat / 2));
        final float sinDLon = FloatMath.sin((float)(dLon / 2));
        float a = sinDLat * sinDLat + FloatMath.cos((float)Math.toRadians(mLatitude))
                * FloatMath.cos((float)Math.toRadians(latitude)) * sinDLon * sinDLon;
        float c = (float)(2 * Math.atan2(FloatMath.sqrt(a), FloatMath.sqrt(1 - a)));
        return 6371000 * c;
    }

    public GeoPoint getGeoPoint() {
        if (mGeoPoint == null) {
            int latE6 = (int)(mLatitude * GeoUtils.MILLION);
            int lonE6 = (int)(mLongitude * GeoUtils.MILLION);
            mGeoPoint = new GeoPoint(latE6, lonE6);
        }
        return mGeoPoint;
    }

    public CharSequence getId() {
        return mId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public CharSequence getName() {
        return mName;
    }

    public Source getSource() {
        return mSource;
    }

    /** The icons will be recalculated the next time they are needed. */
    public void flushIcons() {
        mIcon = null;
        mIconMap = null;
    }
}
