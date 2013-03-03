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

import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.R;
import org.treasurehunter.activity.filterlist.FilterTypeCollection;
import org.treasurehunter.activity.main.GuiState;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/** Show a popup dialog to let the user choose what filter to use */
public class MenuActionFilterListPopup extends StaticLabelMenu implements MenuAction {
    private final Activity mActivity;

    private final FilterTypeCollection mFilterTypeCollection;

    private final GuiState mGuiState;

    public MenuActionFilterListPopup(Activity activity, FilterTypeCollection filterTypeCollection,
            Resources resources, GuiState guiState) {
        super(resources, R.string.menu_choose_filter);
        mActivity = activity;
        mFilterTypeCollection = filterTypeCollection;
        mGuiState = guiState;
    }

    @Override
    public void act() {
        final Dialog dialog = new Dialog(mActivity);

        final RadioGroup radioGroup = new RadioGroup(mActivity);
        LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final OnClickListener mOnSelect = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int ix = radioGroup.getCheckedRadioButtonId();
                GeocacheFilter cacheFilter = mFilterTypeCollection.get(ix);
                Log.d("TreasureHunter", "Setting active filter to " + cacheFilter.getName()
                        + " with id " + cacheFilter.mId);
                mGuiState.setActiveFilter(cacheFilter);
                dialog.dismiss();
            }
        };
        radioGroup.setOnClickListener(mOnSelect);

        for (int i = 0; i < mFilterTypeCollection.getCount(); i++) {
            GeocacheFilter cacheFilter = mFilterTypeCollection.get(i);
            RadioButton newRadioButton = new RadioButton(mActivity);
            newRadioButton.setOnClickListener(mOnSelect);
            newRadioButton.setText(cacheFilter.getName());
            newRadioButton.setId(i);
            radioGroup.addView(newRadioButton, layoutParams);
        }
        int selected = mFilterTypeCollection.getIndexOf(mFilterTypeCollection.getActiveFilter());
        radioGroup.check(selected);

        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Choose filter");

        // dialog.setContentView(R.layout.filterlist);
        dialog.setContentView(radioGroup);

        // TextView title = (TextView)dialog.findViewById(R.id.TextFilterTitle);
        // title.setText(getLabel());

        dialog.show();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
