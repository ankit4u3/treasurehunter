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

package org.treasurehunter.activity.cachelist;

import org.treasurehunter.Geocache;
import org.treasurehunter.GraphicsGenerator;
import org.treasurehunter.R;
import org.treasurehunter.Tags;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.database.DistanceAndBearing;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CacheListRowInflater {
    public static class CacheNameStyler {
        public void setTextStyle(TextView cacheName, boolean fArchived, boolean fUnavailable) {
            if (fArchived)
                cacheName.setTextColor(Color.DKGRAY);
            else if (fUnavailable)
                cacheName.setTextColor(Color.LTGRAY);
            else
                cacheName.setTextColor(Color.WHITE);
            if (fArchived || fUnavailable)
                cacheName.setPaintFlags(cacheName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                cacheName.setPaintFlags(cacheName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    public static class RowViews {
        private final TextView mAttributes;

        private final TextView mCacheName;

        private final TextView mDistance;

        private final ImageView mIcon;

        private final TextView mId;

        private final Resources mResources;

        private final CacheNameStyler mCacheNameStyler;

        RowViews(TextView attributes, TextView cacheName, TextView distance, ImageView icon,
                TextView id, Resources resources, CacheNameStyler cacheNameStyler) {
            mAttributes = attributes;
            mCacheName = cacheName;
            mDistance = distance;
            mIcon = icon;
            mId = id;
            mResources = resources;
            mCacheNameStyler = cacheNameStyler;
        }

        void set(DistanceAndBearing distanceAndBearing, float azimuth,
                DistanceFormatter distanceFormatter, BearingFormatter relativeBearingFormatter,
                GraphicsGenerator graphicsGenerator, DbFrontend dbFrontend) {
            Geocache geocache = distanceAndBearing.getGeocache();
            Drawable icon = geocache.getIcon();
            if (icon == null) {
                icon = graphicsGenerator.createIcon(geocache, mResources, dbFrontend);
                geocache.setIcon(icon);
            }
            mIcon.setImageDrawable(icon);
            CharSequence geocacheId = geocache.getId();
            mId.setText(geocacheId);
            mAttributes.setText(geocache.getFormattedAttributes());
            mCacheName.setText(geocache.getName());
            boolean fArchived = geocache.hasTag(Tags.ARCHIVED, dbFrontend);
            boolean fUnavailable = geocache.hasTag(Tags.UNAVAILABLE, dbFrontend);
            mCacheNameStyler.setTextStyle(mCacheName, fArchived, fUnavailable);
            String distanceText = "";
            double distance = distanceAndBearing.getDistance();
            if (distance != -1) {
                final CharSequence formattedDistance = distanceFormatter
                        .formatDistance((float)distance);
                float bearing = distanceAndBearing.getBearing();
                final String formattedBearing = relativeBearingFormatter.formatBearing(bearing,
                        azimuth);
                distanceText = formattedDistance + " " + formattedBearing;
            }
            mDistance.setText(distanceText);
        }
    }

    private BearingFormatter mBearingFormatter;

    private DistanceFormatter mDistanceFormatter;

    private final LayoutInflater mLayoutInflater;

    private final Resources mResources;

    private final GraphicsGenerator mGraphicsGenerator;

    private final DbFrontend mDbFrontend;

    private final CacheNameStyler mCacheNameAttributes;

    public CacheListRowInflater(DistanceFormatter distanceFormatter, LayoutInflater layoutInflater,
            BearingFormatter relativeBearingFormatter, Resources resources,
            GraphicsGenerator graphicsGenerator, DbFrontend dbFrontend,
            CacheNameStyler cacheNameAttributes) {
        mLayoutInflater = layoutInflater;
        mDistanceFormatter = distanceFormatter;
        mBearingFormatter = relativeBearingFormatter;
        mResources = resources;
        mGraphicsGenerator = graphicsGenerator;
        mDbFrontend = dbFrontend;
        mCacheNameAttributes = cacheNameAttributes;
    }

    BearingFormatter getBearingFormatter() {
        return mBearingFormatter;
    }

    public View inflate() {
        View view = mLayoutInflater.inflate(R.layout.cache_row, null);
        RowViews rowViews = new RowViews((TextView)view.findViewById(R.id.txt_gcattributes),
                (TextView)view.findViewById(R.id.txt_cache),
                (TextView)view.findViewById(R.id.distance),
                (ImageView)view.findViewById(R.id.gc_row_icon),
                (TextView)view.findViewById(R.id.txt_gcid), mResources, mCacheNameAttributes);
        view.setTag(rowViews);
        return view;
    }

    public void setBearingFormatter(boolean absoluteBearing) {
        mBearingFormatter = absoluteBearing ? new BearingFormatter.Absolute()
                : new BearingFormatter.Relative();
    }

    public void setData(View view, DistanceAndBearing geocacheVector, float azimuth) {
        ((RowViews)view.getTag()).set(geocacheVector, azimuth, mDistanceFormatter,
                mBearingFormatter, mGraphicsGenerator, mDbFrontend);
    }
}
