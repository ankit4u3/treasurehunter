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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** This class handles the location of the database */
public class DatabaseLocator {
    private static final String DEFAULT_DB_NAME = "Treasurehunter.db";

    private Context mContext;

    private SharedPreferences mSharedPreferences;

    public DatabaseLocator(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /** @return True on success */
    private static boolean copyFile(File source, File destination) {
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            Log.e("TreasureHunter", "FileNotFoundException " + ex.getMessage());
            return false;
        } catch (IOException e) {
            Log.e("TreasureHunter", "IOException " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Moves the database from its old location on internal storage to external
     * storage.
     */
    public void moveOldDatabase() {
        File newFile = new File(getDatabasePath());

        File oldFile = mContext.getDatabasePath("TreasureHUnterOld.db");
        // Log.d("TreasureHunter", "Old database exists="+oldFile.exists() +
        // ", New db exists="+newFile.exists());

        if (oldFile.exists() && !newFile.exists()) {
            Log.i("TreasureHunter", "Moving " + oldFile.getPath() + " to " + newFile);
            boolean success = copyFile(oldFile, newFile);
            if (success) {
                Log.i("TreasureHunter", "Has copied " + oldFile.getPath() + " to " + newFile);
                boolean couldDelete = oldFile.delete();
                if (couldDelete) {
                    Log.i("TreasureHunter", "Deleted old database " + oldFile.getPath());
                } else {
                    Log.e("TreasureHunter", "Could not delete old database " + oldFile.getPath());
                }
            } else
                Log.e("TreasureHunter", "Could not copy " + oldFile.getPath() + " to " + newFile);
        }
    }

    public static String getStoragePath() {
        File externalStorage = Environment.getExternalStorageDirectory();
        return externalStorage.getPath() + "/TreasureHunter";
    }

    /**
     * @return true if the dir exists or if it was created
     */
    public static boolean createStorageDirectory() {
        File dir = new File(getStoragePath());
        if (dir.exists())
            return true;

        try {
            dir.mkdir();
            Log.d("TreasureHunter", "Created directory " + Geocache.TreasureHunter_DIR);
        } catch (SecurityException ex) {
            Log.w("TreasureHunter", "Not allowed to create directory "
                    + Geocache.TreasureHunter_DIR);
            return false;
        }
        return true;
    }

    public String[] getDatabaseList() {
        String[] result = null;

        final FilenameFilter dbFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".db");
            }
        };

        if (createStorageDirectory()) {
            File dir = new File(getStoragePath());
            result = dir.list(dbFilter);
        }

        return result;
    }

    public String getDatabasePath() {
        return getStoragePath() + "/"
                + mSharedPreferences.getString("use-database", DEFAULT_DB_NAME);
    }

    public String getDefaultDatabaseName() {
        return DEFAULT_DB_NAME;
    }
}
