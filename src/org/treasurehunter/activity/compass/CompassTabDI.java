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

package org.treasurehunter.activity.compass;

import org.treasurehunter.ErrorDisplayer;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GraphicsGenerator;
import org.treasurehunter.R;
import org.treasurehunter.Toaster;
import org.treasurehunter.activity.cachelist.CacheListRowInflater;
import org.treasurehunter.activity.compass.GeocacheViewer.AttributeViewer;
import org.treasurehunter.activity.compass.GeocacheViewer.DrawableImages;
import org.treasurehunter.activity.compass.GeocacheViewer.LabelledAttributeViewer;
import org.treasurehunter.activity.compass.GeocacheViewer.ResourceImages;
import org.treasurehunter.activity.compass.GeocacheViewer.UnlabelledAttributeViewer;
import org.treasurehunter.activity.compass.fieldnotes.CacheLogger;
import org.treasurehunter.activity.compass.fieldnotes.DialogHelperCommon;
import org.treasurehunter.activity.compass.fieldnotes.DialogHelperFile;
import org.treasurehunter.activity.compass.fieldnotes.DialogHelperSms;
import org.treasurehunter.activity.compass.fieldnotes.FieldnoteLogger;
import org.treasurehunter.activity.compass.fieldnotes.FieldnoteLogger.OnClickCancel;
import org.treasurehunter.activity.compass.fieldnotes.FieldnoteLogger.OnClickOk;
import org.treasurehunter.activity.compass.fieldnotes.FieldnoteStringsFVsDnf;
import org.treasurehunter.activity.compass.fieldnotes.FileLogger;
import org.treasurehunter.activity.compass.fieldnotes.SmsLogger;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.cacheactions.CacheActionCacheWebPage;
import org.treasurehunter.cacheactions.CacheActionEdit;
import org.treasurehunter.cacheactions.CacheActionGoogleMaps;
import org.treasurehunter.cacheactions.CacheActionLog;
import org.treasurehunter.cacheactions.CacheActionProjectWaypoint;
import org.treasurehunter.cacheactions.CacheActionProximity;
import org.treasurehunter.cacheactions.CacheActionRMaps;
import org.treasurehunter.cacheactions.CacheActionRadar;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuAction;
import org.treasurehunter.menuactions.MenuActionEnableGPS;
import org.treasurehunter.menuactions.MenuActionFromCacheAction;
import org.treasurehunter.menuactions.MenuActionSettings;
import org.treasurehunter.menuactions.MenuActions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class CompassTabDI {

    public static CompassTab create(final Activity activity, GuiState guiState,
            DbFrontend dbFrontend, GeoFixProvider geoFixProvider, LayoutInflater layoutInflater) {

        View layout = layoutInflater.inflate(R.layout.compass, null);
        final OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        };
        final ErrorDisplayer errorDisplayer = new ErrorDisplayer(activity, mOnClickListener);
        View webPageButton = layout.findViewById(R.id.cache_page);
        View detailsButton = layout.findViewById(R.id.cache_details);
        View waypointsButton = layout.findViewById(R.id.cache_waypoints);

        final TextView gcid = (TextView)layout.findViewById(R.id.gcid);
        final Resources resources = activity.getResources();
        final GraphicsGenerator graphicsGenerator = new GraphicsGenerator(resources);

        final Drawable[] ratings = graphicsGenerator.getRatings(resources);

        final ImageView difficultyImageView = (ImageView)layout.findViewById(R.id.gc_difficulty);
        final TextView terrainTextView = (TextView)layout.findViewById(R.id.gc_text_terrain);
        final ImageView terrainImageView = (ImageView)layout.findViewById(R.id.gc_terrain);
        final TextView difficultyTextView = (TextView)layout.findViewById(R.id.gc_text_difficulty);
        final ImageView containerImageView = (ImageView)layout.findViewById(R.id.gccontainer);
        final DrawableImages ribbonImagesOnDifficulty = new DrawableImages(difficultyImageView,
                ratings);
        final AttributeViewer gcDifficulty = new LabelledAttributeViewer(difficultyTextView,
                difficultyImageView, ribbonImagesOnDifficulty);
        final DrawableImages pawImagesOnTerrain = new DrawableImages(terrainImageView, ratings);
        final AttributeViewer gcTerrain = new LabelledAttributeViewer(terrainTextView,
                terrainImageView, pawImagesOnTerrain);
        final ResourceImages containerImagesOnContainer = new ResourceImages(containerImageView,
                GeocacheViewer.CONTAINER_IMAGES);
        final UnlabelledAttributeViewer gcContainer = new UnlabelledAttributeViewer(
                containerImageView, containerImagesOnContainer);

        final TextView gcName = (TextView)layout.findViewById(R.id.gcname);
        RadarView radar = (RadarView)layout.findViewById(R.id.radarview);
        radar.setUseImperial(false);
        radar.setDistanceView((TextView)layout.findViewById(R.id.radar_distance),
                (TextView)layout.findViewById(R.id.radar_bearing),
                (TextView)layout.findViewById(R.id.radar_accuracy),
                (TextView)layout.findViewById(R.id.radar_lag));
        ImageView favorite = (ImageView)layout.findViewById(R.id.gcfavorite);
        CacheListRowInflater.CacheNameStyler styler = new CacheListRowInflater.CacheNameStyler();
        final GeocacheViewer geocacheViewer = new GeocacheViewer(radar, gcid, gcName,
                (ImageView)layout.findViewById(R.id.gcicon), gcDifficulty, gcTerrain,
                gcContainer/* , favorite */, styler, dbFrontend);

        CompassTab.RadarViewRefresher radarViewRefresher = new CompassTab.RadarViewRefresher(radar,
                geoFixProvider);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(activity);

        AlertDialog.Builder fieldnoteBuilder = new AlertDialog.Builder(activity);
        FieldnoteLoggerFactory fieldnoteLoggerFactory = new FieldnoteLoggerFactory(layoutInflater,
                fieldnoteBuilder, dbFrontend, activity, guiState);
        Drawable icon = resources.getDrawable(R.drawable.ic_menu_compass);

        CacheActionLog logFind = new CacheActionLog(activity, resources, R.string.menu_log_find,
                fieldnoteLoggerFactory, sharedPreferences);
        CacheActionLog logDnf = new CacheActionLog(activity, resources, R.string.menu_log_dnf,
                fieldnoteLoggerFactory, sharedPreferences);

        CacheActionProjectWaypoint projectWaypoint = new CacheActionProjectWaypoint(activity,
                resources);

        final MenuAction[] menuActionArray = {
                new MenuActionFromCacheAction(new CacheActionRMaps(resources, activity,
                        errorDisplayer), guiState),
                new MenuActionFromCacheAction(new CacheActionEdit(activity, resources), guiState),
                new MenuActionFromCacheAction(logFind, guiState),
                new MenuActionFromCacheAction(logDnf, guiState),
                new MenuActionSettings(activity, resources),
                new MenuActionFromCacheAction(new CacheActionGoogleMaps(resources, activity),
                        guiState),
                new MenuActionFromCacheAction(new CacheActionProximity(activity, resources),
                        guiState), new MenuActionFromCacheAction(projectWaypoint, guiState),
                new MenuActionEnableGPS(activity, resources)
        };
        final MenuActions menuActions = new MenuActions(menuActionArray);
        final PowerManager pm = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "TreasureHunter_compass_lock");
        CompassTab compassDelegate = new CompassTab(activity, geocacheViewer, dbFrontend, radar,
                sharedPreferences, favorite, guiState, layout, detailsButton, webPageButton,
                waypointsButton, icon, radarViewRefresher, geoFixProvider, menuActions, wakeLock);

        final CacheActionCacheWebPage cacheWebPage = new CacheActionCacheWebPage(activity,
                resources);
        String errorMsg = activity.getString(R.string.error_activity_not_found);
        final CacheButtonOnClickListener cacheButtonOnClickListener = new CacheButtonOnClickListener(
                cacheWebPage, compassDelegate, errorMsg, errorDisplayer, sharedPreferences);
        layout.findViewById(R.id.cache_page).setOnClickListener(cacheButtonOnClickListener);

        final WaypointButtonOnClickListener waypointButtonOnClickListener = new WaypointButtonOnClickListener(
                activity, guiState, dbFrontend);

        ((Button)layout.findViewById(R.id.cache_waypoints))
                .setOnClickListener(waypointButtonOnClickListener);

        layout.findViewById(R.id.radarview).setOnClickListener(
                new CacheButtonOnClickListener(new CacheActionRadar(activity, resources),
                        compassDelegate,
                        "Please install the GPS Status application for enhanced tracking.",
                        errorDisplayer, sharedPreferences));

        return compassDelegate;
    }

    public static class FieldnoteLoggerFactory {
        private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm'Z'");

        private final LayoutInflater mLayoutInflater;

        private final AlertDialog.Builder mBuilder;

        private final DbFrontend mDbFrontend;

        private final Context mContext;

        private final GuiState mGuiState;

        public FieldnoteLoggerFactory(LayoutInflater layoutInflater, AlertDialog.Builder builder,
                DbFrontend dbFrontend, Context context, GuiState guiState) {
            mLayoutInflater = layoutInflater;
            mBuilder = builder;
            mDbFrontend = dbFrontend;
            mContext = context;
            mGuiState = guiState;
        }

        public FieldnoteLogger create(boolean isFound, CharSequence geocacheId) {
            final FieldnoteStringsFVsDnf fieldnoteStringsFVsDnf = new FieldnoteStringsFVsDnf(
                    mContext.getResources());
            final Toaster toaster = new Toaster(mContext, R.string.error_writing_cache_log, true);
            final FileLogger fileLogger = new FileLogger(fieldnoteStringsFVsDnf, simpleDateFormat,
                    toaster);
            final SmsLogger smsLogger = new SmsLogger(fieldnoteStringsFVsDnf, mContext);
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            final CacheLogger cacheLogger = new CacheLogger(sharedPreferences, fileLogger,
                    smsLogger);
            final View fieldNoteDialogView = mLayoutInflater.inflate(R.layout.fieldnote, null);
            final TextView fieldnoteCaveat = (TextView)fieldNoteDialogView
                    .findViewById(R.id.fieldnote_caveat);

            boolean isDnf = !isFound;
            final EditText editText = (EditText)fieldNoteDialogView.findViewById(R.id.fieldnote);
            final DialogHelperCommon dialogHelperCommon = new DialogHelperCommon(
                    fieldnoteStringsFVsDnf, editText, isDnf, fieldnoteCaveat);

            final DialogHelperFile dialogHelperFile = new DialogHelperFile(fieldnoteCaveat,
                    mContext);
            final DialogHelperSms dialogHelperSms = new DialogHelperSms(geocacheId.length(),
                    fieldnoteStringsFVsDnf, editText, isDnf, fieldnoteCaveat);

            final OnClickOk onClickOk = new OnClickOk(geocacheId, editText, cacheLogger,
                    mDbFrontend, mGuiState, isDnf);
            final OnClickCancel onClickCancel = new OnClickCancel();
            mBuilder.setTitle(R.string.field_note_title);
            mBuilder.setView(fieldNoteDialogView);
            mBuilder.setNegativeButton(R.string.cancel, onClickCancel);
            mBuilder.setPositiveButton(R.string.log_cache, onClickOk);
            Dialog dialog = mBuilder.create();

            FieldnoteLogger fieldnoteLogger = new FieldnoteLogger(dialogHelperCommon,
                    dialogHelperFile, dialogHelperSms, dialog);
            return fieldnoteLogger;
        }
    }
}
