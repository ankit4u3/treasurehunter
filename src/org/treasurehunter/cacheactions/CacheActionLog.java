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
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.compass.CompassTabDI.FieldnoteLoggerFactory;
import org.treasurehunter.activity.compass.fieldnotes.FieldnoteLogger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.format.DateFormat;

public class CacheActionLog extends StaticLabelCache implements CacheAction {
    private final FieldnoteLoggerFactory mFieldnoteLoggerFactory;

    private final boolean mIsFound;

    private final SharedPreferences mSharedPreferences;

    private final Context mContext;

    public CacheActionLog(Activity activity, Resources resources, int idDialog,
            FieldnoteLoggerFactory fieldnoteLoggerFactory, SharedPreferences sharedPreferences) {
        super(resources, idDialog);
        mContext = activity.getApplicationContext();
        mFieldnoteLoggerFactory = fieldnoteLoggerFactory;
        mIsFound = (idDialog == R.string.menu_log_find);
        mSharedPreferences = sharedPreferences;
    }

    @Override
    public void act(Geocache geocache, Waypoint waypoint) {
        FieldnoteLogger fieldnoteLogger = mFieldnoteLoggerFactory
                .create(mIsFound, geocache.getId());

        java.text.DateFormat df = DateFormat.getTimeFormat(mContext);
        String time = df.format(System.currentTimeMillis());

        fieldnoteLogger.onPrepareDialog(mSharedPreferences, time);

        Dialog dialog = fieldnoteLogger.getDialog();
        dialog.show();
    }
}
