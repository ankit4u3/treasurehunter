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

package org.treasurehunter.xmlimport;

import org.treasurehunter.Source;
import org.treasurehunter.database.CacheWriter;

import android.util.Log;

//Extract classes FileImportReceiver and GenericImportReceiver?
/**
 * Takes the output from the FSM and updates the GUI and sends the output to be
 * written to the database. Called on a worker thread.
 */
public class ProcessStatus implements IProcessStatus {
    private final ProcessListenerRelay mProcessListenerRelay;

    private final CacheWriter mCacheWriter;

    private Source mCurrentSource;

    private int mCacheCount = 0;

    private boolean mInTransaction = false;

    private final boolean mCleanOldGpx;

    public ProcessStatus(ProcessStatusListener listener, CacheWriter cacheWriter,
            boolean cleanOldGpx) {
        mProcessListenerRelay = new ProcessListenerRelay(listener);
        mCacheWriter = cacheWriter;
        mCleanOldGpx = cleanOldGpx;
    }

    @Override
    public boolean isFileAlreadyLoaded(Source source, String sqlDate) {
        return mCacheWriter.saveGpxIfAlreadyLoaded(source.mUnique, sqlDate);
    }

    /** Process cancelled before willStartLoading() has been called */
    @Override
    public void abortedBeforeStart() {
        mProcessListenerRelay.onProcessFinished(false);
    }

    public void willStartLoading() {
        // Start a database transaction
        mCacheWriter.beginTransaction();
        mInTransaction = true;
        if (mCleanOldGpx)
            mCacheWriter.markAllGpxForDeletion();
    }

    @Override
    public void onStartLoadingSource(Source source) {
        mCurrentSource = source;

        String status;
        if (source.isFile())
            status = "Opening: " + source.getFilename() + "...";
        else
            status = "Starting " + source.toString() + "...";
        mProcessListenerRelay.onProcessStatus(status);

        if (mCleanOldGpx)
            mCacheWriter.dontDeleteGpx(source.mUnique);
    }

    @Override
    public void onParsedGeocache(GeocacheData data) {
        if (data.mCacheType == null || !data.mCacheType.startsWith("Waypoint"))
            mCacheCount += 1;

        String status;
        if (mCurrentSource.isFile())
            status = mCacheCount + ": " + mCurrentSource.getFilename() + " - " + data.mId + " - "
                    + data.mName;
        else
            status = mCacheCount + ": " + data.mId + " - " + data.mName;
        mProcessListenerRelay.onProcessStatus(status);
        mProcessListenerRelay.onProcessProgress(mCacheCount);

        data.writeTo(mCacheWriter, mCurrentSource);
    }

    @Override
    public void onFinishedLoadingSource(String gpxDate) {
        // Check for mCurrentSource == null to guard against issue #91
        if (mCurrentSource != null && mCurrentSource.isFile())
            mCacheWriter.writeGpx(mCurrentSource.mUnique, gpxDate);
        mCurrentSource = null;
    }

    public void stoppedAllLoading(boolean success) {
        if (mCleanOldGpx) {
            mCacheWriter.deleteAllMarkedForDeletion();
        }
        if (mInTransaction) {
            mCacheWriter.endTransaction();
            mInTransaction = false;
        }
        mProcessListenerRelay.onProcessFinished(success);
        Log.d("TreasureHunter", "ProcessStatus.stoppedAllLoading(" + (success ? "true" : "false")
                + "): Finished syncing");
    }

    public void setMaxProgress(int max) {
        mProcessListenerRelay.onProcessNewMaxProgress(max);
    }
}
