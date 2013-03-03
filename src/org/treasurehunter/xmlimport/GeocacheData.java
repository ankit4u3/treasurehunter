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

package org.treasurehunter.xmlimport;

import org.treasurehunter.GeocacheType;
import org.treasurehunter.GeocacheTypeFactory;
import org.treasurehunter.Source;
import org.treasurehunter.Tags;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.xmlimport.GeocacheDetails.GeocacheLog;
import org.treasurehunter.xmlimport.GeocacheDetails.Travelbug;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GeocacheData {
    public String mId;

    public String mName;

    public String mLatitude;

    public String mLongitude;

    public String mCacheType;

    public String mDifficulty;

    public String mTerrain;

    public String mContainer;

    public String mAvailable;

    public String mArchived;

    public String mSym;

    public boolean mIsMine;

    public String mLastModified;

    public GeocacheDetails mDetails = new GeocacheDetails();

    public List<GeocacheLog> mLogs = new ArrayList<GeocacheLog>();

    public List<Travelbug> mTravelbugs = new ArrayList<Travelbug>();

    public List<GeocacheLog> mUserNotes = new ArrayList<GeocacheLog>();

    /**
     * If Tags.NULL, Find status is unchanged. If Tags.DNF or Tags.FOUND, the
     * status will be updated to this.
     */
    public int mMyFindTag = Tags.NULL;

    public void writeTo(CacheWriter mCacheWriter, Source source) {
        if (mLatitude == null || mLongitude == null || mId == null) {
            return;
        }

        if (mCacheWriter.isLockedFromUpdating(mId)) {
            Log.i("TreasureHunter", "Not updating " + mId + " because it is locked");
            return;
        }

        if (mSym != null) {
            if (mSym.equals("Geocache Found"))
                mCacheWriter.updateTag(mId, Tags.FOUND, true);
        }

        if (mArchived != null)
            mCacheWriter.updateTag(mId, Tags.ARCHIVED, mArchived.equalsIgnoreCase("true"));
        if (mAvailable != null)
            mCacheWriter.updateTag(mId, Tags.UNAVAILABLE, mAvailable.equalsIgnoreCase("false"));

        mCacheWriter.updateTag(mId, Tags.MINE, mIsMine);

        if (mMyFindTag == Tags.FOUND) {
            mCacheWriter.updateTag(mId, Tags.FOUND, true);
            mCacheWriter.updateTag(mId, Tags.DNF, false);
        } else if (mMyFindTag == Tags.DNF) {
            mCacheWriter.updateTag(mId, Tags.FOUND, false);
            mCacheWriter.updateTag(mId, Tags.DNF, true);
        }

        double latitude = Double.parseDouble(mLatitude);
        double longitude = Double.parseDouble(mLongitude);
        GeocacheTypeFactory factory = new GeocacheTypeFactory();
        GeocacheType cacheType = (mCacheType == null ? GeocacheType.NULL : factory
                .fromTag(mCacheType));
        int difficulty = (mDifficulty == null ? 0 : GeocacheTypeFactory.stars(mDifficulty));
        int terrain = (mTerrain == null ? 0 : GeocacheTypeFactory.stars(mTerrain));
        int container = (mContainer == null ? 0 : GeocacheTypeFactory.container(mContainer));

        if (cacheType.isWaypoint()) {
            CharSequence parentId = "GC" + mId.subSequence(2, mId.length());
            // boolean changed =
            mCacheWriter.conditionallyWriteWaypoint(mId, mName, latitude, longitude, source,
                    cacheType, parentId, mLastModified);
            // If a waypoint changed, we don't bother marking the
            // parent geocache as 'new'. The geocache has most likely changed
            // too.
        } else { // Cache is a geocache
            boolean changed = mCacheWriter.conditionallyWriteCache(mId, mName, latitude, longitude,
                    source, cacheType, difficulty, terrain, container, mDetails.mShortDescription,
                    mDetails.mLongDescription, mDetails.mEncodedHints, mLastModified,
                    mDetails.mCreationDate, mDetails.mOwner, mDetails.mPlacedBy);
            if (changed)
                mCacheWriter.updateTag(mId, Tags.NEW, true);
            mCacheWriter.addLogs(mId, mLogs);
            mCacheWriter.addUserNotes(mId, mUserNotes);
            mCacheWriter.setTravelbugs(mId, mTravelbugs);
        }
    }
}
