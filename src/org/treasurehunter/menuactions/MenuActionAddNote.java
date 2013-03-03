
package org.treasurehunter.menuactions;

import org.treasurehunter.R;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.view.Window;

public class MenuActionAddNote extends StaticLabelMenu implements MenuAction {
    private final Activity mActivity;

    private final GuiState mGuiState;

    private final DbFrontend mDbFrontend;

    public MenuActionAddNote(Activity activity, GuiState guiState, DbFrontend dbFrontend,
            Resources resources) {
        super(resources, R.string.menu_add_note);
        mActivity = activity;
        mGuiState = guiState;
        mDbFrontend = dbFrontend;
    }

    @Override
    public void act() {
        final Dialog dialog = new Dialog(mActivity);
        // final DialogFilterGui gui = new DialogFilterGui(dialog);
        //
        // final OnClickListener mOnApply = new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // mFilter.loadFromGui(gui);
        // mFilter.saveToPreferences();
        // dialog.dismiss();
        // mGuiState.setActiveFilter(mFilter);
        // if (mThread != null)
        // mThread.clearListUntilCalculated(mRefresher);
        // }
        // };

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.usernote);

        // SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireFavorites,
        // R.id.CheckBoxForbidFavorites);
        // SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireFound,
        // R.id.CheckBoxForbidFound);
        // SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireDNF,
        // R.id.CheckBoxForbidDNF);
        // SetOpposingCheckBoxes(dialog, R.id.CheckBoxRequireNew,
        // R.id.CheckBoxForbidNew);
        //
        // mFilter = mGuiState.getActiveFilter();
        // TextView title = (TextView)dialog.findViewById(R.id.TextFilterTitle);
        // title.setText("Editing filter \"" + mFilter.getName() + "\"");
        // mFilter.pushToGui(gui);
        // Button apply = (Button) dialog.findViewById(R.id.ButtonApplyFilter);
        // apply.setOnClickListener(mOnApply);
        dialog.show();
    }

    @Override
    public boolean isEnabled() {
        return mGuiState.getActiveGeocache() != null;
    }

}
