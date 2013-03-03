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
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.R;
import org.treasurehunter.SourceFactory;
import org.treasurehunter.activity.cachelist.MenuActionGetNearbyBcaching;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.activity.main.TabBase;
import org.treasurehunter.bcaching.BcachingCommunication;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.MenuActionClearTagNew;
import org.treasurehunter.menuactions.MenuActionConfirm;
import org.treasurehunter.menuactions.MenuActionDeleteAll;
import org.treasurehunter.menuactions.MenuActionSettings;
import org.treasurehunter.menuactions.MenuActions;
import org.treasurehunter.task.ConcurrentTaskRunner;
import org.treasurehunter.task.Task;
import org.treasurehunter.task.TaskQueueRunner;
import org.treasurehunter.task.TaskStarterTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SourcesTab extends TabBase implements OnCreateContextMenuListener {

    public static SourcesTab create(Activity activity, LayoutInflater layoutInflater,
            DbFrontend dbFrontend, BcachingConfig bcachingConfig, GuiState guiState,
            GeoFixProvider geoFixProvider, GeocacheFactory geocacheFactory) {

        LinearLayout layout = (LinearLayout)layoutInflater.inflate(R.layout.database, null);
        ListView listView = (ListView)layout.findViewById(android.R.id.list);
        final TextView emptyTextView = (TextView)layout.findViewById(android.R.id.empty);

        String emptyMessage = activity.getString(R.string.sources_list_empty);
        String calculatingMessage = activity.getString(R.string.sources_list_calculating);
        SourceListAdapter sourceListAdapter = new SourceListAdapter(null, emptyTextView,
                emptyMessage, calculatingMessage, layoutInflater);

        listView.setAdapter(sourceListAdapter);
        listView.setEmptyView(emptyTextView);

        // Future additions:
        // listView.addHeaderView(statusWidget);
        listView.setOnItemClickListener(sourceListAdapter);

        Resources resources = activity.getResources();

        Drawable icon = resources.getDrawable(R.drawable.ic_menu_db);

        final OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        };
        final ErrorDisplayer errorDisplayer = new ErrorDisplayer(activity, onClickListener);
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);

        final PowerManager powerManager = (PowerManager)activity
                .getSystemService(Context.POWER_SERVICE);
        final WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "Importing");

        final ConcurrentTaskRunner sourceTaskPool = new ConcurrentTaskRunner();
        final TaskQueueRunner taskQueue = new TaskQueueRunner();

        final MenuActions menuActions = new MenuActions();
        final MenuActionSyncDirectory syncDirectory = new MenuActionSyncDirectory(activity,
                dbFrontend, errorDisplayer, geoFixProvider, geocacheFactory, guiState,
                sharedPreferences, wakeLock, taskQueue);
        menuActions.add(syncDirectory);
        final MenuActionUpdateBcaching menuActionUpdateBcaching = new MenuActionUpdateBcaching(
                activity, resources, geoFixProvider, dbFrontend, errorDisplayer, guiState,
                bcachingConfig, wakeLock);
        final MenuActionGetNearbyBcaching menuActionSyncBcaching = new MenuActionGetNearbyBcaching(
                activity, resources, geoFixProvider, dbFrontend, errorDisplayer, guiState,
                wakeLock, bcachingConfig);
        menuActions.add(menuActionUpdateBcaching);
        menuActions.add(new MenuActionClearTagNew(dbFrontend, guiState));
        final MenuActionDeleteAll deleteAll = new MenuActionDeleteAll(dbFrontend, guiState,
                bcachingConfig, resources);
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
        final MenuAction confirmDeleteAll = new MenuActionConfirm(activity, builder1, deleteAll,
                resources.getString(R.string.delete_all_caches),
                resources.getString(R.string.confirm_delete_all_caches));
        menuActions.add(confirmDeleteAll);
        menuActions.add(new MenuActionSettings(activity, resources));

        final MenuActionNewDatabase menuActionNewDatabase = new MenuActionNewDatabase(activity,
                dbFrontend, resources, R.string.menu_create_new_database);
        menuActions.add(menuActionNewDatabase);

        final SourceRowFactory sourceRowFactory = new SourceRowFactory(guiState, activity,
                dbFrontend, confirmDeleteAll, menuActionUpdateBcaching, menuActionSyncBcaching);

        SourcesTab sourcesTab = new SourcesTab(layout, icon, menuActions, sourceTaskPool,
                taskQueue, dbFrontend, sourceListAdapter, bcachingConfig, sourceRowFactory,
                geocacheFactory.getSourceFactory(), sharedPreferences, resources);

        sourceRowFactory.setSourcesTab(sourcesTab);

        listView.setOnCreateContextMenuListener(sourcesTab);

        return sourcesTab;
    }

    // ///////////////////////////////////////////////////////////////

    private final TaskQueueRunner mTaskQueue;

    private final DbFrontend mDbFrontend;

    private final SourceListAdapter mSourceListAdapter;

    private final BcachingConfig mBcachingConfig;

    private final SourceRowFactory mSourceRowFactory;

    private final SourceFactory mSourceFactory;

    private final SharedPreferences mSharedPreferences;

    private final Resources mResources;

    private boolean mDataChanged = true;

    private SourceContextMenu mContextMenu;

    public SourcesTab(View contentView, Drawable icon, MenuActions menuActions,
            ConcurrentTaskRunner taskPool, TaskQueueRunner taskQueue, DbFrontend dbFrontend,
            SourceListAdapter sourceListAdapter, BcachingConfig bcachingConfig,
            SourceRowFactory sourceRowFactory, SourceFactory sourceFactory,
            SharedPreferences sharedPreferences, Resources resources) {
        super("tab_sources", getIconLabel(sharedPreferences), contentView, icon, menuActions);
        mTaskQueue = taskQueue;
        mDbFrontend = dbFrontend;
        mSourceListAdapter = sourceListAdapter;
        mBcachingConfig = bcachingConfig;
        mSourceRowFactory = sourceRowFactory;
        mSourceFactory = sourceFactory;
        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    private static String getIconLabel(SharedPreferences preferences) {
        return preferences.getBoolean("ui_show_tab_texts", true) ? "Data" : "";
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onResume() {
        if (mDataChanged) {
            startUpdatingData();
        }
    }

    /** Recalculates everything in the Data list */
    public void startUpdatingData() {
        mTaskQueue.clearEnqueued();
        Task populateTask = new PopulateSourcesTask(mSourceListAdapter, mDbFrontend,
                mBcachingConfig, mSourceRowFactory, mSourceFactory);
        mTaskQueue.runTask(populateTask);
        String bcachingName = mBcachingConfig.getUsername();
        String bcachingPwd = mBcachingConfig.getPassword();
        BcachingCommunication comm = new BcachingCommunication(bcachingName, bcachingPwd);
        Task checkBcachingTask = new CheckBcachingTask(comm, mBcachingConfig, mSourceListAdapter,
                mSourceRowFactory, mResources);
        mTaskQueue.runTask(new TaskStarterTask(checkBcachingTask, mBcachingConfig.getTaskRunner()));
        final String dirName = MenuActionSyncDirectory.getSyncDir(mSharedPreferences);
        Task checkDirectoryTask = new CheckDirectoryTask(dirName, mSourceFactory,
                mSourceListAdapter, mSourceRowFactory, mDbFrontend, mResources);
        mTaskQueue.runTask(checkDirectoryTask);
        mDataChanged = false;
    }

    @Override
    public void onPause() {
        mTaskQueue.abort();
    }

    @Override
    public void onDataViewChanged(GeocacheFilter filter, boolean isTabActive) {
        if (isTabActive) {
            startUpdatingData();
        } else {
            mDataChanged = true;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo acmi = (AdapterContextMenuInfo)menuInfo;
        SourceRow sourceRow = mSourceListAdapter.getSourceRows().get(acmi.position);
        mContextMenu = sourceRow.getContextMenu();
        mContextMenu.applyTo(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        return mContextMenu.onContextItemSelected(menuItem);
    }
}
