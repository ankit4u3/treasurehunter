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

import org.treasurehunter.Clock;
import org.treasurehunter.Source;
import org.treasurehunter.SourceFactory;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.task.Task;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Add rows to the sources list for all sources in the database */
public class PopulateSourcesTask extends Task {
    public final static String STATUS_CALCULATING = "...";

    private final SourceListAdapter mSourceListAdapter;

    private final DbFrontend mDbFrontend;

    private final BcachingConfig mBcachingConfig;

    private final SourceRowFactory mSourceRowFactory;

    private final SourceFactory mSourceFactory;

    public PopulateSourcesTask(SourceListAdapter sourceListAdapter, DbFrontend dbFrontend,
            BcachingConfig bcachingConfig, SourceRowFactory sourceRowFactory,
            SourceFactory sourceFactory) {
        mSourceListAdapter = sourceListAdapter;
        mDbFrontend = dbFrontend;
        mBcachingConfig = bcachingConfig;
        mSourceRowFactory = sourceRowFactory;
        mSourceFactory = sourceFactory;
    }

    protected void doInBackground(Handler handler) {
        final Map<String, Integer> counts = mDbFrontend.getCountPerSource();
        final List<SourceRow> sourceRows = new ArrayList<SourceRow>(counts.size() + 4);

        sourceRows.add(mSourceRowFactory.makeHeadlineRow());
        final String count = Integer.toString(mDbFrontend.countAll());
        sourceRows.add(mSourceRowFactory.makeAllSourceRow(count));

        for (String name : counts.keySet()) {
            GpxSourceRow sourceRow = makeSourceRow(name, counts.get(name));
            if (sourceRow != null)
                sourceRows.add(sourceRow);
        }

        mSourceListAdapter.setSourceRows(sourceRows);
        mSourceListAdapter.notifyThreadChangedData();
    }

    private GpxSourceRow makeSourceRow(String name, int count) {
        Source source = mSourceFactory.fromString(name);
        GpxSourceRow sourceRow = null;

        if (source == Source.MY_LOCATION) {
            sourceRow = mSourceRowFactory.makeMyLocationRow(Integer.toString(count));
        } else if (source == Source.BCACHING) {
            BcachingSourceRow bcaching = mSourceRowFactory.makeBcachingRow();
            String lastUpdate = mBcachingConfig.getLastUpdate();
            if (lastUpdate.equals(""))
                bcaching.setSyncTime("No full sync done");
            else
                bcaching.setSyncTime("Last full sync: "
                        + Clock.timeToString(Long.parseLong(lastUpdate)));
            bcaching.setCount(Integer.toString(count));
            bcaching.setStatus("...");
            sourceRow = bcaching;
        } else if (source.isGpx()) {
            sourceRow = mSourceRowFactory.makeGpxRow(source, "GPX file " + source.mFilename,
                    Integer.toString(count));
            sourceRow.setStatus(STATUS_CALCULATING);
        } else if (source.isLoc()) {
            sourceRow = mSourceRowFactory.makeLocRow(source, Integer.toString(count));
            sourceRow.setStatus(STATUS_CALCULATING);
        } else {
            Log.w("TreasureHunter", "Unknown source " + source.mUnique);
        }

        return sourceRow;
    }
}
