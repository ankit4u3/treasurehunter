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

import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.SourceFactory;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.task.Task;
import org.treasurehunter.xmlimport.DirectoryIterator;
import org.treasurehunter.xmlimport.DirectoryIterator.SourceReader;
import org.treasurehunter.xmlimport.GeocacheData;
import org.treasurehunter.xmlimport.IProcessStatus;
import org.treasurehunter.xmlimport.XmlFiniteStateMachine;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.NoSuchElementException;

/** Updates the sources list with the files in the download directory */
public class CheckDirectoryTask extends Task {
    private static class GetDateProcessStatus implements IProcessStatus {
        public String mDate = "";

        @Override
        public void abortedBeforeStart() {
        }

        @Override
        public boolean isFileAlreadyLoaded(Source source, String sqlDate) {
            mDate = sqlDate;
            return true; // Don't continue parsing
        }

        @Override
        public void onFinishedLoadingSource(String gpxDate) {
        }

        @Override
        public void onParsedGeocache(GeocacheData data) {
        }

        @Override
        public void onStartLoadingSource(Source source) {
        }
    }

    private final String mDirName;

    private final SourceFactory mSourceFactory;

    private final SourceListAdapter mSourceListAdapter;

    private final SourceRowFactory mSourceRowFactory;

    private final DbFrontend mDbFrontend;

    private final Resources mResources;

    public CheckDirectoryTask(String dirName, SourceFactory sourceFactory,
            SourceListAdapter sourceListAdapter, SourceRowFactory sourceRowFactory,
            DbFrontend dbFrontend, Resources resources) {
        mDirName = dirName;
        mSourceFactory = sourceFactory;
        mSourceListAdapter = sourceListAdapter;
        mSourceRowFactory = sourceRowFactory;
        mDbFrontend = dbFrontend;
        mResources = resources;
    }

    protected void doInBackground(Handler handler) {
        DirectoryIterator dirIter = new DirectoryIterator(mDirName, this);
        while (true) {
            if (isAborted())
                return;
            final SourceReader reader;
            try {
                reader = dirIter.next();
            } catch (NoSuchElementException ex) {
                break;
            } catch (IOException ex) {
                Log.w("TreasureHunter", "CheckDirectoryTask IOException " + ex.getMessage());
                return;
            }

            final Source source = mSourceFactory.fromFile(reader.getFilename());
            final GpxSourceRow existingRow = getRow(source);
            String path = mDirName + "/" + source.mFilename;
            if (existingRow != null) {
                updateExistingRow(existingRow, source, reader.getReader(), handler, path);
            } else {
                addRow(source, path);
            }
        }

        markDeletedFiles();
    }

    private void updateExistingRow(final GpxSourceRow existingRow, Source source, Reader reader,
            Handler handler, String path) {
        GetDateProcessStatus processStatus = new GetDateProcessStatus();
        String exportTime;
        try {
            XmlFiniteStateMachine.importFromReader(source, reader, processStatus, this, "");
            exportTime = processStatus.mDate;
        } catch (XmlPullParserException ex) {
            setStatus(existingRow, mResources.getString(R.string.data_error_parsing_file), path);
            return;
        } catch (IOException ex) {
            setStatus(existingRow, ex.getMessage(), path);
            return;
        }

        String status;
        if (mDbFrontend.isSourceLoaded(source.mUnique, exportTime)) {
            status = mResources.getString(R.string.data_file_in_sync1, exportTime);
        } else {
            status = mResources.getString(R.string.data_file_not_synced1, exportTime);
        }
        setStatus(existingRow, status, path);
    }

    private GpxSourceRow getRow(Source source) {
        List<SourceRow> list = mSourceListAdapter.getSourceRows();
        for (SourceRow sourceRow : list) {
            if (sourceRow instanceof GpxSourceRow) {
                GpxSourceRow row = ((GpxSourceRow)sourceRow);
                if (row.getSource() == source) {
                    return row;
                }
            }
        }
        return null;
    }

    private void addRow(Source source, String path) {
        synchronized (mSourceListAdapter) {
            List<SourceRow> list = mSourceListAdapter.getSourceRows();
            GpxSourceRow row = mSourceRowFactory.makeGpxRow(source,
                    "GPX file " + source.getFilename(), "");
            row.mPath = path;
            row.setStatus(mResources.getString(R.string.data_new_file));
            list.add(row);
        }
        mSourceListAdapter.notifyThreadChangedData();
    }

    private void setStatus(GpxSourceRow row, String status, String path) {
        synchronized (mSourceListAdapter) {
            row.setStatus(status);
            row.mPath = path;
        }
        mSourceListAdapter.notifyThreadChangedData();
    }

    private void markDeletedFiles() {
        synchronized (mSourceListAdapter) {
            List<SourceRow> sourceRows = mSourceListAdapter.getSourceRows();
            for (SourceRow sourceRow : sourceRows) {
                if (!(sourceRow instanceof GpxSourceRow))
                    continue;
                GpxSourceRow gpxSourceRow = (GpxSourceRow)sourceRow;
                if (gpxSourceRow.getStatus().equals(PopulateSourcesTask.STATUS_CALCULATING))
                    gpxSourceRow.setStatus(mResources.getString(R.string.data_file_missing));
            }
        }
        mSourceListAdapter.notifyThreadChangedData();
    }
}
