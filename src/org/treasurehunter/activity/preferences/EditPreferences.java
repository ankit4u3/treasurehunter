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

package org.treasurehunter.activity.preferences;

import org.treasurehunter.R;
import org.treasurehunter.database.DatabaseLocator;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class EditPreferences extends PreferenceActivity {
    CharSequence[] databaseEntryValues;

    CharSequence[] databaseEntries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        getDatabaseEntryValues();
        ListPreference listPreferenceDatabases = (ListPreference)findPreference("use-database");
        listPreferenceDatabases.setEntryValues(databaseEntryValues);
        listPreferenceDatabases.setEntries(databaseEntries);
    }

    private void getDatabaseEntryValues() {
        DatabaseLocator databaseLocator = new DatabaseLocator(this);
        databaseEntryValues = databaseLocator.getDatabaseList();
        if (databaseEntryValues.length == 0) {
            databaseEntryValues = new String[1];
            databaseEntryValues[0] = databaseLocator.getDefaultDatabaseName();
        }
        databaseEntries = databaseEntryValues;
    }

}
