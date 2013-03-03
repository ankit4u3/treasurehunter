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

import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheList;
import org.treasurehunter.GeocacheListLazy;
import org.treasurehunter.GeocacheListPrecomputed;
import org.treasurehunter.Waypoint;
import org.treasurehunter.xmlimport.GeocacheDetails;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the front-end to access a database. It takes responsibility to
 * open and close the actual database connection without involving the clients
 * of this class.
 */
public class DbFrontend {
    private SqliteOpenHelperWrapper mOpenHelper;

    private boolean mIsDatabaseOpen;

    private CacheWriter mCacheWriter;

    private ISQLiteDatabase mDatabase;

    private final GeocacheFactory mGeocacheFactory;

    // private final Clock mClock = new Clock();
    /**
     * The total number of geocaches and waypoints in the database. -1 means not
     * initialized
     */
    private int mTotalCacheCount = -1;

    private DatabaseLocator mDatabaseLocator;

    private SQLiteDatabaseWrapper mSqliteDatabase;

    private static final String[] READER_COLUMNS = new String[] {
            "Latitude", "Longitude", "Id", "Description", "Source", "CacheType", "Difficulty",
            "Terrain", "Container", "LastModifiedDate"
    };

    private static final String READER_COLUMNS_STR = "SELECT Latitude, Longitude, Id, Description, Source, CacheType, Difficulty, Terrain, Container, LastModifiedDate ";

    private static final String[] DETAILS_READER_COLUMNS = new String[] {
            "Id", "ShortDesc", "LongDesc", "Hints", "CreationDate", "Owner", "PlacedBy"
    };

    // private static final String[] LOGS_COLUMNS = new String[] {
    // "LogId", "LogType", "LogDate", "FinderName", "Text", "IsTextEncoded" };
    private static final String[] TRAVELBUG_COLUMNS = new String[] {
            "TbId", "Ref", "Name"
    };

    private static final String[] WAYPOINT_COLUMNS = new String[] {
            "Latitude", "Longitude", "Id", "Name", "Source", "CacheType", "LastModifiedDate",
            "ParentId"
    };

    private static final String[] EmptyStringArray = new String[] {};

    public DbFrontend(DatabaseLocator databaseLocator, GeocacheFactory geocacheFactory) {
        mIsDatabaseOpen = false;
        mDatabaseLocator = databaseLocator;
        mGeocacheFactory = geocacheFactory;
    }

    // TODO: Designate a single thread to access the db => remove
    // 'synchronized', set SQLiteDatabase.setLockingEnabled(false)
    public synchronized void openDatabase() {
        if (mIsDatabaseOpen)
            return;
        // Log.d("TreasureHunter", "DbFrontend.openDatabase()");

        mOpenHelper = new SqliteOpenHelperWrapper(mDatabaseLocator.getDatabasePath());
        final SQLiteDatabase sqDb = mOpenHelper.getWritableDatabase();
        mDatabase = new SQLiteDatabaseWrapper(sqDb);
        mSqliteDatabase = mOpenHelper.getWritableSqliteWrapper();

        mIsDatabaseOpen = true;
    }

    public synchronized void closeDatabase() {
        if (!mIsDatabaseOpen)
            return;
        // Log.d("TreasureHunter", "DbFrontend.closeDatabase()");
        mIsDatabaseOpen = false;

        mOpenHelper.close();
        mCacheWriter = null;
        mDatabase = null;
    }

    /**
     * @param sqlQuery A complete SQL query to be executed. The query must
     *            return the id's of geocaches.
     */
    public GeocacheList loadCachesRaw(String sqlQuery) {
        openDatabase();

        // long start = mClock.getCurrentTime();
        Cursor cursor = mDatabase.rawQuery(sqlQuery, EmptyStringArray);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return GeocacheListPrecomputed.EMPTY;
        }

        ArrayList<Object> idList = new ArrayList<Object>();
        if (cursor != null) {
            do {
                idList.add(cursor.getString(0));
                Log.d("TreasureHunter", "DbFrontend" + cursor.getString(0));

            } while (cursor.moveToNext());
            cursor.close();
        }
        // Log.d("TreasureHunter", "DbFrontend.loadCachesRaw took " +
        // (mClock.getCurrentTime()-start)
        // + " ms to load " + idList.size() + " caches from query " + sqlQuery);
        return new GeocacheListLazy(this, idList);
    }

    /**
     * @param sql A complete SQL query, except the "SELECT x, y" part (that is,
     *            it starts with "FROM"
     */
    public GeocacheList loadCachesRawPrecomputed(String sql) {
        openDatabase();

        // long start = mClock.getCurrentTime();

        String query = READER_COLUMNS_STR + sql;
        Cursor cursor = mDatabase.rawQuery(query, EmptyStringArray);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return GeocacheListPrecomputed.EMPTY;
        }

        ArrayList<Geocache> geocaches = new ArrayList<Geocache>();
        do {
            Geocache geocache = mGeocacheFactory.fromCursor(cursor);
            geocaches.add(geocache);
        } while (cursor.moveToNext());
        cursor.close();
        // Log.d("TreasureHunter", "DbFrontend.loadCachesPrecomputed took " +
        // (mClock.getCurrentTime()-start)
        // + " ms (loaded " + geocaches.size() + " caches)");
        return new GeocacheListPrecomputed(geocaches);
    }

    private final String[] TIME_COLUMNS = {
        "strftime('%s', LastModifiedDate)"
    };

    /**
     * Returns the time as the number of millisec after Jan 1, 1970 or 0 if the
     * time wasn't valid.
     */
    public long getLastUpdatedTime(String id) {
        openDatabase();
        Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_CACHES, TIME_COLUMNS, "Id=?",
                null, null, null, null, id);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return 0;
        }
        long timeLong = cursor.getLong(0);
        cursor.close();
        return timeLong * 1000;
    }

    /**
     * Returns the loaded geocache or loads it from the database if not loaded
     * 
     * @return null if the cache id is not in the database
     */
    public Geocache loadCacheFromId(CharSequence id) {
        Geocache loadedGeocache = mGeocacheFactory.getFromId(id);
        if (loadedGeocache != null) {
            return loadedGeocache;
        }
        openDatabase();

        Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_CACHES, READER_COLUMNS, "Id=?",
                null, null, null, null, (String)id);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Geocache geocache = mGeocacheFactory.fromCursor(cursor);
        cursor.close();
        return geocache;
    }

    public Waypoint loadWaypointFromId(String id) {
        openDatabase();
        Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_WAYPOINTS, WAYPOINT_COLUMNS,
                "Id=?", null, null, null, null, id);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Waypoint wp = mGeocacheFactory.waypointFromCursor(cursor);
        cursor.close();
        return wp;
    }

    /** @return null if the cache id is not in the database */
    public GeocacheDetails getCacheDetails(CharSequence id) {
        openDatabase();

        Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_CACHES, DETAILS_READER_COLUMNS,
                "Id=?", null, null, null, null, (String)id);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        GeocacheDetails details = new GeocacheDetails();
        details.mShortDescription = cursor.getString(1);
        details.mLongDescription = cursor.getString(2);
        details.mEncodedHints = cursor.getString(3);
        details.mCreationDate = cursor.getString(4);
        details.mOwner = cursor.getString(5);
        details.mPlacedBy = cursor.getString(6);
        if (details.mShortDescription == null)
            details.mShortDescription = "";
        if (details.mLongDescription == null)
            details.mLongDescription = "";
        if (details.mEncodedHints == null)
            details.mEncodedHints = "";
        if (details.mCreationDate == null)
            details.mCreationDate = "";
        if (details.mOwner == null)
            details.mOwner = "";
        if (details.mPlacedBy == null)
            details.mPlacedBy = "";
        cursor.close();

        return details;
    }

    public List<GeocacheDetails.Travelbug> getTravelbugs(CharSequence id) {
        openDatabase();
        List<GeocacheDetails.Travelbug> travelbugs = new ArrayList<GeocacheDetails.Travelbug>();
        Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_TRAVELBUGS, TRAVELBUG_COLUMNS,
                "CacheId=?", null, null, null, null, (String)id);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return travelbugs;
        }

        do {
            GeocacheDetails.Travelbug tb = new GeocacheDetails.Travelbug();
            tb.mId = cursor.getString(0);
            tb.mRef = cursor.getString(1);
            tb.mName = cursor.getString(2);
            travelbugs.add(tb);
        } while (cursor.moveToNext());
        cursor.close();

        return travelbugs;
    }

    public List<Waypoint> getRelatedWaypoints(CharSequence parentId) {
        openDatabase();
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_WAYPOINTS, WAYPOINT_COLUMNS,
                "ParentId=?", null, null, null, null, (String)parentId);

        if (!cursor.moveToFirst()) {
            cursor.close();
            Log.d("TreasureHunter", "getRelatedWaypoints: could not move tofirst. ");
            return waypoints;
        }

        do {
            Waypoint wp = mGeocacheFactory.waypointFromCursor(cursor);
            waypoints.add(wp);
        } while (cursor.moveToNext());
        cursor.close();

        // Log.d("LOG", "WPs - RETURNING " + waypoints.size());
        return waypoints;
    }

    /** Returns a sorted list, with the most recent log first. */
    public List<GeocacheDetails.GeocacheLog> getGeocacheLogs(CharSequence id) {
        String[] queryArguments = new String[2];
        queryArguments[0] = (String)id;
        queryArguments[1] = (String)id;
        openDatabase();
        List<GeocacheDetails.GeocacheLog> logs = new ArrayList<GeocacheDetails.GeocacheLog>();
        // Cursor cursor = mSqliteDatabase.query(DatabaseConstants.TBL_LOGS,
        // LOGS_COLUMNS,
        // "CacheId=?", null, null, "LogDate DESC, LogId DESC", null,
        // (String)id);
        Cursor cursor = mSqliteDatabase.rawQuery(DatabaseConstants.SQL_GET_LOGS_AND_USERNOTES,
                queryArguments);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return logs;
        }

        do {
            GeocacheDetails.GeocacheLog log = new GeocacheDetails.GeocacheLog();
            log.mId = cursor.getLong(0);
            log.mLogType = cursor.getInt(1);
            log.mDate = cursor.getString(2);
            log.mFinderName = cursor.getString(3);
            log.mText = cursor.getString(4);
            log.mIsTextEncoded = (cursor.getInt(5) != 0);
            logs.add(log);
        } while (cursor.moveToNext());
        cursor.close();

        return logs;
    }

    public CacheWriter getCacheWriter() {
        if (mCacheWriter != null)
            return mCacheWriter;
        openDatabase();

        mCacheWriter = new CacheWriter(mDatabase, this, mGeocacheFactory);
        return mCacheWriter;
    }

    /** Count all geocaches (and currently waypoints) in the database */
    public int countAll() {
        if (mTotalCacheCount != -1)
            return mTotalCacheCount;

        openDatabase();

        // long start = mClock.getCurrentTime();
        Cursor countCursor;
        countCursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + DatabaseConstants.TBL_CACHES,
                null);
        if (!countCursor.moveToFirst()) {
            Log.w("TreasureHunter", "DbFrontend.countAll() got an empty cursor as result");
            countCursor.close();
            return 0;
        }
        mTotalCacheCount = countCursor.getInt(0);
        countCursor.close();
        // Log.d("TreasureHunter", "DbFrontend.countAll took " +
        // (mClock.getCurrentTime()-start) + " ms ("
        // + mTotalCacheCount + " caches)");
        return mTotalCacheCount;
    }

    /**
     * 'sql' must be a complete SQL query that returns a single row with the
     * result in the first column
     */
    public int countRaw(String sql) {
        openDatabase();
        // long start = mClock.getCurrentTime();

        Cursor countCursor = mDatabase.rawQuery(sql, null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();
        // Log.d("TreasureHunter", "DbFrontend.countRaw took " +
        // (mClock.getCurrentTime()-start) + " ms ("
        // + count + " caches)");
        return count;
    }

    public void flushTotalCount() {
        mTotalCacheCount = -1;
    }

    public List<Integer> getGeocacheTags(CharSequence geocacheId) {
        if (geocacheId == null) // Work-around for unexplained crash
            return new ArrayList<Integer>(0);
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT TagId FROM " + DatabaseConstants.TBL_CACHETAGS
                + " WHERE CacheId='" + geocacheId + "'", null);
        if (cursor == null) {
            return new ArrayList<Integer>(0);
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return new ArrayList<Integer>(0);
        }

        ArrayList<Integer> list = new ArrayList<Integer>(cursor.getCount());
        do {
            list.add(cursor.getInt(0));
        } while (cursor.moveToNext());
        cursor.close();

        return list;
    }

    public boolean geocacheHasTag(CharSequence geocacheId, int tagId) {
        if (geocacheId == null) // Work-around for unexplained crash
            return false;
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT COUNT(*) FROM "
                + DatabaseConstants.TBL_CACHETAGS + " WHERE CacheId='" + geocacheId
                + "' AND TagId=" + tagId, null);
        if (cursor == null) {
            return false;
        }
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return (count > 0);
    }

    public List<String> getCachesMarkedForDeletion() {
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT Id FROM CACHES WHERE DeleteMe = 1", null);
        ArrayList<String> list = new ArrayList<String>(cursor.getCount());
        if (!cursor.moveToFirst()) {
            cursor.close();
            return list;
        }
        do {
            list.add(cursor.getString(0));
        } while (cursor.moveToNext());
        return list;
    }

    /** Sets or clear a tag for a geocache */
    public void setGeocacheTag(CharSequence geocacheId, int tagId, boolean set) {
        openDatabase();
        if (set) {
            mSqliteDatabase.execSQL(DatabaseConstants.SQL_REPLACE_CACHETAG, geocacheId, tagId);
        } else
            mSqliteDatabase.execSQL(DatabaseConstants.SQL_DELETE_CACHETAG, geocacheId, tagId);
        Geocache geocache = mGeocacheFactory.getFromId(geocacheId);
        if (geocache != null) {
            geocache.updateCachedTag(tagId, set);
            geocache.flushIcons();
        }
    }

    public Map<String, Integer> getCountPerSource() {
        openDatabase();

        HashMap<String, Integer> sources = new HashMap<String, Integer>();
        Cursor cursor = mDatabase.rawQuery("SELECT Source, COUNT(*) from CACHES GROUP BY Source",
                EmptyStringArray);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return sources;
        }

        do {
            String name = cursor.getString(0);
            int count = cursor.getInt(1);
            sources.put(name, count);
        } while (cursor.moveToNext());
        cursor.close();

        return sources;
    }

    public boolean isSourceLoaded(String sourceName, String sourceTime) {
        return mDatabase.countResults(DatabaseConstants.TBL_GPX,
                DatabaseConstants.SQL_MATCH_NAME_AND_EXPORTED_LATER, sourceName, sourceTime) > 0;
    }

    public String getDatabasePath() {
        return mDatabaseLocator.getDatabasePath();
    }
}
