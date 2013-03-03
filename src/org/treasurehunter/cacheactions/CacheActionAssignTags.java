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
import org.treasurehunter.R;
import org.treasurehunter.Tags;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.Map;

/**
 * Shows a list of checkboxes for the user to choose manually which tags the
 * geocache should have.
 */
public class CacheActionAssignTags implements CacheAction {

    private final Activity mActivity;

    private final DbFrontend mDbFrontend;

    private final GuiState mGuiState;

    private boolean mHasChanged = false;

    public CacheActionAssignTags(Activity activity, DbFrontend dbFrontend, GuiState guiState) {
        mActivity = activity;
        mDbFrontend = dbFrontend;
        mGuiState = guiState;
    }

    @Override
    public void act(final Geocache geocache, Waypoint waypoint) {
        final Dialog dialog = new Dialog(mActivity);

        View rootView = mActivity.getLayoutInflater().inflate(R.layout.assign_tags, null);
        LinearLayout linearLayout = (LinearLayout)rootView
                .findViewById(R.id.AssignTagsLinearLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        final OnClickListener mOnSelect = new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkbox = (CheckBox)v;
                int tagId = v.getId();
                boolean checked = checkbox.isChecked();
                Log.d("TreasureHunter", "Setting tag " + tagId + " to "
                        + (checked ? "true" : "false"));
                mDbFrontend.setGeocacheTag(geocache.getId(), tagId, checked);
                mHasChanged = true;
            }
        };

        final OnDismissListener onDismiss = new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mHasChanged)
                    mGuiState.notifyDataViewChanged();
            }
        };
        dialog.setOnDismissListener(onDismiss);

        Map<Integer, String> allTags = Tags.GetAllTags();
        for (Integer i : allTags.keySet()) {
            String tagName = allTags.get(i);
            boolean hasTag = mDbFrontend.geocacheHasTag(geocache.getId(), i);
            CheckBox checkbox = new CheckBox(mActivity);
            checkbox.setChecked(hasTag);
            checkbox.setOnClickListener(mOnSelect);
            checkbox.setText(tagName);
            checkbox.setId(i);
            linearLayout.addView(checkbox, layoutParams);
        }

        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Assign tags to " + geocache.getId());

        dialog.setContentView(linearLayout);

        dialog.show();
    }

    @Override
    public String getLabel(Geocache geocache) {
        return mActivity.getResources().getString(R.string.assign_tags);
    }

}
