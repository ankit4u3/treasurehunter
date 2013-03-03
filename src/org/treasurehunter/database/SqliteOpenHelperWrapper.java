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

import android.database.sqlite.SQLiteDatabase;

public class SqliteOpenHelperWrapper extends GeneralizedSQLiteOpenHelper {

    public SqliteOpenHelperWrapper(String path) {
        super(path, null, DatabaseConstants.DATABASE_VERSION);
        // Log.d("TreasureHunter", "SqliteOpenHelperWrapper opening " + path);
    }

    public SQLiteDatabaseWrapper getWritableSqliteWrapper() {
        return new SQLiteDatabaseWrapper(this.getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final SQLiteDatabaseWrapper sqliteWrapper = new SQLiteDatabaseWrapper(db);
        DatabaseInitializer.onCreate(sqliteWrapper);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final SQLiteDatabaseWrapper sqliteWrapper = new SQLiteDatabaseWrapper(db);
        DatabaseInitializer.onUpgrade(sqliteWrapper, oldVersion);
    }
}
