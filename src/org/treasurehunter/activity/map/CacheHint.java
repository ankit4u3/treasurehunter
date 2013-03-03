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

package org.treasurehunter.activity.map;

import com.google.android.maps.MapView.LayoutParams;

import org.treasurehunter.GeoObject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.TextView;

public class CacheHint {
    private final Context mContext;

    private final Drawable mBackground;

    private final GeoMapView mGeoMapView;

    private TextView mTextView;

    private LayoutParams mLayoutParams;

    private GeoObject mGeocache;

    public CacheHint(GeoMapView geoMapView) {
        mGeoMapView = geoMapView;
        mContext = geoMapView.getContext();
        mBackground = geoMapView.getResources().getDrawable(android.R.drawable.toast_frame);
        mTextView = new CacheTextView();
    }

    private class CacheTextView extends TextView {
        public CacheTextView() {
            super(mContext);
            setBackgroundColor(Color.GRAY);
            setBackgroundDrawable(mBackground);
            setTextColor(Color.WHITE);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            hide();
            return true;
        }
    }

    private boolean mIsVisible = false;

    public GeoObject getGeoObject() {
        return mGeocache;
    }

    public void showGeoObject(final GeoObject geocache) {
        if (mGeocache == geocache)
            return;
        mGeocache = geocache;

        hide();

        if (geocache == null) {
            return;
        }

        CharSequence text = geocache.getName();
        if (text.equals(""))
            text = geocache.getId();
        mTextView.setText(text);
        if (mLayoutParams == null)
            mLayoutParams = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT, geocache.getGeoPoint(),
                    LayoutParams.CENTER_HORIZONTAL | LayoutParams.TOP);
        else
            mLayoutParams.point = geocache.getGeoPoint();
        show();
    }

    public void show() {
        if (mGeocache == null || mIsVisible)
            return;
        mIsVisible = true;
        mGeoMapView.addView(mTextView, mLayoutParams);
    }

    /** Hide the hint so it can be brought back again with show() */
    public void hide() {
        if (!mIsVisible)
            return;
        mIsVisible = false;
        mGeoMapView.removeView(mTextView);
    }

    /** Removes the hint. It can't be brought back with show() */
    public void reset() {
        if (mGeocache == null)
            return;
        mGeocache = null;
        if (!mIsVisible)
            return;
        mIsVisible = false;
        mGeoMapView.removeView(mTextView);
    }

    public void toggle() {
        if (mIsVisible)
            hide();
        else
            show();
    }
}
