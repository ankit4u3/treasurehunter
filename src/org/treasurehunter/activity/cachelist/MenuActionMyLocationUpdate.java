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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.treasurehunter.Clock;
import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.activity.edit.EditCacheActivityJson;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class MenuActionMyLocationUpdate extends StaticLabelMenu implements MenuAction {
    private final ErrorDisplayer mErrorDisplayer;

    private final DbFrontend mDbFrontend;

    private final GeocacheFactory mGeocacheFactory;

    private final GeoFixProvider mLocationControl;

    private final Activity mActivity;

    private final Resources mResources;

    private final String mBaseUrl = "http://www.electromedica.in/location.php";

    public MenuActionMyLocationUpdate(ErrorDisplayer errorDisplayer,
            GeocacheFactory geocacheFactory, GeoFixProvider locationControl, DbFrontend dbFrontend,
            Resources resources, Activity activity) {
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

        Intent intent = new Intent(mActivity, EditCacheActivityJson.class);
        intent.putExtra(Geocache.ID, newCache.getId());
        intent.putExtra("creating", true);
        mActivity.startActivityForResult(intent, 0);

        // Since the Edit activity will refresh the list, we don't need to do it
        // mListRefresher.forceRefresh();

    }

    private URL getURL() throws Exception {

        return new URL(mBaseUrl);
    }

    public String SendRequest() throws Exception {

        final URL url = getURL();
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(6000);
        conn.setConnectTimeout(6000);
        int responseCode = conn.getResponseCode(); // Will wait for response
        InputStream in = conn.getInputStream();

        String contentEncoding = conn.getContentEncoding();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");

        }
        Log.d("Location Update ", sb.toString());

        return (sb.toString());
    }

    public void sendurl() throws JSONException, Exception {
        Log.d("Location Update ", "inside send url block");

        JSONObject obj = new JSONObject(SendRequest());
        JSONArray summary = obj.getJSONArray("data");
        int count = summary.length();
        Log.d("Location Update ", String.valueOf(count));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
