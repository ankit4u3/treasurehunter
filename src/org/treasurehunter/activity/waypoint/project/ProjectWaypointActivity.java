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

import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.compass.GeoUtils;
import org.treasurehunter.activity.compass.Util;
import org.treasurehunter.database.DatabaseLocator;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ProjectWaypointActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.waypoint_project);
        final Intent intent = getIntent();

        final GeocacheFactory geocacheFactory = new GeocacheFactory();
        DatabaseLocator databaseLocator = new DatabaseLocator(this);
        DbFrontend dbFrontend = new DbFrontend(databaseLocator, geocacheFactory);

        // Get starting coordinates:
        Geocache geocache = dbFrontend.loadCacheFromId(intent.getStringExtra(Geocache.ID));

        Waypoint waypoint = null;
        String waypointId = intent.getStringExtra(Geocache.WAYPOINTID);
        if (waypointId != null && !waypointId.equals("")) {
            waypoint = dbFrontend.loadWaypointFromId(waypointId);
        }

        EditText startCoords = (EditText)findViewById(R.id.project_start_coords);

        double orgLat;
        double orgLon;
        if (waypoint == null) {
            orgLat = geocache.getLatitude();
            orgLon = geocache.getLongitude();
        } else {
            orgLat = waypoint.getLatitude();
            orgLon = waypoint.getLongitude();
        }

        String stringCoords = Util.formatDegreesCoordsAsStringNWSE(orgLat, orgLon);
        startCoords.setText(stringCoords);

        // Other UI widgets:
        EditText distance = (EditText)findViewById(R.id.project_distance_length);
        EditText bearing = (EditText)findViewById(R.id.project_bearing_degrees);
        TextView endCoords = (TextView)findViewById(R.id.project_end_coords);

        // Initialize spinner:
        Spinner unitSpinner = (Spinner)findViewById(R.id.project_distance_unit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.project_distance_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        final SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (mSharedPreferences.getBoolean("imperial", false)) {
            unitSpinner.setSelection(GeoUtils.UNIT_FEET);
        } else {
            unitSpinner.setSelection(GeoUtils.UNIT_METERS);
        }

        // Instantiate delegate:
        final ProjectWaypointActivityDelegate projectWaypoint = new ProjectWaypointActivityDelegate(
                startCoords, distance, bearing, endCoords, unitSpinner, dbFrontend, geocache,
                waypoint, geocacheFactory, this.getResources(), this);

        // Instantiate and set listeners:
        final ProjectWaypointActivityDelegate.CancelButtonOnClickListener cancelButtonOnClickListener = new ProjectWaypointActivityDelegate.CancelButtonOnClickListener(
                this);

        ((Button)findViewById(R.id.project_cancel)).setOnClickListener(cancelButtonOnClickListener);

        final ProjectWaypointActivityDelegate.ProjectNewWaypointOnClickListener ProjectNewWaypointOnClickListener = new ProjectWaypointActivityDelegate.ProjectNewWaypointOnClickListener(
                this, projectWaypoint);

        ((Button)findViewById(R.id.project_new_waypoint))
                .setOnClickListener(ProjectNewWaypointOnClickListener);

        final ProjectWaypointActivityDelegate.UnitOnSelectListener UnitOnSelectListener = new ProjectWaypointActivityDelegate.UnitOnSelectListener(
                projectWaypoint);

        unitSpinner.setOnItemSelectedListener(UnitOnSelectListener);

        final ProjectWaypointActivityDelegate.TextEditWatcher TextEditWatcher = new ProjectWaypointActivityDelegate.TextEditWatcher(
                projectWaypoint);

        startCoords.addTextChangedListener(TextEditWatcher);
        distance.addTextChangedListener(TextEditWatcher);
        bearing.addTextChangedListener(TextEditWatcher);

        // Make initial projection using original coordinates and default
        // values:
        projectWaypoint.projectWaypoint();
    }
}
