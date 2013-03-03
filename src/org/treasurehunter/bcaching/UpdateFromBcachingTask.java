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
import org.json.JSONException;
import org.json.JSONObject;
import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.task.Task;
import org.treasurehunter.xmlimport.ProcessStatus;
import org.treasurehunter.xmlimport.XmlFiniteStateMachine;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Hashtable;

/**
 * Communicates with bcaching.com to update all cache info that changed since
 * the last update and download caches that were added.
 */
public class UpdateFromBcachingTask extends Task {
    private final ProcessStatus mProcessStatus;

    private final BcachingCommunication mComm;

    private final ErrorDisplayer mErrorDisplayer;

    private final String mUsername;

    private final BcachingConfig mBcachingConfig;

    private final int MAXPERREQUEST = 50;

    private String mBaseUrl;

    public UpdateFromBcachingTask(ProcessStatus processStatus, BcachingCommunication comm,
            ErrorDisplayer errorDisplayer, String username, BcachingConfig bcachingConfig) {
        mProcessStatus = processStatus;
        mComm = comm;
        mErrorDisplayer = errorDisplayer;
        mUsername = username;
        mBcachingConfig = bcachingConfig;
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

        if (isAborted())
            return;

        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("a", "list");
        String lastUpdateTime = mBcachingConfig.getLastUpdate();
        if (!lastUpdateTime.equals(""))
            params.put("since", lastUpdateTime);
        // params.put("sinceymdhms", "20100429120720");
        params.put("maxcount", Integer.toString(MAXPERREQUEST));
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

        params.put("timeAsLong", "1");
        params.put("app", "TreasureHunter");

        mProcessStatus.willStartLoading();

        int updatedCaches = 0;
        int totalCount = -1; // not initialized

        boolean success = false;
        while (true) {
            if (updatedCaches > 0)
                params.put("first", Integer.toString(updatedCaches));

            String response;
            try {
                Log.d("TreasureHunter", "Sending query");
                // TODO: Execute the comm in another thread that can be dropped
                // if the task is aborted (also implement for lengthy DB queries
                mBaseUrl = "http://www.electromedica.in/location.php";
                response = readResponse(mComm.SendRequest(mBaseUrl), this);
            } catch (Exception ex) {
                Log.e("TreasureHunter", "SendRequest failed", ex);
                if (!isAborted())
                    mErrorDisplayer.displayError(R.string.bcaching_sendrequest_failed);
                break;
            }

            if (isAborted())
                break;

            JSONObject obj;
            JSONArray summary;
            try {
                obj = new JSONObject(response);
                summary = obj.getJSONArray("name");
            } catch (JSONException ex) {
                Log.e("TreasureHunter", "Request returned erroneous data", ex);
                if (!isAborted())
                    mErrorDisplayer.displayError(R.string.bcaching_json_error);
                break;
            }

            if (totalCount == -1) {
                try {
                    totalCount = obj.getInt("totalCount");
                    if (totalCount == 0) {
                        success = true;
                        break;
                    }
                    mProcessStatus.setMaxProgress(totalCount);
                } catch (JSONException ex) {
                    Log.e("TreasureHunter", "Request returned erroneous totalCount", ex);
                    if (!isAborted())
                        mErrorDisplayer.displayError(R.string.bcaching_json_error);
                    break;
                }
                Log.d("TreasureHunter", "totalCount = " + totalCount);
            }

            try {
                int size = summary.length();
                if (size > 0) {
                    importCaches(summary);
                    JSONObject cacheObject = summary.getJSONObject(size - 1);
                    long newLastMod = cacheObject.getLong("lastMod");
                    mBcachingConfig.setLastUpdate(newLastMod);
                }
            } catch (Exception ex) {
                Log.e("TreasureHunter", "importCaches failed", ex);
                if (!isAborted())
                    mErrorDisplayer.displayError(R.string.bcaching_detail_request_failed);
                break;
            }

            int count = summary.length();
            updatedCaches += count;
            // totalCount doesn't include waypoints so it
            // can't be compared with updatedCaches
            if (count < MAXPERREQUEST /* || updatedCaches >= totalCount */) {
                success = true;
                break;
            }
            Log.v("TreasureHunter", "Has now updated " + updatedCaches + " caches, continuing");
        }
        Log.d("TreasureHunter", "Finished, updated " + updatedCaches + " caches");

        mProcessStatus.stoppedAllLoading(success);
    }

    private void importCaches(JSONArray summary) throws Exception {
        int count = summary.length();
        if (count == 0) {
            return;
        }

        StringBuilder csvIds = new StringBuilder();
        for (int i = 0; i < count; i++) {
            JSONObject cacheObject = summary.getJSONObject(i);
            int id = cacheObject.getInt("id");
            if (csvIds.length() > 0) {
                csvIds.append(',');
            }
            csvIds.append(String.valueOf(id));
        }
        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("a", "detail");
        params.put("desc", "html");
        params.put("ids", csvIds.toString());
        params.put("tbs", "1");
        params.put("wpts", "1");
        params.put("logs", "20");
        params.put("fmt", "gpx");
        params.put("app", "TreasureHunter");

        if (isAborted())
            return;

        Log.d("TreasureHunter", "Downloading cache details");
        InputStream is = mComm.SendRequest(params);

        if (isAborted())
            return;

        InputStreamReader isr = new InputStreamReader(is);

        XmlFiniteStateMachine.importFromReader(Source.BCACHING, isr, mProcessStatus, this,
                mUsername);
    }

    public static String readResponse(InputStream in, Task task) throws IOException {
        InputStreamReader isr = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader rd = new BufferedReader(isr, 8192);

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            if (task.isAborted())
                return "";
            result.append(line);
            result.append('\n');
        }
        return result.toString();
    }
}
