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

package org.treasurehunter.database;

import org.treasurehunter.Clock;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.Source;
import org.treasurehunter.Tags;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.xmlimport.GeocacheDetails;

import android.util.Log;

import java.util.List;

/**
 * Collect all methods that change the database here. Don't retain a reference
 * to CacheWriter outside a local scope since it will be invalid if the
 * underlying database is closed.
 */
public class CacheWriter {
    private final ISQLiteDatabase mSqlite;

    private final GeocacheFactory mGeocacheFactory;

    private final DbFrontend mDbFrontend;

    CacheWriter(ISQLiteDatabase sqlite, DbFrontend dbFrontend, GeocacheFactory geocacheFactory) {
        mSqlite = sqlite;
        mGeocacheFactory = geocacheFactory;
        mDbFrontend = dbFrontend;
    }

    public void markAllGpxForDeletion() {
        mSqlite.execSQL(DatabaseConstants.SQL_RESET_DELETE_ME_CACHES);
        mSqlite.execSQL(DatabaseConstants.SQL_SET_DELETE_ME_GPX);
    }

    /**
     * Deletes all caches and Sources that are still marked for deletion
     */
    public void deleteAllMarkedForDeletion() {
        for (String id : mDbFrontend.getCachesMarkedForDeletion()) {
            mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_TAGS, id);
            mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_LOGS, id);
            mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_TRAVELBUGS, id);
            mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_WAYPOINTS, id);
        }
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_OLD_CACHES);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_OLD_GPX);
        mDbFrontend.flushTotalCount();
        mGeocacheFactory.flushCache();
    }

    public void deleteCache(CharSequence id) {
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE, id);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_TAGS, id);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_LOGS, id);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_TRAVELBUGS, id);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_WAYPOINTS, id);
        mGeocacheFactory.flushGeocache(id);
        mDbFrontend.flushTotalCount();
    }

    /**
     * Checks if the geocache is different from the previously existing one.
     * 
     * @return True if the geocache was updated in the database.
     */
    public boolean conditionallyWriteCache(CharSequence id, CharSequence name, double latitude,
            double longitude, Source source, GeocacheType cacheType, int difficulty, int terrain,
            int container, String shortDesc, String longDesc, String hints, String lastModified,
            String creationDate, String owner, String placedBy) {
        Geocache geocache = mDbFrontend.loadCacheFromId(id);
        GeocacheDetails details = mDbFrontend.getCacheDetails(id);
        // Differing sources or lastModified are not factors when
        // determining if a cache has changed
        if (geocache != null && geocache.getName().equals(name)
                && Util.approxEquals(geocache.getLatitude(), latitude)
                && Util.approxEquals(geocache.getLongitude(), longitude)
                && geocache.getCacheType() == cacheType && geocache.getDifficulty() == difficulty
                && geocache.getTerrain() == terrain && geocache.getContainer() == container
                && details.mShortDescription.equals(shortDesc)
                && details.mLongDescription.equals(longDesc) && details.mEncodedHints.equals(hints)
                && details.mCreationDate.equals(creationDate) && details.mOwner.equals(owner)
                && details.mPlacedBy.equals(placedBy)) {
            mSqlite.execSQL(DatabaseConstants.SQL_CACHES_DONT_DELETE_ID, id);
            return false;
        }

        mGeocacheFactory.flushGeocache(id);
        mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_CACHE_ALL, id, name, new Double(latitude),
                new Double(longitude), source.toString(), cacheType.toInt(), difficulty, terrain,
                container, shortDesc, longDesc, hints, lastModified, creationDate, owner, placedBy);
        return true;
    }

    /**
     * Checks if the waypoint is different from the previously existing one.
     * 
     * @return True if the waypoint was updated in the database.
     */
    public boolean conditionallyWriteWaypoint(CharSequence id, CharSequence name, double latitude,
            double longitude, Source source, GeocacheType cacheType, CharSequence parentId,
            String lastModified) {
        // Log.d("TreasureHunter", "Writing waypoint: "+id+": "+name);
        Waypoint waypoint = mDbFrontend.loadWaypointFromId(id.toString());
        // Differing sources or lastModified are not factors when
        // determining if a cache has changed
        if (waypoint != null && waypoint.getName().equals(name)
                && Util.approxEquals(waypoint.getLatitude(), latitude)
                && Util.approxEquals(waypoint.getLongitude(), longitude)
                && waypoint.getCacheType() == cacheType
                && waypoint.getParentCache().equals(parentId)) {
            return false;
        }

        mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_WAYPOINT, id, name, new Double(latitude),
                new Double(longitude), source.toString(), false, cacheType.toInt(), lastModified,
                Clock.getCurrentStringTime(), parentId);
        return true;
    }

    /**
     * Unconditionally updates certain attributes of a geocache.
     */
    public void updateCache(CharSequence id, CharSequence name, double latitude, double longitude,
            String updateTime) {
        mGeocacheFactory.flushGeocache(id);

        mSqlite.execSQL(DatabaseConstants.SQL_UPDATE_CACHE_ATTRS, name, new Double(latitude),
                new Double(longitude), updateTime, id);
    }

    /**
     * Unconditionally updates certain attributes of a waypoint.
     */
    public void updateWaypoint(CharSequence id, CharSequence name, double latitude,
            double longitude, String updateTime) {
        mGeocacheFactory.flushGeocache(id);

        mSqlite.execSQL(DatabaseConstants.SQL_UPDATE_WAYPOINT_ATTRS, name, new Double(latitude),
                new Double(longitude), updateTime, id);
    }

    /** Unconditionally update the geocache in the database */
    public void insertAndUpdateCache(CharSequence id, CharSequence name, double latitude,
            double longitude, Source source, GeocacheType cacheType, int difficulty, int terrain,
            int container, String creationDate) {
        mGeocacheFactory.flushGeocache(id);
        mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_CACHE, id, name, new Double(latitude),
                new Double(longitude), source.toString(), cacheType.toInt(), difficulty, terrain,
                container, creationDate, creationDate);
    }

    /** Unconditionally update the waypoint in the database */
    public void insertAndUpdateWaypoint(CharSequence id, CharSequence name, double latitude,
            double longitude, Source source, GeocacheType cacheType, String creationDate,
            CharSequence parentId) {
        mGeocacheFactory.flushGeocache(id);
        mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_WAYPOINT, id, name, new Double(latitude),
                new Double(longitude), source.toString(), 0, cacheType.toInt(), creationDate,
                creationDate, parentId);
        // Id, Name, Latitude, Longitude, Source, DeleteMe, CacheType,
        // LastModifiedDate, CreationDate, ParentId
    }

    /**
     * If another log with the same id is already in the database, it will be
     * replaced.
     */
    public void addLogs(String cacheId, List<GeocacheDetails.GeocacheLog> logs) {
        for (GeocacheDetails.GeocacheLog log : logs) {
            mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_LOG, log.mId, cacheId, log.mLogType,
                    log.mDate, log.mFinderName, log.mText, log.mIsTextEncoded);
        }
    }

    public void addUserNotes(String cacheId, List<GeocacheDetails.GeocacheLog> userNotes) {
        boolean deletedGSAKNote = false;
        for (GeocacheDetails.GeocacheLog note : userNotes) {
            if ((note.mLogType == GeocacheDetails.GeocacheLog.GSAKNOTE_LOG_TYPE)
                    && (!deletedGSAKNote)) {
                /* Delete the GSAK note for this cache - GSAK allows only one */
                try {
                    mSqlite.execSQL(DatabaseConstants.SQL_DELETE_USERNOTE, cacheId,
                            GeocacheDetails.GeocacheLog.GSAKNOTE_LOG_TYPE);
                } catch (android.database.SQLException sqlException) {
                    /* could be because the log did not exist, don't care... */
                }
                deletedGSAKNote = true;
            }
            mSqlite.execSQL(DatabaseConstants.SQL_INSERT_USERNOTE, cacheId, note.mLogType,
                    note.mDate, note.mFinderName, note.mText);
        }
    }

    /**
     * Replaces the previous list of travelbugs for the geocache with this one.
     * If another travelbug with the same id is already in the database, it will
     * be replaced.
     */
    public void setTravelbugs(String cacheId, List<GeocacheDetails.Travelbug> travelbugs) {
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHE_TRAVELBUGS, cacheId);
        for (GeocacheDetails.Travelbug tb : travelbugs) {
            mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_TRAVELBUG, tb.mId, cacheId, tb.mRef,
                    tb.mName);
        }
    }

    public void updateTag(CharSequence id, int tag, boolean set) {
        mDbFrontend.setGeocacheTag(id, tag, set);
    }

    public boolean isLockedFromUpdating(CharSequence id) {
        return mDbFrontend.geocacheHasTag(id, Tags.LOCKED_FROM_OVERWRITING);
    }

    /**
     * If the Source is already loaded, marks this gpx and its caches in the
     * database to protect them from being nuked when the load is complete.
     * 
     * @param gpxName
     * @param gpxTime
     * @return True if the gpx is already loaded
     */
    public boolean saveGpxIfAlreadyLoaded(String gpxName, String gpxTime) {
        boolean gpxAlreadyLoaded = mDbFrontend.isSourceLoaded(gpxName, gpxTime);
        if (gpxAlreadyLoaded) {
            mSqlite.execSQL(DatabaseConstants.SQL_CACHES_DONT_DELETE_SOURCE, gpxName);
            mSqlite.execSQL(DatabaseConstants.SQL_GPX_DONT_DELETE_ME, gpxName);
        }
        Log.d("TreasureHunter", "saveGpxIfAlreadyLoaded " + gpxName + " returns "
                + (gpxAlreadyLoaded ? "true" : "false"));
        return gpxAlreadyLoaded;
    }

    public void dontDeleteGpx(String gpxName) {
        mSqlite.execSQL(DatabaseConstants.SQL_GPX_DONT_DELETE_ME, gpxName);
    }

    public void beginTransaction() {
        mSqlite.beginTransaction();
    }

    public void endTransaction() {
        mSqlite.setTransactionSuccessful();
        mSqlite.endTransaction();
    }

    public void writeGpx(String gpxName, String pocketQueryExportTime) {
        mSqlite.execSQL(DatabaseConstants.SQL_REPLACE_GPX, gpxName, pocketQueryExportTime);
    }

    /** When calling this, also call BcachingConfig.clearLastUpdate() */
    public void deleteAll() {
        Log.i("TreasureHunter", "CacheWriter.deleteAll()");

        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_TAGS, Tags.FAVORITE);
        clearTagForAllCaches(Tags.LOCKED_FROM_OVERWRITING);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_CACHES);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_GPX);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_LOGS);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_TRAVELBUGS);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_WAYPOINTS);

        mGeocacheFactory.flushCache();
    }

    public void deleteSource(String source) {
        Log.i("TreasureHunter", "CacheWriter.deleteSource(" + source + ")");

        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_LOGS_FROM_SOURCE, source);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_TRAVELBUGS_FROM_SOURCE, source);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_TAGS_FROM_SOURCE, source);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_WAYPOINTS_FROM_SOURCE, source);

        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_CACHES_FROM_SOURCE, source);
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_GPX, source);

        mGeocacheFactory.flushCache();
    }

    public void clearTagForAllCaches(int tag) {
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_ALL_TAGS, tag);
        mGeocacheFactory.removeTagFromAllCaches(tag);
    }

    public void deleteWaypoint(String id) {
        mSqlite.execSQL(DatabaseConstants.SQL_DELETE_WAYPOINT, id);
    }
}
