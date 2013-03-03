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
import org.treasurehunter.R;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.bcaching.BcachingCommunication;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.bcaching.UpdateFromBcachingTask;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;
import org.treasurehunter.xmlimport.ProcessStatus;
import org.treasurehunter.xmlimport.ProgressDialogWrapper;
import org.treasurehunter.xmlimport.ProgressDialogWrapper.ProcessFinishedListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Updates all caches from bcaching.com that was changed after the last update
 * time.
 */
public class MenuActionUpdateBcaching extends StaticLabelMenu implements MenuAction {
    private final Context mContext;

    private final GeoFixProvider mLocationControl;

    private final DbFrontend mDbFrontend;

    private final ErrorDisplayer mErrorDisplayer;

    private final GuiState mGuiState;

    private final BcachingConfig mBcachingConfig;

    private final WakeLock mWakeLock;

    private UpdateFromBcachingTask mProcess;

    public MenuActionUpdateBcaching(Context context, Resources resources,
            GeoFixProvider locationControl, DbFrontend dbFrontend, ErrorDisplayer errorDisplayer,
            GuiState guiState, BcachingConfig bcachingConfig, WakeLock wakeLock) {
        super(resources, R.string.menu_update_from_bcaching);
        mContext = context;
        mLocationControl = locationControl;
        mDbFrontend = dbFrontend;
        mErrorDisplayer = errorDisplayer;
        mGuiState = guiState;
        mBcachingConfig = bcachingConfig;
        mWakeLock = wakeLock;
    }

    @Override
    public void act() {
        String username = mBcachingConfig.getUsername();
        String password = mBcachingConfig.getPassword();
        if (username.equals("") || password.equals("")) {
            Toast t = Toast.makeText(mContext, R.string.bcaching_set_user_pwd, 4);
            t.show();
            return;
        }

        BcachingCommunication comm = new BcachingCommunication(username, password);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        String gcUsername = sharedPreferences.getString("username", null);
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        mWakeLock.acquire();

        ProcessFinishedListener finishedListener = new ProcessFinishedListener() {
            @Override
            public void onFinished(boolean success) {
                if (success) {
                    mBcachingConfig.setLastUpdateToNow();
                    mGuiState.notifyDataViewChanged();
                }
                mWakeLock.release();
                if (!mProcess.isAborted())
                    mLocationControl.startUpdates();
            }
        };

        ProgressDialogWrapper progressHandler = new ProgressDialogWrapper(mContext,
                finishedListener);
        ProcessStatus processStatus = new ProcessStatus(progressHandler, cacheWriter, false);

        mLocationControl.stopUpdates();
        progressHandler.show(mContext.getString(R.string.bcaching_updating),
                mContext.getString(R.string.please_wait), 1);
        mProcess = new UpdateFromBcachingTask(processStatus, comm, mErrorDisplayer, gcUsername,
                mBcachingConfig);
        progressHandler.setTask(mProcess);
        Log.d("TreasureHunter", "sync dir will abortAndJoin");
        mBcachingConfig.getTaskRunner().abort();
        mBcachingConfig.getTaskRunner().clearEnqueued();
        Log.d("TreasureHunter", "sync dir will run task");
        mBcachingConfig.getTaskRunner().runTask(mProcess);
    }

    @Override
    public boolean isEnabled() {
        return mBcachingConfig.isSetup();
    }
}
