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

package org.treasurehunter.activity.details;

import org.treasurehunter.R;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.cacheactions.CacheActionAddWaypoint;
import org.treasurehunter.cacheactions.CacheActionEdit;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuActionAddNote;
import org.treasurehunter.menuactions.MenuActionCopyDetails;
import org.treasurehunter.menuactions.MenuActionFromCacheAction;
import org.treasurehunter.menuactions.MenuActions;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

public class DetailsTabDI {

    public static DetailsTab create(Activity activity, GuiState guiState, DbFrontend dbFrontend,
            LayoutInflater layoutInflater, SharedPreferences sharedPreferences) {
        View contentView = layoutInflater.inflate(R.layout.cache_details, null);
        Resources resources = activity.getResources();

        final MenuActions menuActions = new MenuActions();
        menuActions.add(new MenuActionCopyDetails(activity, guiState, dbFrontend, resources));
        menuActions.add(new MenuActionAddNote(activity, guiState, dbFrontend, resources));
        menuActions.add(new MenuActionFromCacheAction(new CacheActionAddWaypoint(activity,
                resources, dbFrontend), guiState));
        menuActions.add(new MenuActionFromCacheAction(new CacheActionEdit(activity, resources),
                guiState));

        Drawable icon = resources.getDrawable(R.drawable.ic_menu_view);

        DetailsTab detailsDelegate = new DetailsTab(contentView, icon, menuActions, guiState,
                dbFrontend, activity, sharedPreferences);
        return detailsDelegate;
    }
}
