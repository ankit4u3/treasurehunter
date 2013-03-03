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

package org.treasurehunter.activity.waypoint.project;

import org.treasurehunter.Clock;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.compass.GeoUtils;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.activity.edit.EditCacheActivity;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ProjectWaypointActivityDelegate {

    public static class CancelButtonOnClickListener implements OnClickListener {
        private final Activity mActivity;

        public CancelButtonOnClickListener(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onClick(View v) {
            mActivity.setResult(Activity.RESULT_CANCELED, null);
            mActivity.finish();
        }
    }

    /**
     * Action to take when the user clicks the Project Waypoint button.
     */
    public static class ProjectNewWaypointOnClickListener implements OnClickListener {
        private final Activity mActivity;

        private final ProjectWaypointActivityDelegate mProjectWaypoint;

        public ProjectNewWaypointOnClickListener(Activity activity,
                ProjectWaypointActivityDelegate projectWaypointDelegate) {
            mActivity = activity;
            mProjectWaypoint = projectWaypointDelegate;
        }

        @Override
        public void onClick(View v) {
            String id = mProjectWaypoint.createNewWaypoint();
            final Intent i = new Intent();

            if (id != null && !id.equals("")) {
                i.putExtra(Geocache.NAVIGATE_TO_NEW_CACHE, true);
                i.putExtra(Geocache.WAYPOINTID, id);
            }
            mActivity.setResult(Activity.RESULT_OK, i);
            mActivity.finish();
        }
    }

    /**
     * The methods of UnitOnSelectListener are called when the user changes the
     * distance unit (Meters, Kilometers,...).
     */
    public static class UnitOnSelectListener implements OnItemSelectedListener {
        private final ProjectWaypointActivityDelegate mProjectWaypoint;

        public UnitOnSelectListener(ProjectWaypointActivityDelegate projectWaypointDelegate) {
            mProjectWaypoint = projectWaypointDelegate;
        }

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mProjectWaypoint.projectWaypoint();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // Not needed
        }

    }

    /**
     * The methods of TextEditWatcher are called when the user makes changes to
     * any of the text input fields (coordinates, distance, bearing).
     */
    public static class TextEditWatcher implements TextWatcher {
        private final ProjectWaypointActivityDelegate mProjectWaypoint;

        public TextEditWatcher(ProjectWaypointActivityDelegate projectWaypointDelegate) {
            mProjectWaypoint = projectWaypointDelegate;
        }

        @Override
        public void afterTextChanged(Editable s) {
            mProjectWaypoint.projectWaypoint();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not needed
        }

    }

    private final EditText mStartCoords;

    private final EditText mDistance;

    private final EditText mBearing;

    private final TextView mEndCoords;

    private final Spinner mUnitSpinner;

    private final DbFrontend mDbFrontend;

    private final GeocacheFactory mGeocacheFactory;

    private final Resources mResources;

    private final Activity mActivity;

    private double mEndLat;

    private double mEndLon;

    private Geocache mOriginalGeocache;

    private Waypoint mOriginalWaypoint;

    public ProjectWaypointActivityDelegate(EditText startCoords, EditText distance,
            EditText bearing, TextView endCoords, Spinner unitSpinner, DbFrontend dbFrontend,
            Geocache geocache, Waypoint waypoint, GeocacheFactory geocacheFactory,
            Resources resources, Activity activity) {
        mStartCoords = startCoords;
        mDistance = distance;
        mBearing = bearing;
        mEndCoords = endCoords;
        mUnitSpinner = unitSpinner;
        mDbFrontend = dbFrontend;
        mOriginalGeocache = geocache;
        mOriginalWaypoint = waypoint;
        mGeocacheFactory = geocacheFactory;
        mResources = resources;
        mActivity = activity;
    }

    /**
     * ProjectWaypoint performs the projection calculation.
     */
    public void projectWaypoint() {
        try {
            double bearing = Double.parseDouble(mBearing.getText().toString());
            double distance = Double.parseDouble(mDistance.getText().toString());
            distance = GeoUtils.convertToKm(distance, mUnitSpinner.getSelectedItemPosition());
            CharSequence latLon[] = Util.splitLatLon(mStartCoords.getText().toString());

            if (latLon.length == 2) {
                double lat = Util.parseCoordinate(latLon[0]);
                double lon = Util.parseCoordinate(latLon[1]);
                double[] projectedCoords = GeoUtils.project(distance, bearing, lat, lon);
                mEndLat = projectedCoords[0];
                mEndLon = projectedCoords[1];

                String coordString = Util.formatDegreesCoordsAsStringNWSE(mEndLat, mEndLon);
                mEndCoords.setText(coordString);
            } else {
                mEndCoords.setText(R.string.projection_invalid_start_coords);
            }
        } catch (NumberFormatException nx) {
            mEndCoords.setText(R.string.projection_invalid_input);
        }
    }

    /**
     * CreateNewWaypoint uses the coordinates from the projection calculation to
     * create a new waypoint. After the waypoint is created, the application
     * navigates to the Edit Cache action.
     */
    public String createNewWaypoint() {
        long time = System.currentTimeMillis();
        String id = String.format("ML%1$tk%1$tM%1$tS", time);

        CharSequence originalName = (mOriginalWaypoint != null) ? mOriginalWaypoint.getName()
                : mOriginalGeocache.getName();

        String name = mResources.getString(R.string.projection_name_format, time, originalName);
        Waypoint waypoint = mGeocacheFactory.createWaypoint(id, name, mEndLat, mEndLon,
                Source.MY_LOCATION, GeocacheType.WAYPOINT, mOriginalGeocache.getId());

        if (waypoint == null) {
            // TODO: error handling in this case.
            // mErrorDisplayer.displayError(R.string.current_location_null);
            Log.e("TreasureHunter", "Could not create waypoint " + id);
            return "";
        }

        String now = Clock.getCurrentStringTime();
        CacheWriter cacheWriter = mDbFrontend.getCacheWriter();
        cacheWriter.beginTransaction();
        boolean updated = cacheWriter.conditionallyWriteWaypoint(waypoint.getId(),
                waypoint.getName(), waypoint.getLatitude(), waypoint.getLongitude(),
                waypoint.getSource(), waypoint.getCacheType(), waypoint.getParentCache(), now);
        cacheWriter.endTransaction();

        Log.d("TreasureHunter", "ProjectWaypoint updated = " + updated);

        Intent intent = new Intent(mActivity, EditCacheActivity.class);
        intent.putExtra(Waypoint.ID, id);
        mActivity.startActivityForResult(intent, 0);

        return id;
    }
}
