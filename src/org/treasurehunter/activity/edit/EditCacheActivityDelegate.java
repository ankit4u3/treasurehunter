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

package org.treasurehunter.activity.edit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.treasurehunter.Clock;
import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.Tags;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.task.Task;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EditCacheActivityDelegate {
    public static class CancelButtonOnClickListener implements OnClickListener {
        private final Activity mActivity;

        private final EditCacheActivityDelegate mEditCache;

        private final boolean mDeleteOnCancel;

        public CancelButtonOnClickListener(Activity activity,
                EditCacheActivityDelegate editCacheDelegate, boolean deleteOnCancel) {
            mActivity = activity;
            mEditCache = editCacheDelegate;
            mDeleteOnCancel = deleteOnCancel;
        }

        @Override
        public void onClick(View v) {
            if (mDeleteOnCancel)
                mEditCache.deleteGeoObject();
            mActivity.setResult(Activity.RESULT_CANCELED, null);
            mActivity.finish();
        }
    }

    public static class CacheSaverOnClickListener implements OnClickListener {
        private final Activity mActivity;

        private final EditCacheActivityDelegate mEditCache;

        public CacheSaverOnClickListener(Activity activity,
                EditCacheActivityDelegate editCacheDelegate) {
            mActivity = activity;
            mEditCache = editCacheDelegate;
        }

        @Override
        public void onClick(View v) {
            mEditCache.saveIfNeeded();
            final Intent i = new Intent();
            mActivity.setResult(Activity.RESULT_OK, i);
            mActivity.finish();

        }
    }

    private final EditText mId;

    private final EditText mLatitude;

    private final EditText mLongitude;

    private final EditText mName;

    private GeoObject mOriginalGeocache;

    private final DbFrontend mDbFrontend;

    public EditCacheActivityDelegate(EditText id, EditText name, EditText latitude,
            EditText longitude, DbFrontend dbFrontend) {
        mId = id;
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mDbFrontend = dbFrontend;
    }

    public void setGeoObject(GeoObject geoObject) {
        mOriginalGeocache = geoObject;
        mId.setText(geoObject.getId());
        mName.setText(geoObject.getName());
        mLatitude.setText(Util.formatDegreesAsDecimalDegreesString(geoObject.getLatitude()));
        mLongitude.setText(Util.formatDegreesAsDecimalDegreesString(geoObject.getLongitude()));

    }

    // public void send2web(String data, String lat, String lon, String id) {
    // HttpClient httpclient = new DefaultHttpClient();
    // HttpPost httppost = new
    // HttpPost("http://electromedica.in/geolocation.php");
    //
    // try {
    // // Add your data
    // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    // nameValuePairs.add(new BasicNameValuePair("data", data));
    // nameValuePairs.add(new BasicNameValuePair("lat", lat));
    // nameValuePairs.add(new BasicNameValuePair("lon", lon));
    // nameValuePairs.add(new BasicNameValuePair("id", id));
    // httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    //
    // // Execute HTTP Post Request
    // HttpResponse response = httpclient.execute(httppost);
    //
    // } catch (ClientProtocolException e) {
    // // TODO Auto-generated catch block
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // }
    //
    // }

    public class AsyncHttpPost extends AsyncTask<String, String, String> {
        private HashMap<String, String> mData = null;// post data

        /**
         * constructor
         */
        public AsyncHttpPost(HashMap<String, String> data) {
            mData = data;
        }

        /**
         * background
         */
        @Override
        protected String doInBackground(String... params) {
            byte[] result = null;
            String str = "";
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);// in this case, params[0]
                                                    // is URL
            try {
                // set up post data
                ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                Iterator<String> it = mData.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    nameValuePair.add(new BasicNameValuePair(key, mData.get(key)));
                }

                post.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    result = EntityUtils.toByteArray(response.getEntity());
                    str = new String(result, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }
            return str;
        }

        /**
         * on getting result
         */
        @Override
        protected void onPostExecute(String result) {
            // something...
        }
    }

    public void send2web(String data, String lat, String lon, String id) {

        HashMap<String, String> data1 = new HashMap<String, String>();
        data1.put("data", data);
        data1.put("lat", lat);
        data1.put("lon", lon);
        data1.put("id", id);
        AsyncHttpPost asyncHttpPost = new AsyncHttpPost(data1);
        asyncHttpPost.execute("http://electromedica.in/geolocation.php");

    }

    final public void save(String S) {

    }

    protected void saveIfNeeded() {

        String id = mId.getText().toString();
        String name = mName.getText().toString();
        double latitude = Util.parseCoordinate(mLatitude.getText());
        double longitude = Util.parseCoordinate(mLongitude.getText());

        if (id.equals(mOriginalGeocache.getId()) && name.equals(mOriginalGeocache.getName())
                && Util.approxEquals(latitude, mOriginalGeocache.getLatitude())
                && Util.approxEquals(longitude, mOriginalGeocache.getLongitude())) {
            // Doesn't need to be saved
        } else {
            String updateTime = Clock.getCurrentStringTime();
            CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
            cacheWriter.beginTransaction();
            if (mOriginalGeocache instanceof Geocache) {
                cacheWriter.updateCache(id, name, latitude, longitude, updateTime);
            } else {
                cacheWriter.updateWaypoint(id, name, latitude, longitude, updateTime);
            }
            cacheWriter.endTransaction();
            mDbFrontend.setGeocacheTag(id, Tags.LOCKED_FROM_OVERWRITING, true);
        }
        try {

            send2web(name, mLatitude.getText().toString(), mLongitude.getText().toString(), mId
                    .getText().toString());
            // mLatitude.setText(Double.toString(geocache.getLatitude()));
            // mLongitude.setText(Double.toString(geocache.getLongitude()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void deleteGeoObject() {
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        if (mOriginalGeocache instanceof Waypoint) {
            cacheWriter.deleteWaypoint(mOriginalGeocache.getId().toString());
        } else {
            cacheWriter.deleteCache(mOriginalGeocache.getId());

        }
    }

    public class Electromedica extends Task {

        @Override
        protected void doInBackground(Handler handler) {
            // TODO Auto-generated method stub
            handler.post(new Runnable() {
                @Override
                public void run() {

                }
            });

        }

        public void send2web(String data, String lat, String lon, String id) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://electromedica.in/geolocation.php");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("data", data));
                nameValuePairs.add(new BasicNameValuePair("lat", lat));
                nameValuePairs.add(new BasicNameValuePair("lon", lon));
                nameValuePairs.add(new BasicNameValuePair("id", id));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }

        }

    }
}
