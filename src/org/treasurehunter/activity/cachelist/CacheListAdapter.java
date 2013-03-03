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

import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheList;
import org.treasurehunter.R;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.database.DistanceAndBearing;
import org.treasurehunter.database.DistanceAndBearing.IDistanceAndBearingProvider;
import org.treasurehunter.task.DelayingTaskRunner;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

/** Feeds the caches in a CachesProvider to the GUI list view */
public class CacheListAdapter extends BaseAdapter /* implements Refresher */{
    private final DelayingTaskRunner mListTaskRunner;

    private final IDistanceAndBearingProvider mDistances;

    private final CacheListRowInflater mCacheListRowInflater;

    private final DbFrontend mDbFrontend;

    private final TextView mEmptyTextView;

    private GeocacheList mListData = null; // Start off as "not calculated"

    private float mAzimuth;

    public static class ScrollListener implements OnScrollListener {
        private final CacheListAdapter mCacheListAdapter;

        public ScrollListener(CacheListAdapter updateFlag) {
            mCacheListAdapter = updateFlag;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCacheListAdapter.enableUpdates(scrollState == SCROLL_STATE_IDLE);
        }
    }

    public CacheListAdapter(IDistanceAndBearingProvider distances, CacheListRowInflater inflater,
            DbFrontend dbFrontend, TextView emptyTextView, DelayingTaskRunner listTaskRunner) {
        mCacheListRowInflater = inflater;
        mDistances = distances;
        mDbFrontend = dbFrontend;
        mEmptyTextView = emptyTextView;
        mListTaskRunner = listTaskRunner;
    }

    public void setGeocacheList(GeocacheList geocacheList) {
        mListData = geocacheList;
        if (mListData == null || mListData.isEmpty())
            setEmptyListMessage();
        notifyDataSetChanged();
    }

    public void enableUpdates(boolean enable) {
        if (enable)
            mListTaskRunner.resume();
        else
            mListTaskRunner.pause();
    }

    public void setAzimuth(float azimuth) {
        mAzimuth = azimuth;
    }

    private void setEmptyListMessage() {
        if (mListData == null) {
            mEmptyTextView.setText(R.string.cache_list_calculating);
        } else {
            int sqlCount = mDbFrontend.countAll(); // count all caches
            if (sqlCount > 0) {
                mEmptyTextView.setText(R.string.no_nearby_caches);
            } else {
                mEmptyTextView.setText(R.string.no_caches);
            }
        }
    }

    @Override
    public int getCount() {
        if (mListData == null)
            return 0;
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get the geocache for a certain row in the displayed list, starting with
     * zero
     */
    public Geocache getGeocacheAt(int position) {
        if (mListData == null)
            return null;
        Geocache cache = mListData.get(position);
        return cache;
    }

    public GeocacheList getListData() {
        return mListData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mListData == null)
            return null; // What happens in this case?
        View view = (convertView != null ? convertView : mCacheListRowInflater.inflate());
        Geocache cache = mListData.get(position);
        DistanceAndBearing geocacheVector = mDistances.getDistanceAndBearing(cache);
        mCacheListRowInflater.setData(view, geocacheVector, mAzimuth);
        return view;
    }

    public void refreshIcons() {
        notifyDataSetChanged();
    }

}
