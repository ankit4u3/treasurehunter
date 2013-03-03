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
import org.treasurehunter.Tags;

import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

public class DatabaseInitializer {
    public static void onCreate(ISQLiteDatabase db) {
        Log.i("TreasureHunter", "database onCreate version " + DatabaseConstants.DATABASE_VERSION);
        db.execSQL(DatabaseConstants.SQL_CREATE_CACHE_TABLE_V17);
        db.execSQL(DatabaseConstants.SQL_CREATE_GPX_TABLE_V10);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_LATITUDE);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_LONGITUDE);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_SOURCE);
        db.execSQL(DatabaseConstants.SQL_CREATE_CACHETAGS_TABLE_V14);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_CACHETAGS);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_CACHETAGS_CACHEID);
        db.execSQL(DatabaseConstants.SQL_CREATE_LOGS_TABLE);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_LOGS_CACHEID);
        db.execSQL(DatabaseConstants.SQL_CREATE_TB_TABLE);
        db.execSQL(DatabaseConstants.SQL_CREATE_IDX_TB_CACHEID);
        db.execSQL(DatabaseConstants.SQL_CREATE_USERNOTES_TABLE);
        db.execSQL(DatabaseConstants.SQL_CREATE_WAYPOINTS_TABLE);
    }

    @SuppressWarnings("deprecation")
    public static void onUpgrade(ISQLiteDatabase db, int oldVersion) {
        Log.i("TreasureHunter", "database onUpgrade oldVersion=" + oldVersion);
        if (oldVersion < 9) {
            db.execSQL(DatabaseConstants.SQL_DROP_CACHE_TABLE);
            db.execSQL(DatabaseConstants.SQL_CREATE_CACHE_TABLE_V08);
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_LATITUDE);
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_LONGITUDE);
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_SOURCE);
        }
        if (oldVersion < 10) {
            db.execSQL("ALTER TABLE CACHES ADD COLUMN " + DatabaseConstants.S0_COLUMN_DELETE_ME);
            db.execSQL(DatabaseConstants.SQL_CREATE_GPX_TABLE_V10);
        }
        if (oldVersion < 11) {
            db.execSQL("ALTER TABLE CACHES ADD COLUMN " + DatabaseConstants.S0_COLUMN_CACHE_TYPE);
            db.execSQL("ALTER TABLE CACHES ADD COLUMN " + DatabaseConstants.S0_COLUMN_CONTAINER);
            db.execSQL("ALTER TABLE CACHES ADD COLUMN " + DatabaseConstants.S0_COLUMN_DIFFICULTY);
            db.execSQL("ALTER TABLE CACHES ADD COLUMN " + DatabaseConstants.S0_COLUMN_TERRAIN);
            // This date has to precede 2000-01-01 (due to a bug in
            // CacheTagSqlWriter.java in v10).
            db.execSQL("UPDATE GPX SET ExportTime = \"1990-01-01\"");
        }
        if (oldVersion == 12) {
            db.execSQL("DROP TABLE IF EXISTS LABELS");
            db.execSQL("DROP TABLE IF EXISTS CACHELABELS");
        }
        if (oldVersion < 13) {
            Log.i("TreasureHunter", "Upgrading database to v13");
            db.execSQL(DatabaseConstants.SQL_CREATE_TAGS_TABLE_V12);
            db.execSQL(DatabaseConstants.SQL_REPLACE_TAG, Tags.FOUND, "Found", true);
            db.execSQL(DatabaseConstants.SQL_REPLACE_TAG, Tags.DNF, "DNF", true);
            db.execSQL(DatabaseConstants.SQL_REPLACE_TAG, Tags.FAVORITE, "Favorites", true);

            db.execSQL(DatabaseConstants.SQL_CREATE_CACHETAGS_TABLE_V12);
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_CACHETAGS);
        }
        if (oldVersion < 14) {
            Log.i("TreasureHunter", "Upgrading database to v14 (add primary key to CACHETAGS)");
            db.execSQL("CREATE TABLE temp AS SELECT DISTINCT * FROM cachetags");
            db.execSQL("DROP TABLE cachetags");
            db.execSQL(DatabaseConstants.SQL_CREATE_CACHETAGS_TABLE_V14);
            db.execSQL("INSERT INTO cachetags SELECT * FROM temp");
            db.execSQL("DROP TABLE temp");
        }
        if (oldVersion < 15) {
            Log.i("TreasureHunter", "Upgrading database to v15");
            db.execSQL(DatabaseConstants.SQL_CREATE_LOGS_TABLE);
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_LOGS_CACHEID);
            db.execSQL(DatabaseConstants.SQL_CREATE_TB_TABLE);
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_TB_CACHEID);
            db.execSQL("ALTER TABLE CACHES ADD COLUMN ShortDesc VARCHAR");
            db.execSQL("ALTER TABLE CACHES ADD COLUMN LongDesc VARCHAR");
            db.execSQL("ALTER TABLE CACHES ADD COLUMN Hints VARCHAR");
            db.execSQL("ALTER TABLE CACHES ADD COLUMN LastModifiedDate DATETIME");
            db.execSQL(DatabaseConstants.SQL_CREATE_IDX_CACHETAGS_CACHEID);
            db.execSQL(DatabaseConstants.SQL_DELETE_ALL_TAGS, Tags.CONTAINS_TRAVELBUG);

            final FilenameFilter htmlFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".html");
                }
            };
            Thread deleteHtml = new Thread("DeleteHTML") {
                @Override
                public void run() {
                    Log.d("TreasureHunter", "Will delete all *.html files");
                    File dir = new File(Geocache.TreasureHunter_DIR);
                    for (File file : dir.listFiles(htmlFilter))
                        file.delete();
                    Log.i("TreasureHunter", "Finished deleting all *.html files");
                }
            };
            deleteHtml.setPriority(Thread.MIN_PRIORITY);
            deleteHtml.start();
        }
        if (oldVersion < 16) {
            Log.i("TreasureHunter", "Upgrading database to v16");
            db.execSQL("ALTER TABLE CACHES ADD COLUMN CreationDate DATETIME");
            db.execSQL("ALTER TABLE CACHES ADD COLUMN Owner VARCHAR");
        }
        if (oldVersion < 17) {
            Log.i("TreasureHunter", "Upgrading database to v17");
            db.execSQL("ALTER TABLE CACHES ADD COLUMN PlacedBy VARCHAR");
        }
        if (oldVersion < 18) {
            Log.i("TreasureHunter", "Upgrading database to v18");
            db.execSQL(DatabaseConstants.SQL_CREATE_USERNOTES_TABLE);
        }
        if (oldVersion < 20) {
            Log.i("TreasureHunter", "Upgrading database to v20");
            try {
                db.execSQL(DatabaseConstants.SQL_CREATE_WAYPOINTS_TABLE);
            } catch (SQLiteException ex) {
                Log.i("TreasureHunter", "Upgrade to v20 exception");
            }
        }
    }
}
