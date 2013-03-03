
package org.treasurehunter.menuactions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.treasurehunter.Clock;
import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.activity.edit.EditCacheActivityJson;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.CachesProviderDb;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.task.Task;
import org.treasurehunter.xmlimport.ProcessStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class MenuActionSearchOnline extends StaticLabelMenu implements MenuAction {
    private final ErrorDisplayer mErrorDisplayer;

    public CachesProviderDb mCachesProviderDbClosest;

    ProcessStatus mProcessStatuss;

    public AlertDialog.Builder mBuilder;

    public String mTitle;

    public String mBody;

    private final DbFrontend mDbFrontend;

    private final GeocacheFactory mGeocacheFactory;

    private final GeoFixProvider mLocationControl;

    private final Activity mActivity;

    private final Resources mResources;

    ProcessStatus mProcessStatus;

    private final String mBaseUrl = "http://www.electromedica.in/location.php";

    private Intent intent;

    public MenuActionSearchOnline(ErrorDisplayer errorDisplayer, GeocacheFactory geocacheFactory,
            GeoFixProvider locationControl, DbFrontend dbFrontend, Resources resources,
            Activity activity) {
        super(resources, R.string.update);
        mErrorDisplayer = errorDisplayer;
        mGeocacheFactory = geocacheFactory;
        mLocationControl = locationControl;
        mDbFrontend = dbFrontend;
        mActivity = activity;
        mResources = resources;
    }

    private URL getURL() throws Exception {

        return new URL(mBaseUrl);
    }

    @Override
    public void act() {

        getfromweb();

    }

    Calendar cal;

    public void actfool(String id, String name, String double1, String double2) {
        try {
            Date d = cal.getInstance().getTime();

            Geocache newCache = mGeocacheFactory.create(id, name, Util.parseCoordinate(double1),
                    Util.parseCoordinate(double2), Source.MY_LOCATION, GeocacheType.MY_LOCATION, 0,
                    0, 0);

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

            // intent.putExtra(Geocache.ID, newCache.getId());
            // intent.putExtra("creating", true);

            // Since the Edit activity will refresh the list, we don't need to
            // do it
            // mListRefresher.forceRefresh();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            mErrorDisplayer.displayError(R.string.gpxfailed);
        }

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public class AsyncHttpPost extends AsyncTask<String, String, String> {

        /**
         * background
         */
        @Override
        protected String doInBackground(String... params) {

            new ImportGPXTask().doInBackground(null);
            return mBaseUrl;

        }

        /**
         * on getting result
         */
        @Override
        protected void onPostExecute(String result) {
            // something...
        }
    }

    public void getfromweb() {

        AsyncHttpPost asyncHttpPost = new AsyncHttpPost();
        asyncHttpPost.execute("Query Getting Data From Web Server Electromedica JSON Encoded");

    }

    class ImportGPXTask extends Task {

        protected void doInBackground(Handler handler) {

            act2();

        }

        void act2() {
            try {

                try {
                    final URL url = getURL();
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setReadTimeout(6000);
                    conn.setConnectTimeout(6000);
                    int responseCode = conn.getResponseCode(); // Will wait for
                                                               // response

                    InputStream in = conn.getInputStream();

                    String contentEncoding = conn.getContentEncoding();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");

                    }
                    if (null == sb.toString()) {
                        return;

                    }
                    Log.d("Location Update ", sb.toString());
                    JSONObject obj = new JSONObject(sb.toString());
                    JSONArray summary = obj.getJSONArray("name");
                    int count = summary.length();
                    Log.d("Location Update ", String.valueOf(count));

                    if (count == -1) {
                        return;
                    }
                    CacheWriter cacheWriter = mDbFrontend.getCacheWriter();

                    for (int i = 0; i < count; i++) {
                        JSONObject cacheObject = summary.getJSONObject(i);
                        intent = new Intent(mActivity, EditCacheActivityJson.class);
                        try {

                            actfool2(cacheObject.getString("id"), cacheObject.getString("data"),
                                    cacheObject.getString("lat"), cacheObject.getString("lon"));
                            // mActivity.startActivityForResult(intent, 0);
                        } catch (Exception e) {

                            mProcessStatus.abortedBeforeStart();
                            if (!isAborted())
                                mErrorDisplayer
                                        .displayError(R.string.bcaching_processing_result_failed);
                            return;
                        }

                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mErrorDisplayer.displayError(R.string.gpxfailed);
                }

            }

            catch (Exception e) {
                // TODO Auto-generated catch block
                mErrorDisplayer.displayError(R.string.gpxfailed);
            }
            mErrorDisplayer.displayError(R.string.gpxupdate);

        }

        Calendar cal;

        public void actfool2(String id, String name, String double1, String double2) {
            try {
                Date d = cal.getInstance().getTime();

                Geocache newCache = mGeocacheFactory.create(id, name,
                        Util.parseCoordinate(double1), Util.parseCoordinate(double2),
                        Source.MY_LOCATION, GeocacheType.MY_LOCATION, 0, 0, 0);

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

                // intent.putExtra(Geocache.ID, newCache.getId());
                // intent.putExtra("creating", true);

                // Since the Edit activity will refresh the list, we don't need
                // to
                // do it
                // mListRefresher.forceRefresh();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mProcessStatus.abortedBeforeStart();

            }

        }

    }

}
