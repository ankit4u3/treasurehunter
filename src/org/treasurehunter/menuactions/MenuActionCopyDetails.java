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
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.ClipboardManager;
import android.widget.Toast;

/** Copy Cache details to clipboard */
public class MenuActionCopyDetails extends StaticLabelMenu implements MenuAction {
    private final Activity mActivity;

    private final GuiState mGuiState;

    private final DbFrontend mDbFrontend;

    public MenuActionCopyDetails(Activity activity, GuiState guiState, DbFrontend dbFrontend,
            Resources resources) {
        super(resources, R.string.menu_copy_details);
        mActivity = activity;
        mGuiState = guiState;
        mDbFrontend = dbFrontend;
    }

    @Override
    public void act() {
        String geocacheId = mGuiState.getActiveGeocacheId();

        if (geocacheId.equals("")) {
            return;
        }

        String cacheDetails = mDbFrontend.getCacheDetails(geocacheId).mLongDescription;

        // Strip HTML from string:
        cacheDetails = cacheDetails.replaceAll("<br[^>]*>", "\n");
        cacheDetails = cacheDetails.replaceAll("<p>", "\n");
        cacheDetails = cacheDetails.replaceAll("<[^>]*>", "");

        // Copy description to clipboard:
        ClipboardManager clipboard = (ClipboardManager)mActivity
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(cacheDetails);

        // Send notification to user:
        Toast toast = Toast.makeText(mActivity.getApplicationContext(),
                R.string.toast_clipboard_copy, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public boolean isEnabled() {
        return mGuiState.getActiveGeocache() != null;
    }
}
