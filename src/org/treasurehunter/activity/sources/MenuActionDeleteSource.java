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
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.StaticLabelMenu;

import android.content.res.Resources;

public class MenuActionDeleteSource extends StaticLabelMenu implements MenuAction {
    private final GuiState mGuiState;

    private final DbFrontend mDbFrontend;

    private final String mSource;

    public MenuActionDeleteSource(Resources resources, String source, GuiState guiState,
            DbFrontend dbFrontend) {
        super(resources, R.string.menu_remove_source);
        mSource = source;
        mGuiState = guiState;
        mDbFrontend = dbFrontend;
    }

    @Override
    public void act() {
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        cacheWriter.beginTransaction();
        cacheWriter.deleteSource(mSource);
        cacheWriter.endTransaction();
        mGuiState.notifyDataViewChanged();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
