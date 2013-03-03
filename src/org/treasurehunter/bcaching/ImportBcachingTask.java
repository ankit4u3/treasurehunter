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

package org.treasurehunter.bcaching;

import org.json.JSONArray;
import org.json.JSONObject;
import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.task.Task;
import org.treasurehunter.xmlimport.ProcessStatus;
import org.treasurehunter.xmlimport.ProcessStatusListener;
import org.treasurehunter.xmlimport.XmlFiniteStateMachine;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Hashtable;

/** Import all caches in a certain area or those closest to a certain location */
public class ImportBcachingTask extends Task {
    private final boolean mUsesCenterCoord;

    private final double mCenterLat;

    private final double mCenterLon;

    private final double mLatLow;

    private final double mLonLow;

    private final double mLatHigh;

    private final double mLonHigh;

    private final ProcessStatus mProcessStatus;

    private final BcachingCommunication mComm;

    private final ErrorDisplayer mErrorDisplayer;

    private final int mMaxCount;

    private final String mUsername;

    private final ProcessStatusListener mProcessStatusListener;

    private final BcachingConfig mBcachingConfig;

    private final DbFrontend mDbFrontend;

    public ImportBcachingTask(double lat, double lon, int maxCount, ProcessStatus processStatus,
            BcachingCommunication comm, ErrorDisplayer errorDisplayer, String username,
            ProcessStatusListener processStatusListener, BcachingConfig bcachingConfig,
            DbFrontend dbFrontend) {
        mUsesCenterCoord = true;
        mCenterLat = lat;
        mCenterLon = lon;
        mLatLow = mLonLow = mLatHigh = mLonHigh = 0;
        mMaxCount = maxCount;
        mProcessStatus = processStatus;
        mComm = comm;
        mErrorDisplayer = errorDisplayer;
        mUsername = username;
        mProcessStatusListener = processStatusListener;
        mBcachingConfig = bcachingConfig;
        mDbFrontend = dbFrontend;
    }

    public ImportBcachingTask(double latLow, double lonLow, double latHigh, double lonHigh,
            int maxCount, ProcessStatus processStatus, BcachingCommunication comm,
            ErrorDisplayer errorDisplayer, Context context, String username,
            ProcessStatusListener processStatusListener, BcachingConfig bcachingConfig,
            DbFrontend dbFrontend) {
        mUsesCenterCoord = false;
        mCenterLat = mCenterLon = 0;
        mLatLow = latLow;
        mLonLow = lonLow;
        mLatHigh = latHigh;
        mLonHigh = lonHigh;
        mMaxCount = maxCount;
        mProcessStatus = processStatus;
        mComm = comm;
        mErrorDisplayer = errorDisplayer;
        mUsername = username;
        mProcessStatusListener = processStatusListener;
        mBcachingConfig = bcachingConfig;
        mDbFrontend = dbFrontend;
    }

    protected void doInBackground(Handler handler) {
        try {
            Log.d("TreasureHunter", "Doing bcaching login");
            mComm.validateCredentials();
        } catch (Exception ex) {
            Log.e("TreasureHunter", "Bcaching.com login failed", ex);
            mProcessStatus.abortedBeforeStart();
            if (!isAborted())
                mErrorDisplayer.displayError(R.string.bcaching_login_failed);
            return;
        }

        if (isAborted()) {
            mProcessStatus.abortedBeforeStart();
            return;
        }

        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("a", "find");
        if (mUsesCenterCoord) {
            params.put("lat", String.valueOf(mCenterLat));
            params.put("lon", String.valueOf(mCenterLon));
        } else {
            // bbox=lat1,lon1,lat2,lon2
            params.put("bbox", String.valueOf(mLatLow) + "," + String.valueOf(mLonLow) + ","
                    + String.valueOf(mLatHigh) + "," + String.valueOf(mLonHigh));
            params.put("lon", String.valueOf(mCenterLon));
        }
        params.put("wpts", "0");
        params.put("maxcount", Integer.toString(mMaxCount));
        params.put("fmt", "json");
        params.put("timeAsLong", "1"); // Don't return "new Date(xxxx)"
        params.put("idsonly", "1"); // Don't get name or other additional
                                    // properties
        if (mBcachingConfig.shouldDownloadMyFinds()) {
            params.put("found", "b");
        } else {
            params.put("found", "0");
        }
        if (mBcachingConfig.shouldDownloadMyHides()) {
            params.put("own", "b");
        } else {
            params.put("own", "0");
        }
        params.put("app", "TreasureHunter");

        String response;
        try {
            Log.d("TreasureHunter", "Sending query");
            response = readResponse(mComm.SendRequest(params));
        } catch (Exception ex) {
            Log.e("TreasureHunter", "SendRequest failed", ex);
            mProcessStatus.abortedBeforeStart();
            if (!isAborted())
                mErrorDisplayer.displayError(R.string.bcaching_sendrequest_failed);
            return;
        }

        if (isAborted()) {
            mProcessStatus.abortedBeforeStart();
            return;
        }

        StringBuilder csvIds = new StringBuilder();
        int detailsCount = 0;

        try {
            JSONObject obj = new JSONObject(response);
            JSONArray summary = obj.getJSONArray("data");
            int count = summary.length();
            Log.v("TreasureHunter", "Number of caches: " + count);
            mProcessStatusListener.onProcessNewMaxProgress(count);

            if (count == 0) {
                mProcessStatus.abortedBeforeStart();
                if (!isAborted())
                    mErrorDisplayer.displayError(R.string.bcaching_no_nearby_caches);
                return;
            }

            if (isAborted()) {
                mProcessStatus.abortedBeforeStart();
                return;
            }

            for (int i = 0; i < count; i++) {
                JSONObject cacheObject = summary.getJSONObject(i);
                String cacheId = cacheObject.getString("wpt");
                long localLastMod = mDbFrontend.getLastUpdatedTime(cacheId);
                long serverLastMod = 1;
                if (localLastMod != 0) {
                    // String serverLastModStr =
                    // cacheObject.getString("lastMod");
                    // serverLastMod =
                    // Long.parseLong(serverLastModStr.replace("new Date(",
                    // "").replace(")", ""));
                    serverLastMod = cacheObject.getLong("lastMod");
                }

                if (serverLastMod >= localLastMod) {
                    detailsCount += 1;
                    if (csvIds.length() > 0) {
                        csvIds.append(',');
                    }
                    int id = cacheObject.getInt("id");
                    csvIds.append(String.valueOf(id));
                }
            }
        } catch (Exception ex) {
            Log.e("TreasureHunter", "Processing SendRequest result failed", ex);
            mProcessStatus.abortedBeforeStart();
            if (!isAborted())
                mErrorDisplayer.displayError(R.string.bcaching_processing_result_failed);
            return;
        }

        mProcessStatus.setMaxProgress(detailsCount);

        params.clear();
        params.put("a", "detail");
        params.put("desc", "html");
        params.put("ids", csvIds.toString());
        params.put("tbs", "1");
        params.put("wpts", "1");
        params.put("logs", "20");
        params.put("fmt", "gpx");
        params.put("app", "TreasureHunter");

        if (isAborted()) {
            mProcessStatus.abortedBeforeStart();
            return;
        }

        InputStream is;
        try {
            Log.d("TreasureHunter", "Downloading cache details");
            is = mComm.SendRequest(params);
        } catch (Exception ex) {
            Log.e("TreasureHunter", "Detail Request failed", ex);
            mProcessStatus.abortedBeforeStart();
            if (!isAborted())
                mErrorDisplayer.displayError(R.string.bcaching_detail_request_failed);
            return;
        }

        if (isAborted()) {
            mProcessStatus.abortedBeforeStart();
            return;
        }

        InputStreamReader isr = new InputStreamReader(is);

        mProcessStatus.willStartLoading();
        boolean success = false;
        try {
            XmlFiniteStateMachine.importFromReader(Source.BCACHING, isr, mProcessStatus, this,
                    mUsername);
            success = true; // No assertion thrown
        } catch (XmlPullParserException e) {
            if (!isAborted())
                mErrorDisplayer.displayError(R.string.error_parsing_file, e.getMessage());
        } catch (IOException e) {
            if (!isAborted())
                mErrorDisplayer.displayError(R.string.error_reading_file, e.getMessage());
        }
        mProcessStatus.stoppedAllLoading(success);
    }

    private String readResponse(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader rd = new BufferedReader(isr, 8192);

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
            result.append('\n');
        }
        return result.toString();
    }
}
