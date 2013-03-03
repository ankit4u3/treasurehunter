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
import org.treasurehunter.database.ICachesProvider;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class CacheContextMenu implements OnCreateContextMenuListener {
    private final ICachesProvider mCachesProvider;

    private final CacheAction mCacheActions[];

    /** The geocache for which the menu was launched */
    private Geocache mGeocache = null;

    public CacheContextMenu(ICachesProvider cachesProvider, CacheAction cacheActions[]) {
        mCachesProvider = cachesProvider;
        mCacheActions = cacheActions;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo acmi = (AdapterContextMenuInfo)menuInfo;
        if (acmi.position > 0) {
            mGeocache = mCachesProvider.getCaches().get(acmi.position - 1);
            menu.setHeaderTitle(mGeocache.getId());
            for (int ix = 0; ix < mCacheActions.length; ix++) {
                menu.add(0, ix, ix, mCacheActions[ix].getLabel(mGeocache));
            }
        }
    }

    public boolean onContextItemSelected(MenuItem menuItem) {
        Log.d("TreasureHunter", "Context menu doing action " + menuItem.getItemId() + " = "
                + mCacheActions[menuItem.getItemId()].getClass().toString());
        mCacheActions[menuItem.getItemId()].act(mGeocache, null);
        return true;
    }

    public Geocache getGeocache() {
        return mGeocache;
    }
}
