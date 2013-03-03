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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Constructs the contents of the graphical list, given a list of data */
public class SourceListAdapter extends BaseAdapter implements OnItemClickListener {
    private final TextView mEmptyTextView;

    private final String mEmptyLabel;

    private final String mCalculatingLabel;

    private final LayoutInflater mLayoutInflater;

    /** If the list is null, the data hasn't been calculated yet. */
    private List<SourceRow> mSourceRows;

    public SourceListAdapter(List<SourceRow> initialRows, TextView emptyTextView,
            String emptyLabel, String calculatingLabel, LayoutInflater layoutInflater) {
        mEmptyTextView = emptyTextView;
        mEmptyLabel = emptyLabel;
        mCalculatingLabel = calculatingLabel;
        mSourceRows = initialRows;
        mLayoutInflater = layoutInflater;
        updateEmptyText();
    }

    private void updateEmptyText() {
        if (mSourceRows == null)
            mEmptyTextView.setText(mCalculatingLabel);
        else if (mSourceRows.isEmpty())
            mEmptyTextView.setText(mEmptyLabel);
    }

    public synchronized List<SourceRow> getSourceRows() {
        if (mSourceRows == null)
            return new ArrayList<SourceRow>();
        return mSourceRows;
    }

    /** Also call notifyDataSetChanged */
    public synchronized void setSourceRows(List<SourceRow> sources) {
        mSourceRows = sources;
        updateEmptyText();
    }

    private final Runnable mNotifyChanged = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    /** Can be called from any thread */
    public void notifyThreadChangedData() {
        mEmptyTextView.post(mNotifyChanged);
    }

    @Override
    public synchronized int getCount() {
        if (mSourceRows == null)
            return 0;
        return mSourceRows.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        if (position >= mSourceRows.size())
            return null;
        return mSourceRows.get(position).getView(mLayoutInflater);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Log.d("TreasureHunter", "Clicked " + arg2);
        arg0.showContextMenuForChild(arg1);
    }

}
