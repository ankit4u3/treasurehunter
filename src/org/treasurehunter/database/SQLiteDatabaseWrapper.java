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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteDatabaseWrapper implements ISQLiteDatabase {
    private final SQLiteDatabase mSQLiteDatabase;

    public SQLiteDatabaseWrapper(SQLiteDatabase writableDatabase) {
        mSQLiteDatabase = writableDatabase;
    }

    @Override
    public void beginTransaction() {
        mSQLiteDatabase.beginTransaction();
    }

    @Override
    public int countResults(String table, String selection, String... selectionArgs) {
        Cursor cursor = mSQLiteDatabase.query(table, null, selection, selectionArgs, null, null,
                null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public void endTransaction() {
        mSQLiteDatabase.endTransaction();
    }

    public void execSQL(String sql) {
        // Log.d("TreasureHunter", "SQL: " + sql);
        mSQLiteDatabase.execSQL(sql);
    }

    @Override
    public void execSQL(String sql, Object... bindArgs) {
        // Log.d("TreasureHunter", "SQL: " + sql + ", " +
        // Arrays.toString(bindArgs));
        mSQLiteDatabase.execSQL(sql, bindArgs);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String groupBy,
            String having, String orderBy, String limit, String... selectionArgs) {
        final Cursor query = mSQLiteDatabase.query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        // Log.d("TreasureHunter", "limit: " + limit + ", query: " + selection);
        return query;
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mSQLiteDatabase.rawQuery(sql, selectionArgs);
    }

    @Override
    public void setTransactionSuccessful() {
        mSQLiteDatabase.setTransactionSuccessful();
    }

    @Override
    public void close() {
        Log.d("TreasureHunter", "----------closing sqlite------");
        mSQLiteDatabase.close();
    }

    @Override
    public boolean isOpen() {
        return mSQLiteDatabase.isOpen();
    }
}
