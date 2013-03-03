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

package org.treasurehunter.cacheactions;

import org.treasurehunter.Geocache;
import org.treasurehunter.Tags;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

public class CacheActionToggleFavorite implements CacheAction {
    private final DbFrontend mDbFrontend;

    private final GuiState mGuiState;

    public CacheActionToggleFavorite(DbFrontend dbFrontend, GuiState guiState) {
        mDbFrontend = dbFrontend;
        mGuiState = guiState;
    }

    @Override
    public void act(Geocache geocache, Waypoint waypoint) {
        boolean isFavorite = mDbFrontend.geocacheHasTag(geocache.getId(), Tags.FAVORITE);
        mDbFrontend.setGeocacheTag(geocache.getId(), Tags.FAVORITE, !isFavorite);
        mGuiState.notifyDataViewChanged();
    }

    @Override
    public String getLabel(Geocache geocache) {
        boolean isFavorite = mDbFrontend.geocacheHasTag(geocache.getId(), Tags.FAVORITE);
        return isFavorite ? "Remove from Favorites" : "Add to Favorites";
    }
}
