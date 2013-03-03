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

package org.treasurehunter.menuactions;

import org.treasurehunter.R;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.bcaching.BcachingConfig;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;

import android.content.res.Resources;

/** Deletes all caches in the database */
public class MenuActionDeleteAll extends StaticLabelMenu implements MenuAction {
    private final DbFrontend mDbFrontend;

    private final GuiState mGuiState;

    private final BcachingConfig mBcachingConfig;

    public MenuActionDeleteAll(DbFrontend dbFrontend, GuiState guiState,
            BcachingConfig bcachingConfig, Resources resources) {
        super(resources, R.string.delete_all_caches);
        mDbFrontend = dbFrontend;
        mGuiState = guiState;
        mBcachingConfig = bcachingConfig;
    }

    @Override
    public void act() {
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();

        cacheWriter.beginTransaction();

        cacheWriter.deleteAll();
        cacheWriter.endTransaction();
        mDbFrontend.flushTotalCount();

        mBcachingConfig.clearLastUpdate();
        mGuiState.notifyDataViewChanged();
    }

    @Override
    public boolean isEnabled() {
        return mDbFrontend.countAll() != 0;
    }
}
