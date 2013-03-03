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

package org.treasurehunter.activity.cachelist;

import org.treasurehunter.Clock;
import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.GraphicsGenerator;
import org.treasurehunter.R;
import org.treasurehunter.activity.cachelist.CacheListRowInflater.CacheNameStyler;
import org.treasurehunter.activity.filterlist.FilterTypeCollection;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.cacheactions.CacheAction;
import org.treasurehunter.cacheactions.CacheActionAssignTags;
import org.treasurehunter.cacheactions.CacheActionConfirm;
import org.treasurehunter.cacheactions.CacheActionDelete;
import org.treasurehunter.cacheactions.CacheActionEdit;
import org.treasurehunter.cacheactions.CacheActionSetCenterpoint;
import org.treasurehunter.cacheactions.CacheActionToggleFavorite;
import org.treasurehunter.cacheactions.CacheActionView;
import org.treasurehunter.cacheactions.CacheContextMenu;
import org.treasurehunter.database.CachesProviderCount;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.database.CachesProviderSorted;
import org.treasurehunter.database.CachesProviderToggler;
import org.treasurehunter.database.CachesProviderWaitForInit;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.database.ICachesProviderCenter;
import org.treasurehunter.menuactions.MenuActionAbout;
import org.treasurehunter.menuactions.MenuActionClearCenterpoint;
import org.treasurehunter.menuactions.MenuActionEditFilter;
import org.treasurehunter.menuactions.MenuActionEnableGPS;
import org.treasurehunter.menuactions.MenuActionFilterListPopup;
import org.treasurehunter.menuactions.MenuActionSearchOnline;
import org.treasurehunter.menuactions.MenuActionSettings;
import org.treasurehunter.menuactions.MenuActions;
import org.treasurehunter.task.DelayingTaskRunner;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CacheListTabDI {
    public static CacheListTab create(Activity activity, LayoutInflater layoutInflater,
            GuiState guiState, DbFrontend dbFrontend, GeoFixProvider geoFixProvider,
            GeocacheFactory geocacheFactory, FilterTypeCollection filterTypeCollection,
            BcachingConfig bcachingConfig) {
        final OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        };
        final ErrorDisplayer errorDisplayer = new ErrorDisplayer(activity, onClickListener);
        final BearingFormatter relativeBearingFormatter = new BearingFormatter.Relative();
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);
        final DistanceFormatter distanceFormatter = new DistanceFormatter(sharedPreferences);
        final Resources resources = activity.getResources();

        final GraphicsGenerator graphicsGenerator = new GraphicsGenerator(resources);
        final CacheNameStyler cacheNameAttributes = new CacheNameStyler();
        final CacheListRowInflater geocacheSummaryRowInflater = new CacheListRowInflater(
                distanceFormatter, layoutInflater, relativeBearingFormatter,
                activity.getResources(), graphicsGenerator, dbFrontend, cacheNameAttributes);

        View statusWidget = activity.getLayoutInflater().inflate(R.layout.gps_widget, null);

        final Clock clock = new Clock();
        final GpsStatusWidgetDelegate gpsStatusWidgetDelegate = new GpsStatusWidgetDelegate(
                activity, statusWidget, geoFixProvider, distanceFormatter, clock);

        final GpsWidgetUpdater updateGpsWidgetRunnable = new GpsWidgetUpdater(
                gpsStatusWidgetDelegate, clock);

        GeocacheFilter cacheFilter = guiState.getActiveFilter();
        final CachesProviderDb cachesProviderDb = new CachesProviderDb(dbFrontend);
        cachesProviderDb.setFilter(cacheFilter);
        final ICachesProviderCenter cachesProviderCount = new CachesProviderWaitForInit(
                new CachesProviderCount(cachesProviderDb, 15, 30));
        final CachesProviderSorted cachesProviderSorted = new CachesProviderSorted(
                cachesProviderCount);
        // final CachesProviderLazy cachesProviderLazy = new
        // CachesProviderLazy(cachesProviderSorted, 0.01, 2000, clock);
        // ICachesProviderCenter cachesProviderLazy = cachesProviderSorted;

        final CachesProviderDb cachesProviderAll = new CachesProviderDb(dbFrontend);
        cachesProviderAll.setFilter(cacheFilter);
        final CachesProviderToggler cachesProviderToggler = new CachesProviderToggler(
                cachesProviderSorted, cachesProviderAll);

        LinearLayout layout = (LinearLayout)layoutInflater.inflate(R.layout.cache_list, null);
        ListView listView = (ListView)layout.findViewById(android.R.id.list);

        final TextView emptyTextView = (TextView)layout.findViewById(android.R.id.empty);

        final DelayingTaskRunner listTaskRunner = new DelayingTaskRunner();

        final CacheListAdapter cacheListAdapter = new CacheListAdapter(cachesProviderSorted,
                geocacheSummaryRowInflater, dbFrontend, emptyTextView, listTaskRunner);
        final CacheListUpdater cacheListPositionUpdater = new CacheListUpdater(geoFixProvider,
                cacheListAdapter, cachesProviderCount, cachesProviderToggler, listTaskRunner);
        final CacheListAdapter.ScrollListener scrollListener = new CacheListAdapter.ScrollListener(
                cacheListAdapter);

        final PowerManager powerManager = (PowerManager)activity
                .getSystemService(Context.POWER_SERVICE);
        final WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "Importing");

        // *** BUILD MENU ***
        final MenuActionGetNearbyBcaching menuActionSyncBcaching = new MenuActionGetNearbyBcaching(
                activity, resources, geoFixProvider, dbFrontend, errorDisplayer, guiState,
                wakeLock, bcachingConfig);
        final CacheActionEdit cacheActionEdit = new CacheActionEdit(activity, resources);
        final MenuActions menuActions = new MenuActions();
        menuActions.add(new MenuActionToggleFilter(cachesProviderToggler, resources,
                cacheListPositionUpdater));
        menuActions.add(new MenuActionSearchOnline(errorDisplayer, geocacheFactory, geoFixProvider,
                dbFrontend, resources, activity));
        menuActions.add(new MenuActionEditFilter(activity, guiState, resources));
        menuActions.add(new MenuActionFilterListPopup(activity, filterTypeCollection, resources,
                guiState));
        menuActions.add(new MenuActionMyLocation(errorDisplayer, geocacheFactory, geoFixProvider,
                dbFrontend, resources, activity));
        menuActions.add(menuActionSyncBcaching);
        menuActions.add(new MenuActionSettings(activity, resources));
        menuActions.add(new MenuActionAbout(activity));
        menuActions.add(new MenuActionEnableGPS(activity, resources));
        menuActions.add(new MenuActionClearCenterpoint(activity, cacheListPositionUpdater));

        // *** BUILD CONTEXT MENU ***
        final CacheActionView cacheActionView = new CacheActionView(guiState, resources);
        final CacheActionToggleFavorite cacheActionToggleFavorite = new CacheActionToggleFavorite(
                dbFrontend, guiState);
        final CacheActionDelete cacheActionDelete = new CacheActionDelete(dbFrontend, guiState,
                resources);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final CacheActionAssignTags cacheActionAssignTags = new CacheActionAssignTags(activity,
                dbFrontend, guiState);
        final CacheActionConfirm cacheActionConfirmDelete = new CacheActionConfirm(activity,
                builder, cacheActionDelete, activity.getString(R.string.confirm_delete_title),
                activity.getString(R.string.confirm_delete_body_text));
        final CacheActionSetCenterpoint cacheActionSetCenterpoint = new CacheActionSetCenterpoint(
                activity, cacheListPositionUpdater);

        final CacheAction[] contextActions = new CacheAction[] {
                cacheActionView, cacheActionToggleFavorite, cacheActionEdit, cacheActionAssignTags,
                cacheActionConfirmDelete, cacheActionSetCenterpoint
        };

        final CacheContextMenu contextMenu = new CacheContextMenu(cachesProviderToggler,
                contextActions);

        Drawable icon = resources.getDrawable(R.drawable.ic_menu_bull_list);
        final CacheListTab cacheListDelegate = new CacheListTab(contextMenu,
                geocacheSummaryRowInflater, activity, distanceFormatter, menuActions, layout, icon,
                cacheActionView, cacheListAdapter, geoFixProvider, cacheListPositionUpdater,
                gpsStatusWidgetDelegate, updateGpsWidgetRunnable, cachesProviderDb,
                cachesProviderAll, sharedPreferences);

        listView.addHeaderView(statusWidget);
        listView.setAdapter(cacheListAdapter);
        listView.setEmptyView(emptyTextView);

        listView.setOnCreateContextMenuListener(contextMenu);
        listView.setOnScrollListener(scrollListener);

        listView.setOnItemClickListener(cacheListDelegate);

        return cacheListDelegate;
    }
}
