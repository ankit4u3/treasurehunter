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

import org.treasurehunter.database.DbFrontend;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.List;

public class GraphicsGenerator {

    private final int mMapPinWidth = 25;

    private final int mMapPinHeight = 30;

    private final Drawable mNormalPinBg;

    private final Drawable mSelectedPinBg;

    private final Drawable mUnavailablePinBg;

    private final Drawable mMineOverlay;

    private final Drawable mFoundOverlay;

    private final Drawable mDnfOverlay;

    private final Drawable mNewOverlay;

    private final Resources mResources;

    /** Cache positioned drawable of encountered geocache types */
    private HashMap<Integer, Drawable> mCacheTypeDrawables = new HashMap<Integer, Drawable>();

    private Drawable mWaypointPinBg;

    private Drawable mSelectedWaypointPinBg;

    public GraphicsGenerator(Resources resources) {
        mResources = resources;
        mTempPaint = new Paint();
        mTempRect = new Rect();

        mNormalPinBg = upperLeft(resources.getDrawable(R.drawable.pin_25x30_light2));
        mSelectedPinBg = upperLeft(resources.getDrawable(R.drawable.pin_25x30_yellow));
        mUnavailablePinBg = upperLeft(resources.getDrawable(R.drawable.pin_25x30_dark));

        mWaypointPinBg = upperLeft(resources.getDrawable(R.drawable.pin_waypoint_bg));
        mSelectedWaypointPinBg = upperLeft(resources
                .getDrawable(R.drawable.pin_waypoint_bg_selected));

        mMineOverlay = upperRight(resources.getDrawable(R.drawable.overlay_mine));
        mFoundOverlay = upperRight(resources.getDrawable(R.drawable.overlay_found));
        mDnfOverlay = upperRight(resources.getDrawable(R.drawable.overlay_dnf));
        mNewOverlay = upperRight(resources.getDrawable(R.drawable.overlay_new));
    }

    private static Drawable upperLeft(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, width - 1, height - 1);
        return drawable;
    }

    private Drawable upperRight(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(mMapPinWidth - 1 - width, 0, mMapPinWidth - 1, height - 1);
        return drawable;
    }

    private Drawable upperCenter(int y, Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(mMapPinWidth - 1 - width, 0, mMapPinWidth - 1, height - 1);
        int center = mMapPinWidth / 2;
        drawable.setBounds(center - width / 2, y, center + width / 2, y + height - 1);
        return drawable;
    }

    public Drawable createRating(Drawable unselected, Drawable halfSelected, Drawable selected,
            int rating) {
        int width = unselected.getIntrinsicWidth();
        int height = unselected.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(5 * width, 16, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        for (int i = 0; i < rating / 2; i++) {
            selected.setBounds(width * i, 0, width * (i + 1) - 1, height - 1);
            selected.draw(c);
        }
        if (rating % 2 == 1) {
            int i = rating / 2;
            halfSelected.setBounds(width * i, 0, width * (i + 1) - 1, height - 1);
            halfSelected.draw(c);
        }
        for (int i = rating / 2 + (rating % 2); i < 5; i++) {
            unselected.setBounds(width * i, 0, width * (i + 1) - 1, height - 1);
            unselected.draw(c);
        }
        return new BitmapDrawable(bitmap);
    }

    public Drawable[] getRatings(Resources r) {
        Drawable[] ratings = new Drawable[10];
        for (int i = 1; i <= 10; i++) {
            ratings[i - 1] = createRating(r.getDrawable(R.drawable.star_unselected),
                    r.getDrawable(R.drawable.star_half_selected),
                    r.getDrawable(R.drawable.star_selected), i);
        }
        return ratings;
    }

    private final Paint mTempPaint;

    private final Rect mTempRect;

    private Drawable createOverlay(Geocache geocache, int thickness, int bottom, int backdropId,
            Drawable overlayIcon, Resources resources) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, backdropId);
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();

        Bitmap copy;
        if (bottom >= 0) {
            copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } else {
            copy = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight() - bottom,
                    Bitmap.Config.ARGB_8888);
            int[] pixels = new int[imageWidth * imageHeight];
            bitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
            copy.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
            imageHeight = copy.getHeight();
            bottom = 0;
        }

        Canvas canvas = new Canvas(copy);

        drawRatings(canvas, geocache.getDifficulty(), geocache.getTerrain(), imageWidth,
                imageHeight - bottom, thickness);

        if (overlayIcon != null) {
            overlayIcon.setBounds(imageWidth - 1 - overlayIcon.getIntrinsicWidth(), 0,
                    imageWidth - 1, overlayIcon.getIntrinsicHeight() - 1);
            overlayIcon.draw(canvas);
        }

        return new BitmapDrawable(copy);
    }

    private void drawRatings(Canvas canvas, int difficulty, int terrain, int maxWidth,
            int maxHeight, int thickness) {
        // mTempPaint.setColor(Color.BLUE);
        // mTempPaint.setARGB(255, 0x20, 0x20, 0xFF); //light blue
        mTempPaint.setColor(Color.YELLOW);
        int diffWidth = (int)(maxWidth * difficulty / 10.0);
        mTempRect.set(0, maxHeight - 2 * thickness - 2, diffWidth, maxHeight - thickness - 2);
        canvas.drawRect(mTempRect, mTempPaint);

        mTempPaint.setARGB(255, 0xDB, 0xA1, 0x09); // a lighter brown
        // mTempPaint.setARGB(255, 139, 94, 23); //same color as paws
        int terrWidth = (int)(maxWidth * terrain / 10.0);
        // Draw on right side:
        // mTempRect.set(imageWidth-terrWidth, imageHeight-bottom-thickness-1,
        // imageWidth-1, imageHeight-bottom-1);
        // Draw on left side:
        mTempRect.set(0, maxHeight - thickness - 1, terrWidth, maxHeight - 1);
        canvas.drawRect(mTempRect, mTempPaint);
    }

    /** Creates the icon to use in the list view */
    public Drawable createIcon(Geocache geocache, Resources resources, DbFrontend dbFrontend) {
        Drawable overlayIcon = null;
        List<Integer> tags = geocache.getTags(dbFrontend);
        if (tags.contains(Tags.MINE))
            overlayIcon = resources.getDrawable(R.drawable.overlay_mine_cacheview);
        else if (tags.contains(Tags.FOUND))
            overlayIcon = resources.getDrawable(R.drawable.overlay_found_cacheview);
        else if (tags.contains(Tags.DNF))
            overlayIcon = resources.getDrawable(R.drawable.overlay_dnf_cacheview);
        else if (tags.contains(Tags.NEW))
            overlayIcon = resources.getDrawable(R.drawable.overlay_new_cacheview);

        return createOverlay(geocache, 3, -5, geocache.getCacheType().icon(), overlayIcon,
                resources);
    }

    private Drawable getGeocacheTypeDrawable(GeocacheType cacheType) {
        int cacheTypeId = cacheType.toInt();
        Drawable cacheTypeDrawable = mCacheTypeDrawables.get(cacheTypeId);
        if (cacheTypeDrawable == null) {
            int id = cacheType.iconMap();
            cacheTypeDrawable = upperCenter(1, mResources.getDrawable(id));
            mCacheTypeDrawables.put(cacheTypeId, cacheTypeDrawable);
        }
        return cacheTypeDrawable;
    }

    public Drawable getMapIcon(Geocache geocache, boolean isSelected, DbFrontend dbFrontend) {
        if (!isSelected && geocache.getIconMap() != null)
            return geocache.getIconMap();
        List<Integer> tags = geocache.getTags(dbFrontend);

        Drawable background;
        if (isSelected) {
            background = mSelectedPinBg;
        } else if (tags.contains(Tags.UNAVAILABLE)) {
            background = mUnavailablePinBg;
        } else {
            background = mNormalPinBg;
        }

        Drawable cacheType = getGeocacheTypeDrawable(geocache.getCacheType());

        Drawable overlayIcon = null;
        if (tags.contains(Tags.MINE))
            overlayIcon = mMineOverlay;
        else if (tags.contains(Tags.FOUND))
            overlayIcon = mFoundOverlay;
        else if (tags.contains(Tags.DNF))
            overlayIcon = mDnfOverlay;
        else if (tags.contains(Tags.NEW))
            overlayIcon = mNewOverlay;

        Drawable pin = getMapIcon(background, cacheType, geocache.getDifficulty(),
                geocache.getTerrain(), overlayIcon);
        if (!isSelected)
            geocache.setIconMap(pin);
        return pin;
    }

    public Drawable getMapIcon(Waypoint waypoint, boolean isSelected, DbFrontend dbFrontend) {
        if (!isSelected && waypoint.getIconMap() != null)
            return waypoint.getIconMap();

        Drawable background;
        if (isSelected) {
            background = mSelectedWaypointPinBg;
        } else {
            background = mWaypointPinBg;
        }

        Drawable cacheType = getGeocacheTypeDrawable(waypoint.getCacheType());

        Drawable pin = getMapIcon(background, cacheType);
        if (!isSelected)
            waypoint.setIconMap(pin);
        return pin;
    }

    /** The drawables must already have their bounds set correctly */
    private Drawable getMapIcon(Drawable background, Drawable cacheType, int difficulty,
            int terrain, Drawable mark) {
        Bitmap bitmap = Bitmap.createBitmap(mMapPinWidth, mMapPinHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        cacheType.draw(canvas);
        final int fromBottom = 4;
        final int thickness = 2;
        drawRatings(canvas, difficulty, terrain, mMapPinWidth, mMapPinHeight - fromBottom,
                thickness);
        if (mark != null)
            mark.draw(canvas);

        BitmapDrawable bd = new BitmapDrawable(bitmap);
        // The center of the pin is at bottom middle:
        bd.setBounds(-mMapPinWidth / 2, -mMapPinHeight, mMapPinWidth / 2, 0);
        return bd;
    }

    private Drawable getMapIcon(Drawable background, Drawable cacheType) {
        Bitmap bitmap = Bitmap.createBitmap(mMapPinWidth, mMapPinHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        cacheType.draw(canvas);

        BitmapDrawable bd = new BitmapDrawable(bitmap);
        // The center of the pin is at bottom middle:
        bd.setBounds(-mMapPinWidth / 2, -mMapPinHeight, mMapPinWidth / 2, 0);
        return bd;
    }

}
