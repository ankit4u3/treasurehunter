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

package org.treasurehunter.activity.compass;

import org.treasurehunter.Geocache;
import org.treasurehunter.R;
import org.treasurehunter.Tags;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.cachelist.CacheListRowInflater.CacheNameStyler;
import org.treasurehunter.database.DbFrontend;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class GeocacheViewer {
    public interface AttributeViewer {
        void setImage(int attributeValue);

        void clear();
    }

    public static class LabelledAttributeViewer implements AttributeViewer {
        private final UnlabelledAttributeViewer mUnlabelledAttributeViewer;

        private final TextView mLabel;

        public LabelledAttributeViewer(TextView label, ImageView imageView,
                AttributeViewer imageCollection) {
            mUnlabelledAttributeViewer = new UnlabelledAttributeViewer(imageView, imageCollection);
            mLabel = label;
        }

        @Override
        public void setImage(int attributeValue) {
            mUnlabelledAttributeViewer.setImage(attributeValue);
            mLabel.setVisibility(attributeValue == 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void clear() {
            mUnlabelledAttributeViewer.clear();
            mLabel.setVisibility(View.GONE);
        }
    }

    public static class DrawableImages implements AttributeViewer {
        private final Drawable[] mDrawables;

        private final ImageView mImageView;

        public DrawableImages(ImageView imageView, Drawable[] drawables) {
            mImageView = imageView;
            mDrawables = drawables;
        }

        @Override
        public void setImage(int attributeValue) {
            mImageView.setImageDrawable(mDrawables[attributeValue]);
            mImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void clear() {
            mImageView.setVisibility(View.INVISIBLE);
        }
    }

    public static class ResourceImages implements AttributeViewer {
        private final int[] mResources;

        private final ImageView mImageView;

        public ResourceImages(ImageView imageView, int[] resources) {
            mImageView = imageView;
            mResources = resources;
        }

        @Override
        public void setImage(int attributeValue) {
            mImageView.setImageResource(mResources[attributeValue]);
            mImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void clear() {
            mImageView.setVisibility(View.INVISIBLE);
        }
    }

    public static class UnlabelledAttributeViewer implements AttributeViewer {
        private final ImageView mImageView;

        private final AttributeViewer mImageCollection;

        public UnlabelledAttributeViewer(ImageView imageView, AttributeViewer imageCollection) {
            mImageView = imageView;
            mImageCollection = imageCollection;
        }

        @Override
        public void setImage(int attributeValue) {
            if (attributeValue == 0) {
                mImageView.setVisibility(View.GONE);
                return;
            }
            mImageCollection.setImage(attributeValue - 1);
            mImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void clear() {
            setImage(0);
        }
    }

    public static final int CONTAINER_IMAGES[] = {
            R.drawable.size_1, R.drawable.size_2, R.drawable.size_3, R.drawable.size_4
    };

    private final ImageView mCacheTypeImageView;

    private final AttributeViewer mContainer;

    private final AttributeViewer mDifficulty;

    private final TextView mId;

    private final TextView mName;

    private final RadarView mRadarView;

    private final AttributeViewer mTerrain;

    private final CacheNameStyler mCacheNameStyler;

    private final DbFrontend mDbFrontend;

    public GeocacheViewer(RadarView radarView, TextView gcId, TextView gcName,
            ImageView cacheTypeImageView, AttributeViewer gcDifficulty, AttributeViewer gcTerrain,
            UnlabelledAttributeViewer gcContainer, CacheNameStyler cacheNameStyler,
            DbFrontend dbFrontend) {
        mRadarView = radarView;
        mId = gcId;
        mName = gcName;
        mCacheTypeImageView = cacheTypeImageView;
        mDifficulty = gcDifficulty;
        mTerrain = gcTerrain;
        mContainer = gcContainer;
        mCacheNameStyler = cacheNameStyler;
        mDbFrontend = dbFrontend;
    }

    public void set(Geocache geocache) {
        final double latitude = geocache.getLatitude();
        final double longitude = geocache.getLongitude();
        mRadarView.setTarget((int)(latitude * GeoUtils.MILLION),
                (int)(longitude * GeoUtils.MILLION));
        mId.setText(geocache.getId());

        mCacheTypeImageView.setVisibility(View.VISIBLE);
        mCacheTypeImageView.setImageResource(geocache.getCacheType().iconBig());
        mContainer.setImage(geocache.getContainer());
        mDifficulty.setImage(geocache.getDifficulty());
        mTerrain.setImage(geocache.getTerrain());

        mName.setText(geocache.getName());
        CharSequence id = geocache.getId();
        boolean fArchived = mDbFrontend.geocacheHasTag(id, Tags.ARCHIVED);
        boolean fUnavailable = mDbFrontend.geocacheHasTag(id, Tags.UNAVAILABLE);
        mCacheNameStyler.setTextStyle(mName, fArchived, fUnavailable);
    }

    public void set(Waypoint waypoint) {
        final double latitude = waypoint.getLatitude();
        final double longitude = waypoint.getLongitude();
        mRadarView.setTarget((int)(latitude * GeoUtils.MILLION),
                (int)(longitude * GeoUtils.MILLION));
        mId.setText(waypoint.getId());

        mCacheTypeImageView.setVisibility(View.VISIBLE);
        mCacheTypeImageView.setImageResource(waypoint.getCacheType().iconBig());
        // Set parent values? They should be set as you cannot set other
        // waypoints than currently selected cache's
        // mContainer.setImage(geocache.getContainer());
        // mDifficulty.setImage(geocache.getDifficulty());
        // mTerrain.setImage(geocache.getTerrain());

        mName.setText(waypoint.getName());
        // CharSequence id = waypoint.getId();
        // boolean fArchived = mDbFrontend.geocacheHasTag(id, Tags.ARCHIVED);
        // boolean fUnavailable = mDbFrontend.geocacheHasTag(id,
        // Tags.UNAVAILABLE);
        mCacheNameStyler.setTextStyle(mName, false, false);
    }

    public void clear() {
        mRadarView.handleUnknownLocation(); // Probably overwritten by next
                                            // location
        // setTarget((int)(latitude * GeoUtils.MILLION), (int)(longitude *
        // GeoUtils.MILLION));
        mId.setText("");

        mCacheTypeImageView.setVisibility(View.INVISIBLE);
        mContainer.clear();
        mDifficulty.clear();
        mTerrain.clear();
        mName.setText("");
    }
}
