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
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.MenuActionConfirm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;

public class SourceRowFactory {
    private final GuiState mGuiState;

    private final Activity mActivity;

    private final DbFrontend mDbFrontend;

    private final MenuAction mDeleteAll;

    private final MenuAction mUpdateBcaching;

    private final MenuAction mGetNearbyBcaching;

    private SourcesTab mSourcesTab;

    public SourceRowFactory(GuiState guiState, Activity activity, DbFrontend dbFrontend,
            MenuAction deleteAll, MenuAction updateBcaching, MenuAction getNearbyBcaching) {
        mGuiState = guiState;
        mActivity = activity;
        mDbFrontend = dbFrontend;
        mDeleteAll = deleteAll;
        mUpdateBcaching = updateBcaching;
        mGetNearbyBcaching = getNearbyBcaching;
    }

    public void setSourcesTab(SourcesTab sourcesTab) {
        mSourcesTab = sourcesTab;
    }

    public HeadlineRow makeHeadlineRow() {
        return new HeadlineRow();
    }

    public AllSourceRow makeAllSourceRow(String count) {
        AllSourceRow allSource = new AllSourceRow(mDeleteAll, mDbFrontend.getDatabasePath());
        allSource.setCount(count);
        return allSource;
    }

    public GpxSourceRow makeMyLocationRow(String count) {
        String label = mActivity.getString(R.string.my_location);
        Source source = Source.MY_LOCATION;
        Resources resources = mActivity.getResources();
        MenuActionDeleteSource deleteSource = new MenuActionDeleteSource(resources, source.mUnique,
                mGuiState, mDbFrontend);
        SourceContextMenu contextMenu = new SourceContextMenu(label, new MenuAction[] {
            deleteSource
        });

        GpxSourceRow sourceRow = new GpxSourceRow(label, source, contextMenu);
        sourceRow.setCount(count);
        sourceRow.setStatus("");
        return sourceRow;
    }

    public GpxSourceRow makeGpxRow(Source source, String label, String count) {
        Resources resources = mActivity.getResources();
        MenuActionDeleteSource deleteSource = new MenuActionDeleteSource(resources, source.mUnique,
                mGuiState, mDbFrontend);
        MenuActionDeleteFile deleteFile = new MenuActionDeleteFile(mSourcesTab, resources);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        MenuActionConfirm confirmDeleteFile = new MenuActionConfirm(mActivity, builder, deleteFile,
                resources.getString(R.string.menu_delete_file), resources.getString(
                        R.string.menu_confirm_delete_file, source.mFilename));
        SourceContextMenu contextMenu = new SourceContextMenu(label, new MenuAction[] {
                deleteSource, confirmDeleteFile
        });

        GpxSourceRow sourceRow = new GpxSourceRow(label, source, contextMenu);
        deleteFile.setSourceRow(sourceRow);
        sourceRow.setCount(count);
        return sourceRow;
    }

    public GpxSourceRow makeLocRow(Source source, String count) {
        String label = "LOC file " + source.mFilename;
        Resources resources = mActivity.getResources();
        MenuActionDeleteSource deleteSource = new MenuActionDeleteSource(resources, source.mUnique,
                mGuiState, mDbFrontend);
        MenuActionDeleteFile deleteFile = new MenuActionDeleteFile(mSourcesTab, resources);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        MenuActionConfirm confirmDeleteFile = new MenuActionConfirm(mActivity, builder, deleteFile,
                resources.getString(R.string.menu_delete_file), resources.getString(
                        R.string.menu_confirm_delete_file, source.mFilename));
        SourceContextMenu contextMenu = new SourceContextMenu(label, new MenuAction[] {
                deleteSource, confirmDeleteFile
        });

        GpxSourceRow sourceRow = new GpxSourceRow(label, source, contextMenu);
        deleteFile.setSourceRow(sourceRow);
        sourceRow.setCount(count);
        return sourceRow;
    }

    public BcachingSourceRow makeBcachingRow() {
        Resources resources = mActivity.getResources();
        MenuActionDeleteSource deleteSource = new MenuActionDeleteSource(resources,
                Source.BCACHING.mUnique, mGuiState, mDbFrontend);
        MenuAction[] menuActions = new MenuAction[] {
                mGetNearbyBcaching, mUpdateBcaching, deleteSource
        };
        SourceContextMenu contextMenu = new SourceContextMenu("bcaching.com", menuActions);

        return new BcachingSourceRow(contextMenu);
    }
}
