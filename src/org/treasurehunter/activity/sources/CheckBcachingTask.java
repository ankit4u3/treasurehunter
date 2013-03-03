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

package org.treasurehunter.activity.sources;

import org.json.JSONException;
import org.json.JSONObject;
import org.treasurehunter.R;
import org.treasurehunter.bcaching.BcachingCommunication;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.bcaching.UpdateFromBcachingTask;
import org.treasurehunter.task.Task;

import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

public class CheckBcachingTask extends Task {
    private final BcachingCommunication mComm;

    private final BcachingConfig mBcachingConfig;

    private final SourceListAdapter mSourceListAdapter;

    private final SourceRowFactory mSourceRowFactory;

    private final Resources mResources;

    public CheckBcachingTask(BcachingCommunication comm, BcachingConfig bcachingConfig,
            SourceListAdapter sourceListAdapter, SourceRowFactory sourceRowFactory,
            Resources resources) {
        mComm = comm;
        mBcachingConfig = bcachingConfig;
        mSourceListAdapter = sourceListAdapter;
        mSourceRowFactory = sourceRowFactory;
        mResources = resources;
    }

    protected void doInBackground(Handler handler) {
        Log.d("TreasureHunter", "CheckBcachingTask start");
        if (!mBcachingConfig.isSetup()) {
            synchronized (mSourceListAdapter) {
                BcachingSourceRow sourceRow = getSourceRow();
                // Don't create a bcaching row if there wasn't one
                if (sourceRow != null) {
                    sourceRow.setStatus(mResources.getString(R.string.bcaching_no_account));
                    mSourceListAdapter.notifyThreadChangedData();
                }
            }
            Log.d("TreasureHunter", "CheckBcachingTask not set up");
            return;
        }

        Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("a", "list");
        String lastUpdateTime = mBcachingConfig.getLastUpdate();
        if (!lastUpdateTime.equals(""))
            params.put("since", lastUpdateTime);
        params.put("maxcount", "0");
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

        final int totalCount;
        try {
            String response = UpdateFromBcachingTask.readResponse(mComm.SendRequest(params), this);
            JSONObject obj = new JSONObject(response);
            totalCount = obj.getInt("totalCount");
            String text = mResources.getString(R.string.bcaching_caches_to_update1, totalCount);
            setSourceRowStatus(text);
            Log.d("TreasureHunter", "CheckBcachingTask finished");
            return;

        } catch (JSONException ex) {
            Log.w("TreasureHunter", "CheckBcachingTask JSONException");
        } catch (IOException e) {
            Log.w("TreasureHunter", "CheckBcachingTask IOException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.w("TreasureHunter", "CheckBcachingTask Exception");
            e.printStackTrace();
        }

        setSourceRowStatus("Error getting bcaching status");
        Log.d("TreasureHunter", "CheckBcachingTask error getting bcaching status");
    }

    private BcachingSourceRow getSourceRow() {
        for (SourceRow sourceRow : mSourceListAdapter.getSourceRows()) {
            if (sourceRow instanceof BcachingSourceRow)
                return (BcachingSourceRow)sourceRow;
        }
        return null;
    }

    private void setSourceRowStatus(String status) {
        synchronized (mSourceListAdapter) {
            BcachingSourceRow bcaching = getSourceRow();
            if (bcaching == null) {
                List<SourceRow> sourceRows = mSourceListAdapter.getSourceRows();
                bcaching = mSourceRowFactory.makeBcachingRow();
                sourceRows.add(bcaching);
                mSourceListAdapter.setSourceRows(sourceRows);
            }
            bcaching.setStatus(status);
            mSourceListAdapter.notifyThreadChangedData();
        }
    }
}
