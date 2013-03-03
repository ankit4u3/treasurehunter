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

package org.treasurehunter.activity.prox;

import org.treasurehunter.GeoFix;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeoFixProviderDI;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.Refresher;
import org.treasurehunter.activity.filterlist.FilterTypeCollection;
import org.treasurehunter.database.CachesProviderCount;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.database.DatabaseLocator;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;

public class ProximityActivity extends Activity implements SurfaceHolder.Callback {

    class DataCollector implements Refresher {
        @Override
        public void forceRefresh() {
            refresh();
        }

        @Override
        public void refresh() {
            mProximityPainter.setUserDirection(mGeoFixProvider.getAzimuth());
            GeoFix location = mGeoFixProvider.getLocation();
            mProximityPainter.setUserLocation(location.getLatitude(), location.getLongitude(),
                    location.getAccuracy());
        }
    }

    private ProximityView mProximityView;

    ProximityPainter mProximityPainter;

    DataCollector mDataCollector;

    private AnimatorThread mAnimatorThread;

    private boolean mStartWhenSurfaceCreated = false; // TODO: Is
                                                      // mStartWhenSurfaceCreated
                                                      // needed?

    private DbFrontend mDbFrontend;

    private GeoFixProvider mGeoFixProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GeocacheFactory geocacheFactory = new GeocacheFactory();
        DatabaseLocator databaseLocator = new DatabaseLocator(this);
        mDbFrontend = new DbFrontend(databaseLocator, geocacheFactory);
        CachesProviderDb cachesProviderDb = new CachesProviderDb(mDbFrontend);
        final FilterTypeCollection filterTypeCollection = new FilterTypeCollection(this);
        GeocacheFilter cacheFilter = filterTypeCollection.getActiveFilter();
        cachesProviderDb.setFilter(cacheFilter);
        CachesProviderCount cachesProviderCount = new CachesProviderCount(cachesProviderDb, 5, 10);

        mProximityPainter = new ProximityPainter(cachesProviderCount);

        // Set metric or English units.
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        final boolean mImperial = sharedPreferences.getBoolean("imperial", false);
        mProximityPainter.setUseImperial(mImperial);

        mProximityView = new ProximityView(this);
        setContentView(mProximityView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SurfaceHolder holder = mProximityView.getHolder();
        holder.addCallback(this);
        mDataCollector = new DataCollector();
        mGeoFixProvider = GeoFixProviderDI.create(this);
        mGeoFixProvider.addObserver(mDataCollector);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String id = getIntent().getStringExtra(Geocache.ID);
        if (!id.equals("")) {
            Geocache geocache = mDbFrontend.loadCacheFromId(id);
            mProximityPainter.setSelectedGeocache(geocache);
        }

        mGeoFixProvider.startUpdates();

        GeoFix location = mGeoFixProvider.getLocation();
        mProximityPainter.setUserLocation(location.getLatitude(), location.getLongitude(),
                location.getAccuracy());

        if (mAnimatorThread == null)
            mStartWhenSurfaceCreated = true;
        else
            mAnimatorThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGeoFixProvider.stopUpdates();
        if (mAnimatorThread != null) {
            AnimatorThread.IThreadStoppedListener listener = new AnimatorThread.IThreadStoppedListener() {
                @Override
                public void OnThreadStopped() {
                    mDbFrontend.closeDatabase();
                }
            };
            mAnimatorThread.stop(listener);
        }
    }

    // ***********************************
    // ** Implement SurfaceHolder.Callback **

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("TreasureHunter", "surfaceChanged called (" + width + "x" + height + ")");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("TreasureHunter", "surfaceCreated called");
        mAnimatorThread = new AnimatorThread(holder, mProximityPainter);
        if (mStartWhenSurfaceCreated) {
            mStartWhenSurfaceCreated = false;
            mAnimatorThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("TreasureHunter", "surfaceDestroyed called");
    }

}
