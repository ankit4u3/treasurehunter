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

package org.treasurehunter.activity.main;

import com.google.android.maps.MapActivity;

import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeoFixProviderDI;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.R;
import org.treasurehunter.Toaster;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.cachelist.CacheListTab;
import org.treasurehunter.activity.cachelist.CacheListTabDI;
import org.treasurehunter.activity.compass.CompassTabDI;
import org.treasurehunter.activity.details.DetailsTabDI;
import org.treasurehunter.activity.filterlist.FilterTypeCollection;
import org.treasurehunter.activity.map.MapTabDI;
import org.treasurehunter.activity.sources.SourcesTab;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.database.DatabaseLocator;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.task.TaskQueueRunner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

//MapView can only be used from within a MapActivity, so the main activity
//must be a MapActivity. TabActivity didn't add much functionality anyway.
public class MainActivity extends MapActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private DbFrontend mDbFrontend;

    private GuiState mGuiState;

    private TabHostWrapper mTabHostWrapper;

    private IntentHandler mIntentHandler;

    private Toaster mSdcardErrorToaster;

    private TaskQueueRunner mBcachingTaskRunner;

    private SearchIntentHandler mSearchIntentHandler;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (mSharedPreferences.getBoolean("debug_trace", false))
            Debug.startMethodTracing("TreasureHunter");

        mSdcardErrorToaster = new Toaster(this, R.string.toaster_sdcard_error, true);

        boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!mounted) {
            Log.e("TreasureHunter",
                    "External storage in state '" + Environment.getExternalStorageState()
                            + "': Not starting TreasureHunter");
            mSdcardErrorToaster.showToast();
            this.finish(); // Don't start TreasureHunter at all
            return;
        }

        if (!DatabaseLocator.createStorageDirectory()) {
            mSdcardErrorToaster.showToast();
            this.finish(); // Don't start TreasureHunter at all
        }

        DatabaseLocator databaseLocator = new DatabaseLocator(this);
        databaseLocator.moveOldDatabase();

        LayoutInflater layoutInflater = getLayoutInflater();
        final TabHost tabHost = (TabHost)layoutInflater.inflate(R.layout.tabs, null);
        tabHost.setup();
        setContentView(tabHost);

        GeocacheFactory geocacheFactory = new GeocacheFactory();
        mDbFrontend = new DbFrontend(databaseLocator, geocacheFactory);
        final GeoFixProvider geoFixProvider = GeoFixProviderDI.create(this);
        FilterTypeCollection filterTypeCollection = new FilterTypeCollection(this);

        mTabHostWrapper = new TabHostWrapper(tabHost, geoFixProvider);
        mGuiState = new GuiState(mDbFrontend, mTabHostWrapper, filterTypeCollection,
                geocacheFactory);
        // SourceFactory sourceFactory = geocacheFactory.getSourceFactory();

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mBcachingTaskRunner = new TaskQueueRunner();
        final BcachingConfig bcachingConfig = new BcachingConfig(mSharedPreferences,
                mBcachingTaskRunner);

        CacheListTab cacheListTab;
        mTabHostWrapper.addTab(cacheListTab = CacheListTabDI.create(this, layoutInflater,
                mGuiState, mDbFrontend, geoFixProvider, geocacheFactory, filterTypeCollection,
                bcachingConfig));
        mTabHostWrapper.addTab(MapTabDI.create(this, mGuiState, geocacheFactory, mDbFrontend,
                filterTypeCollection, geoFixProvider, bcachingConfig, mSharedPreferences));
        mTabHostWrapper.addTab(CompassTabDI.create(this, mGuiState, mDbFrontend, geoFixProvider,
                layoutInflater));
        mTabHostWrapper.addTab(DetailsTabDI.create(this, mGuiState, mDbFrontend, layoutInflater,
                mSharedPreferences));
        mTabHostWrapper.addTab(SourcesTab.create(this, layoutInflater, mDbFrontend, bcachingConfig,
                mGuiState, geoFixProvider, geocacheFactory));

        mGuiState.onCreate(savedInstanceState);
        mTabHostWrapper.onCreate(savedInstanceState);

        mIntentHandler = new IntentHandler(mGuiState, geocacheFactory, mDbFrontend);
        mIntentHandler.handleIntent(getIntent());

        mSearchIntentHandler = new SearchIntentHandler(cacheListTab);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mGuiState.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TreasureHunter", "MainActivity.onResume()");

        boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!mounted) {
            Log.e("TreasureHunter",
                    "External storage in state '" + Environment.getExternalStorageState()
                            + "': Not resuming TreasureHunter");
            mSdcardErrorToaster.showToast();
            this.finish(); // Don't start TreasureHunter at all
            return;
        }

        mDbFrontend.openDatabase();
        mTabHostWrapper.onResume();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return (mTabHostWrapper.onContextItemSelected(item) || super.onContextItemSelected(item));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
        // return mGuiState.onCreateOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onMenuOpened(int, android.view.Menu)
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        super.onMenuOpened(featureId, menu);
        return mTabHostWrapper.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return mTabHostWrapper.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mTabHostWrapper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("TreasureHunter",
                "onNewIntent action=" + intent.getAction() + " data=" + intent.getData());
        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            mSearchIntentHandler.handleSearchIntent(intent);
        } else {
            mIntentHandler.handleIntent(intent);
        }
    }

    // Coming back from the Edit screen or camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TreasureHunter", "MainActivity.onActivityResult()");

        if (data != null && data.getBooleanExtra(Geocache.NAVIGATE_TO_NEW_CACHE, false)) {
            String wptId = data.getStringExtra(Geocache.WAYPOINTID);
            if (wptId != null && !wptId.equals("")) {
                final Waypoint waypoint = mDbFrontend.loadWaypointFromId(wptId);
                mGuiState.showCompass(waypoint);
            } else {
                String id = data.getStringExtra(Geocache.ID);
                if (id != null && !id.equals("")) {
                    final Geocache geocache = mDbFrontend.loadCacheFromId(id);
                    mGuiState.showCompass(geocache);
                }
            }
        }

        mGuiState.notifyDataViewChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TreasureHunter", "MainActivity.onPause() " + this);
        mBcachingTaskRunner.clearEnqueued();
        mBcachingTaskRunner.abort();
        mTabHostWrapper.onPause();
        mDbFrontend.closeDatabase();
        Debug.stopMethodTracing();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mTabHostWrapper.onKeyDown(keyCode, event))
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        Log.d("TreasureHunter", "MainActivity.onConfigurationChanged()");
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("use-database")) {
            mGuiState.notifyDataViewChanged();
        }
    }

    @Override
    public boolean onSearchRequested() {
        Log.d("TreasureHunter", "Custom search launching");
        startSearch(getResources().getString(R.string.search_initial_query), true, null, false);
        return true;
    }
    /*
     * @Override public Object onRetainNonConfigurationInstance() { return
     * getIntent(); }
     */
}
