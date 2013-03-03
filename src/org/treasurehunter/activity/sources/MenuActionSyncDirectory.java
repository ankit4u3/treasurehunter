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

package org.treasurehunter.activity.sources;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.R;
import org.treasurehunter.Tags;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.task.TaskQueueRunner;
import org.treasurehunter.xmlimport.ImportDirectoryTask;
import org.treasurehunter.xmlimport.ProcessStatus;
import org.treasurehunter.xmlimport.ProgressDialogWrapper;
import org.treasurehunter.xmlimport.ProgressDialogWrapper.ProcessFinishedListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.io.File;

public class MenuActionSyncDirectory implements MenuAction {
    private final Context mContext;

    private final DbFrontend mDbFrontend;

    private final ErrorDisplayer mErrorDisplayer;

    /** Used to turn off all listening to location changes during the import */
    private final GeoFixProvider mGeoFixProvider;

    private final GeocacheFactory mGeocacheFactory;

    private final GuiState mGuiState;

    private final SharedPreferences mSharedPreferences;

    private final WakeLock mWakeLock;

    private TaskQueueRunner mTaskRunner;

    public MenuActionSyncDirectory(Context context, DbFrontend dbFrontend,
            ErrorDisplayer errorDisplayer, GeoFixProvider geoFixProvider,
            GeocacheFactory geocacheFactory, GuiState guiState,
            SharedPreferences sharedPreferences, WakeLock wakeLock, TaskQueueRunner taskRunner) {
        mContext = context;
        mDbFrontend = dbFrontend;
        mErrorDisplayer = errorDisplayer;
        mGeoFixProvider = geoFixProvider;
        mGeocacheFactory = geocacheFactory;
        mSharedPreferences = sharedPreferences;
        mGuiState = guiState;
        mWakeLock = wakeLock;
        mTaskRunner = taskRunner;
    }

    public static String getSyncDir(SharedPreferences sharedPreferences) {
        final String syncdir = sharedPreferences.getString("syncdir", "");
        if (syncdir.equals("")) {
            File externalStorage = Environment.getExternalStorageDirectory();
            return externalStorage.getPath() + "/download";
        }
        return syncdir;
    }

    private boolean mIsPausing = false;

    @Override
    public void act() {
        Log.d("TreasureHunter", "Starting SyncDirectory");
        mIsPausing = false;
        // Only the most recent batch of caches is shown as 'new'
        final String username = mSharedPreferences.getString("username", "");
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        cacheWriter.clearTagForAllCaches(Tags.NEW);
        mWakeLock.acquire();

        ProcessFinishedListener finishedListener = new ProcessFinishedListener() {
            @Override
            public void onFinished(boolean success) {
                Log.d("TreasureHunter", "SyncDirectory onFinished success=" + success);
                if (success)
                    mGuiState.notifyDataViewChanged();
                mWakeLock.release();
                if (!mIsPausing)
                    mGeoFixProvider.startUpdates();
            }
        };

        ProgressDialogWrapper progressHandler = new ProgressDialogWrapper(mContext,
                finishedListener);
        progressHandler.show(
                mContext.getString(R.string.sync_dir_format, getSyncDir(mSharedPreferences)),
                mContext.getString(R.string.please_wait));

        ProcessStatus processStatus = new ProcessStatus(progressHandler, cacheWriter, true);

        mGeoFixProvider.stopUpdates();
        ImportDirectoryTask importTask = new ImportDirectoryTask(processStatus,
                getSyncDir(mSharedPreferences), mErrorDisplayer,
                mGeocacheFactory.getSourceFactory(), username);
        progressHandler.setTask(importTask);
        Log.d("TreasureHunter", "SyncDirectory will abortAndJoin");
        mTaskRunner.abort();
        mTaskRunner.clearEnqueued();
        Log.d("TreasureHunter", "SyncDirectory will run task");
        mTaskRunner.runTask(importTask);
    }

    @Override
    public String getLabel() {
        return mContext.getString(R.string.sync_dir_format, getSyncDir(mSharedPreferences));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
