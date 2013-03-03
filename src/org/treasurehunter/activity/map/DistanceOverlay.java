
package org.treasurehunter.activity.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import org.treasurehunter.GeoFix;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeoObject;
import org.treasurehunter.activity.cachelist.DistanceFormatter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;

public class DistanceOverlay extends Overlay {

    private GeoObject mTarget;

    private GeoFixProvider mGeoFixProvider;

    private DistanceFormatter mFormatter;

    private boolean mActive = true;

    private final Paint mPaint = new Paint();

    private Paint mTextPaint;

    public DistanceOverlay(GeoFixProvider geoFixProvider, DistanceFormatter formatter) {
        mGeoFixProvider = geoFixProvider;
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(30);
        mPaint.setStrokeWidth(3);
        mPaint.setTextSize(12);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.FILL_AND_STROKE);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAlpha(150);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mFormatter = formatter;
        mFormatter.updateFormatter();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (!mActive || mTarget == null)
            return;
        GeoFix location = mGeoFixProvider.getLocation();
        if (location.equals(GeoFix.NO_FIX))
            return;
        float results[] = {
            0
        };
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                mTarget.getLatitude(), mTarget.getLongitude(), results);
        float distance = results[0];

        mPaint.setColor(getColorForDistance(distance, 2000));

        Point start = new Point();
        Point stop = new Point();

        mapView.getProjection().toPixels(toGeoPoint(location), start);
        mapView.getProjection().toPixels(toGeoPoint(mTarget), stop);

        canvas.drawLine(start.x, start.y, stop.x, stop.y, mPaint);

        canvas.drawOval(rectByCenter(start.x, start.y + 15, 50, 20), mPaint);
        // canvas.drawCircle(start.x, start.y + 15, 15, bg);
        CharSequence distanceString = mFormatter.formatDistance(distance);
        canvas.drawText(distanceString.toString(), start.x, start.y + 20, mTextPaint);

        // Log.d("TreasureHunter","Distance overlay draw called "+distance);
    }

    private RectF rectByCenter(int x, int y, int width, int height) {
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        return new RectF(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
    }

    void setGeoObject(GeoObject newTarget) {
        mTarget = newTarget;
    }

    private int toIE6(double coord) {
        return (int)(coord * 1000000);
    }

    private GeoPoint toGeoPoint(GeoFix geoFix) {
        return new GeoPoint(toIE6(geoFix.getLatitude()), toIE6(geoFix.getLongitude()));
    }

    private GeoPoint toGeoPoint(GeoObject geoObject) {
        return new GeoPoint(toIE6(geoObject.getLatitude()), toIE6(geoObject.getLongitude()));
    }

    private int getColorForDistance(float distance, float maxDistance) {
        int red = 0, green = 0, blue = 0;
        float half = maxDistance / 2;
        if (distance > maxDistance)
            red = 255;
        else if (distance > half) {
            green = (int)(255 - ((distance - half) / half * 255));
            red = 255;
        } else {
            green = 255;
            red = (int)((distance) / half * 255);
        }
        // Log.d("TreasureHunter",
        // "Red: "+red+", Green: "+green+", dist: "+distance);
        return Color.rgb(red, green, blue);
    }

    public void updatePreferences() {
        mFormatter.updateFormatter();
    }

    public void setActive(boolean active) {
        this.mActive = active;
        // Log.i("TreasureHunter", "Distance overlay toggle: "+active);
    }

    public boolean isActive() {
        return mActive;
    }
}
