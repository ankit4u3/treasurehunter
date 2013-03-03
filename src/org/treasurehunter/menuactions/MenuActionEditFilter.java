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
import org.treasurehunter.activity.main.GuiState;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Show a dialog to let the user edit the current filter for which geocaches to
 * display
 */
public class MenuActionEditFilter extends StaticLabelMenu implements MenuAction {
    private final Activity mActivity;

    private final GuiState mGuiState;

    private GeocacheFilter mFilter;

    public MenuActionEditFilter(Activity activity, GuiState guiState, Resources resources) {
        super(resources, R.string.menu_edit_filter);
        mActivity = activity;
        mGuiState = guiState;
    }

    private class DialogFilterGui implements GeocacheFilter.FilterGui {
        private Dialog mDialog;

        public DialogFilterGui(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public boolean getBoolean(int id) {
            return ((CompoundButton)mDialog.findViewById(id)).isChecked();
        }

        @Override
        public String getString(int id) {
            return ((EditText)mDialog.findViewById(id)).getText().toString();
        }

        @Override
        public void setBoolean(int id, boolean value) {
            ((CompoundButton)mDialog.findViewById(id)).setChecked(value);
        }

        @Override
        public void setString(int id, String value) {
            ((EditText)mDialog.findViewById(id)).setText(value);
        }
    };

    @Override
    public void act() {
        final Dialog dialog = new Dialog(mActivity);
        final DialogFilterGui gui = new DialogFilterGui(dialog);

        final OnClickListener mOnApply = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilter.loadFromGui(gui);
                mFilter.saveToPreferences();
                dialog.dismiss();
                mGuiState.setActiveFilter(mFilter);
            }
        };

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter);

        SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireFavorites, R.id.CheckBoxForbidFavorites);
        SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireFound, R.id.CheckBoxForbidFound);
        SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireDNF, R.id.CheckBoxForbidDNF);
        SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireNew, R.id.CheckBoxForbidNew);
        SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireMine, R.id.CheckBoxForbidMine);

        mFilter = mGuiState.getActiveFilter();
        TextView title = (TextView)dialog.findViewById(R.id.TextFilterTitle);
        title.setText("Editing filter \"" + mFilter.getName() + "\"");
        mFilter.pushToGui(gui);
        Button apply = (Button)dialog.findViewById(R.id.ButtonApplyFilter);
        apply.setOnClickListener(mOnApply);
        dialog.show();
    }

    private static final OnClickListener OnCheck = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final CheckBox checkBox = (CheckBox)v;
            if (checkBox.isChecked())
                ((CheckBox)checkBox.getTag()).setChecked(false);
        }
    };

    /**
     * Registers two checkboxes to be opposing -- selecting one will unselect
     * the other
     */
    private static void SetOpposingCheckBoxes(Dialog dialog, int id1, int id2) {
        CheckBox checkBox1 = (CheckBox)dialog.findViewById(id1);
        CheckBox checkBox2 = (CheckBox)dialog.findViewById(id2);
        checkBox1.setTag(checkBox2);
        checkBox1.setOnClickListener(OnCheck);
        checkBox2.setTag(checkBox1);
        checkBox2.setOnClickListener(OnCheck);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
