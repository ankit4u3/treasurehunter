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
import org.treasurehunter.menuactions.MenuAction;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public interface SourceRow {
    /**
     * Only call from the GUI thread since the method is expected to modify the
     * GUI widgets
     */
    View getView(LayoutInflater layoutInflater);

    SourceContextMenu getContextMenu();
}

class HeadlineRow implements SourceRow {
    private View mView = null;

    @Override
    public View getView(LayoutInflater layoutInflater) {
        if (mView != null)
            return mView;

        mView = layoutInflater.inflate(R.layout.database_row, null);
        mView.findViewById(R.id.cb_include).setVisibility(View.INVISIBLE);
        TextView name = (TextView)mView.findViewById(R.id.txt_name);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setText("Database contents");
        TextView countView = (TextView)mView.findViewById(R.id.txt_count);
        countView.setText("count");
        TextView status = (TextView)mView.findViewById(R.id.txt_status);
        status.setVisibility(View.GONE);
        return mView;
    }

    @Override
    public SourceContextMenu getContextMenu() {
        return new SourceContextMenu();
    }
}

class AllSourceRow implements SourceRow {
    private final MenuAction mDeleteAll;

    private View mView = null;

    private String mCount = "";

    private String mDatabasePath;

    public AllSourceRow(MenuAction deleteAll, String databasePath) {
        mDeleteAll = deleteAll;
        mDatabasePath = databasePath;
    }

    public void setCount(String count) {
        mCount = count;
        mView = null;
    }

    @Override
    public View getView(LayoutInflater layoutInflater) {
        if (mView != null)
            return mView;

        mView = layoutInflater.inflate(R.layout.database_row, null);
        mView.findViewById(R.id.cb_include).setVisibility(View.INVISIBLE);

        TextView countView = (TextView)mView.findViewById(R.id.txt_count);
        countView.setText(mCount);

        TextView name = (TextView)mView.findViewById(R.id.txt_name);
        name.setText("Whole database");

        TextView status = (TextView)mView.findViewById(R.id.txt_status);
        status.setText(mDatabasePath);

        return mView;
    }

    @Override
    public SourceContextMenu getContextMenu() {
        return new SourceContextMenu("Whole database", new MenuAction[] {
            mDeleteAll
        });
    }
}

// TODO: Rename since the class is used by all source types
class GpxSourceRow implements SourceRow {
    private final String mLabel;

    private final Source mSource;

    private final SourceContextMenu mContextMenu;

    protected View mView;

    private String mCount = "";

    private String mStatus = "";

    /** The filename with full path in case the source exists on disk */
    public String mPath = null;

    public GpxSourceRow(String label, Source source, SourceContextMenu contextMenu) {
        mSource = source;
        mContextMenu = contextMenu;
        mLabel = label;
    }

    @Override
    public View getView(LayoutInflater layoutInflater) {
        if (mView != null)
            return mView;

        mView = layoutInflater.inflate(R.layout.database_row, null);
        mView.findViewById(R.id.cb_include).setVisibility(View.INVISIBLE);

        TextView nameView = (TextView)mView.findViewById(R.id.txt_name);
        nameView.setText(mLabel);

        TextView statusView = (TextView)mView.findViewById(R.id.txt_status);
        statusView.setText(mStatus);

        TextView countView = (TextView)mView.findViewById(R.id.txt_count);
        countView.setText(mCount);

        return mView;
    }

    public Source getSource() {
        return mSource;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
        mView = null;
    }

    public void setCount(String count) {
        mCount = count;
        mView = null;
    }

    @Override
    public SourceContextMenu getContextMenu() {
        return mContextMenu;
    }
}

class BcachingSourceRow extends GpxSourceRow {
    private String mSyncTime = "";

    private String mStatus = "";

    public BcachingSourceRow(SourceContextMenu contextMenu) {
        super("bcaching.com", Source.BCACHING, contextMenu);
    }

    @Override
    public View getView(LayoutInflater layoutInflater) {
        if (mView != null)
            return mView;
        super.getView(layoutInflater);

        TextView statusView = (TextView)mView.findViewById(R.id.txt_status);
        if (mSyncTime.equals(""))
            statusView.setText(mStatus);
        else if (mStatus.equals(""))
            statusView.setText(mSyncTime);
        else
            statusView.setText(mSyncTime + '\n' + mStatus);

        return mView;
    }

    public void setSyncTime(String syncTime) {
        mSyncTime = syncTime;
        mView = null;
    }

    @Override
    public void setStatus(String status) {
        mStatus = status;
        mView = null;
    }
}
