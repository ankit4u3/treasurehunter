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
import org.treasurehunter.GeoFix;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.activity.edit.EditCacheActivity;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;

import java.util.Calendar;
import java.util.Date;

public class MenuActionMyLocation extends StaticLabelMenu implements MenuAction {
    private final ErrorDisplayer mErrorDisplayer;

    private final DbFrontend mDbFrontend;

    private final GeocacheFactory mGeocacheFactory;

    private final GeoFixProvider mLocationControl;

    private final Activity mActivity;

    private final Resources mResources;

    public MenuActionMyLocation(ErrorDisplayer errorDisplayer, GeocacheFactory geocacheFactory,
            GeoFixProvider locationControl, DbFrontend dbFrontend, Resources resources,
            Activity activity) {
        super(resources, R.string.menu_add_my_location);
        mErrorDisplayer = errorDisplayer;
        mGeocacheFactory = geocacheFactory;
        mLocationControl = locationControl;
        mDbFrontend = dbFrontend;
        mActivity = activity;
        mResources = resources;
    }

    @Override
    public void act() {

        GeoFix location = mLocationControl.getLocation();
        if (location == null)
            return;

        long time = location.getTime();
        String name = mResources.getString(R.string.my_location_name_format, time);
        Geocache newCache = mGeocacheFactory.create(String.format("%1$tk%1$tM%1$tS", time), name,
                location.getLatitude(), location.getLongitude(), Source.MY_LOCATION,
                GeocacheType.MY_LOCATION, 0, 0, 0);

        if (newCache == null) {
            mErrorDisplayer.displayError(R.string.current_location_null);
            return;
        }

        String now = Clock.getCurrentStringTime();
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        cacheWriter.beginTransaction();
        cacheWriter.insertAndUpdateCache(newCache.getId(), newCache.getName(),
                newCache.getLatitude(), newCache.getLongitude(), newCache.getSource(),
                newCache.getCacheType(), newCache.getDifficulty(), newCache.getTerrain(),
                newCache.getContainer(), now);
        cacheWriter.endTransaction();

        Intent intent = new Intent(mActivity, EditCacheActivity.class);
        intent.putExtra(Geocache.ID, newCache.getId());
        intent.putExtra("creating", true);
        mActivity.startActivityForResult(intent, 0);

        // Since the Edit activity will refresh the list, we don't need to do it
        // mListRefresher.forceRefresh();

    }

    Calendar cal;

    public void actfool(String lat, String lon) {
        Date d = cal.getInstance().getTime();

        Geocache newCache = mGeocacheFactory.create(String.format("%1$tk%1$tM%1$tS", d), "testing",
                Double.valueOf(lat), Double.valueOf(lon), Source.MY_LOCATION,
                GeocacheType.MY_LOCATION, 0, 0, 0);

        if (newCache == null) {
            mErrorDisplayer.displayError(R.string.current_location_null);
            return;
        }

        String now = Clock.getCurrentStringTime();
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        cacheWriter.beginTransaction();
        cacheWriter.insertAndUpdateCache(newCache.getId(), newCache.getName(),
                newCache.getLatitude(), newCache.getLongitude(), newCache.getSource(),
                newCache.getCacheType(), newCache.getDifficulty(), newCache.getTerrain(),
                newCache.getContainer(), now);
        cacheWriter.endTransaction();

        Intent intent = new Intent(mActivity, EditCacheActivity.class);
        intent.putExtra(Geocache.ID, newCache.getId());
        intent.putExtra("creating", true);
        mActivity.startActivityForResult(intent, 0);

        // Since the Edit activity will refresh the list, we don't need to do it
        // mListRefresher.forceRefresh();

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
