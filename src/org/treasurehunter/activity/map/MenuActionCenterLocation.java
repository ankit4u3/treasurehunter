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
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.treasurehunter.R;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MenuActionCenterLocation extends StaticLabelMenu implements MenuAction {
    private final MapController mMapController;

    private Context mContext;

    private final MyLocationOverlay mMyLocationOverlay;

    public MenuActionCenterLocation(Resources resources, MapController mapController,
            MyLocationOverlay myLocationOverlay) {
        super(resources, R.string.menu_center_location);
        mMapController = mapController;
        mMyLocationOverlay = myLocationOverlay;
    }

    @Override
    public void act() {
        GeoPoint geopoint = mMyLocationOverlay.getMyLocation();

        if (geopoint != null)

            // send2web("Developer Here",
            // String.valueOf((geopoint.getLatitudeE6() / 1E6)),
            // String.valueOf((geopoint.getLongitudeE6() / 1E6)), "007");
            mMapController.animateTo(geopoint);
    }

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

    @Override
    public boolean isEnabled() {
        return true;
    }
}
