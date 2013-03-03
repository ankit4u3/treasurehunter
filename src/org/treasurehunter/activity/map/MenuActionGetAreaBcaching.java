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

package org.treasurehunter.activity.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.R;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.bcaching.BcachingCommunication;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.bcaching.ImportBcachingTask;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;
import org.treasurehunter.task.ITaskRunner;
import org.treasurehunter.xmlimport.ProcessStatus;
import org.treasurehunter.xmlimport.ProgressDialogWrapper;
import org.treasurehunter.xmlimport.ProgressDialogWrapper.ProcessFinishedListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MenuActionGetAreaBcaching extends StaticLabelMenu implements MenuAction {
    private final Context mContext;

    private final DbFrontend mDbFrontend;

    private final ErrorDisplayer mErrorDisplayer;

    private final GeocacheFactory mGeocacheFactory;

    private final MapView mMapView;

    private final GuiState mGuiState;

    private final WakeLock mWakeLock;

    private final BcachingConfig mBcachingConfig;

    private final ITaskRunner mTaskRunner;

    public MenuActionGetAreaBcaching(Context context, Resources resources, DbFrontend dbFrontend,
            ErrorDisplayer errorDisplayer, GeocacheFactory geocacheFactory, MapView mapView,
            GuiState guiState, WakeLock wakeLock, BcachingConfig bcachingConfig,
            ITaskRunner taskRunner) {
        super(resources, R.string.menu_get_area_bcaching);
        mContext = context;
        mMapView = mapView;
        mDbFrontend = dbFrontend;
        mErrorDisplayer = errorDisplayer;
        mGeocacheFactory = geocacheFactory;
        mGuiState = guiState;
        mWakeLock = wakeLock;
        mBcachingConfig = bcachingConfig;
        mTaskRunner = taskRunner;
    }

    @Override
    public void act() {
        Projection projection = mMapView.getProjection();
        GeoPoint newTopLeft = projection.fromPixels(0, 0);
        GeoPoint newBottomRight = projection.fromPixels(mMapView.getRight(), mMapView.getBottom());

        double latLow = newBottomRight.getLatitudeE6() / 1.0E6;
        double latHigh = newTopLeft.getLatitudeE6() / 1.0E6;
        double lonLow = newTopLeft.getLongitudeE6() / 1.0E6;
        double lonHigh = newBottomRight.getLongitudeE6() / 1.0E6;

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
                // mLocationControl.onResume();
                mWakeLock.release();
                if (!success)
                    return;

                mGeocacheFactory.flushCacheTagsAndIcons();
                mDbFrontend.flushTotalCount();
                // mCacheList.forceRefresh();
                mGuiState.notifyDataViewChanged();
            }
        };

        ProgressDialogWrapper progressHandler = new ProgressDialogWrapper(mContext,
                finishedListener);
        ProcessStatus processStatus = new ProcessStatus(progressHandler, cacheWriter, false);

        progressHandler.show(mContext.getString(R.string.bcaching_synching),
                mContext.getString(R.string.please_wait), 1);
        ImportBcachingTask process = new ImportBcachingTask(latLow, lonLow, latHigh, lonHigh, 2000,
                processStatus, comm, mErrorDisplayer, mContext, gcUsername, progressHandler,
                mBcachingConfig, mDbFrontend);
        progressHandler.setTask(process);
        mTaskRunner.runTask(process);
    }

    @Override
    public boolean isEnabled() {
        return mBcachingConfig.isSetup();
    }
}
