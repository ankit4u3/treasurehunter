
package org.treasurehunter.activity.compass;

import org.treasurehunter.R;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class WaypointButtonOnClickListener implements android.view.View.OnClickListener {
    public Activity mActivity;

    public GuiState mGuiState;

    public CharSequence mGeocacheID;

    public DbFrontend mDbFrontend;

    public List<Waypoint> mWaypoints;

    public WaypointButtonOnClickListener(Activity activity, GuiState guiState, DbFrontend dbFrontend) {
        mActivity = activity;
        mGuiState = guiState;
        mDbFrontend = dbFrontend;
    }

    @Override
    public void onClick(View v) {
        if (mGuiState.getActiveGeocache() != null) {
            mGeocacheID = mGuiState.getActiveGeocache().getId();
        } else if (mGuiState.getActiveWaypoint() != null) {
            mGeocacheID = mGuiState.getActiveWaypoint().getParentCache();
        } else {
            return;
        }

        CharSequence currentWaypointId = mGuiState.getActiveWaypointId();

        mWaypoints = mDbFrontend.getRelatedWaypoints(mGeocacheID);

        final CharSequence[] items = new CharSequence[mWaypoints.size() + 1];

        items[0] = mGeocacheID + " " + mGuiState.getActiveGeocache().getName();

        int i = 1;
        int SelectedItem = 0; // By default main cache is selected
        for (Waypoint wp : mWaypoints) {
            if (currentWaypointId != null && wp.getId().equals(currentWaypointId)) {
                SelectedItem = i;
            }
            items[i] = wp.toString();
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.waypoints);
        builder.setSingleChoiceItems(items, SelectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(mActivity.getApplicationContext(), items[item], Toast.LENGTH_SHORT)
                        .show();

                if (item == 0) {
                    mGuiState.setCurrentGeocache(mGeocacheID);
                    mGuiState.notifyDataViewChanged();
                } else {
                    mGuiState.setCurrentWaypoint(mWaypoints.get(item - 1).getId());
                    mGuiState.notifyDataViewChanged();
                }

                dialog.dismiss();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }
}
