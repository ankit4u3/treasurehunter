//http://www.spectrekking.com/download/FixedMyLocationOverlay.java

package org.treasurehunter.activity.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.R;
import org.treasurehunter.Refresher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;

public class FixedMyLocationOverlay extends MyLocationOverlay implements Refresher {
    private boolean bugged = false;

    private Paint accuracyPaint;

    private Point center;

    private Point left;

    private Drawable drawable;

    private int width;

    private int height;

    private GeoFixProvider geoFixProvider;

    private float heading = -1000;

    private Bitmap arrowBMP;

    private SharedPreferences mSharedPreferences;

    public FixedMyLocationOverlay(Context context, MapView mapView, GeoFixProvider geoFixProvider,
            SharedPreferences sharedPreferences) {
        super(context, mapView);
        arrowBMP = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);
        this.geoFixProvider = geoFixProvider;
        mSharedPreferences = sharedPreferences;
        geoFixProvider.addObserver(this);
    }

    @Override
    public void forceRefresh() {
        heading = geoFixProvider.getAzimuth();
    }

    @Override
    public void refresh() {
        forceRefresh();
    };

    @Override
    protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc,
            long when) {

        boolean useLocationArrow = mSharedPreferences.getBoolean("use-location-arrow", false);

        if (!bugged) {
            try {
                super.drawMyLocation(canvas, mapView, lastFix, myLoc, when);
            } catch (Exception e) {
                bugged = true;
            }
        }

        if (useLocationArrow) { // Draw the direction we are heading
            drawable = mapView.getContext().getResources().getDrawable(R.drawable.mylocation);
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
            center = new Point();
            left = new Point();

            Projection projection = mapView.getProjection();

            double latitude = lastFix.getLatitude();
            double longitude = lastFix.getLongitude();
            float accuracy = lastFix.getAccuracy();

            float[] result = new float[1];

            Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
            float longitudeLineDistance = result[0];

            GeoPoint leftGeo = new GeoPoint((int)(latitude * 1e6), (int)((longitude - accuracy
                    / longitudeLineDistance) * 1e6));
            projection.toPixels(leftGeo, left);
            projection.toPixels(myLoc, center);

            if (heading != -1000) { // We has a bearing
                Matrix matrix = new Matrix();
                matrix.setRotate(heading, arrowBMP.getWidth() / 2, arrowBMP.getHeight() / 2);
                matrix.postTranslate(center.x - arrowBMP.getWidth() / 2f,
                        center.y - arrowBMP.getHeight() / 2f);
                canvas.drawBitmap(arrowBMP, matrix, null);
            }
        }

        if (bugged) {
            if (drawable == null) {
                accuracyPaint = new Paint();
                accuracyPaint.setAntiAlias(true);
                accuracyPaint.setStrokeWidth(2.0f);

            }

            int radius = center.x - left.x;
            accuracyPaint.setColor(0xff6666ff);
            accuracyPaint.setStyle(Style.STROKE);
            canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

            accuracyPaint.setColor(0x186666ff);
            accuracyPaint.setStyle(Style.FILL);
            canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

            drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2,
                    center.y + height / 2);
            drawable.draw(canvas);
        }

        mapView.invalidate();
    }
}
