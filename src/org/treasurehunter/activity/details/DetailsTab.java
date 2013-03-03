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

package org.treasurehunter.activity.details;

import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.GeocacheTypeFactory;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.edit.EditCacheActivity;
import org.treasurehunter.activity.main.GuiState;
import org.treasurehunter.activity.main.TabBase;
import org.treasurehunter.database.DbFrontend;
import org.treasurehunter.menuactions.MenuActions;
import org.treasurehunter.xmlimport.GeocacheDetails;
import org.treasurehunter.xmlimport.GeocacheDetails.GeocacheLog;
import org.treasurehunter.xmlimport.GeocacheDetails.Travelbug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class DetailsTab extends TabBase {
    private static Map<Pattern, String> mPatterns = null;

    private final GuiState mGuiState;

    private final DbFrontend mDbFrontend;

    /** The geocache currently displayed */
    private String mGeocacheId = "";

    private String mWaypointId = "";

    private WebView mWebView;

    private boolean mError = false;

    private Activity mActivity;

    public DetailsTab(View contentView, Drawable icon, MenuActions menuActions, GuiState guiState,
            DbFrontend dbFrontend, Activity activity, SharedPreferences sharedPreferences) {
        super("tab_details", getIconLabel(sharedPreferences), contentView, icon, menuActions);
        mGuiState = guiState;
        mDbFrontend = dbFrontend;
        mActivity = activity;
    }

    private static String getIconLabel(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("ui_show_tab_texts", true) ? "Details" : "";
    }

    @Override
    public void onCreate() {
        mWebView = (WebView)getContentView().findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new SelectGeoobjectJSInterface(), "waypoint");
        setText("<html><body><i>No Questions Available Reach Hotspot To get Questions </i></body></html>");
    }

    @Override
    public void onResume() {
        updateHtml();
    }

    private void updateHtml() {
        if (mGuiState.getActiveGeocacheId().equals(mGeocacheId)
                && mGuiState.getActiveWaypoint() != null
                && mGuiState.getActiveWaypoint().equals(mWaypointId) && !mError)
            return;
        mError = false;

        mWebView.clearView();
        mGeocacheId = mGuiState.getActiveGeocacheId();
        mWaypointId = mGuiState.getActiveWaypointId();

        if (mGeocacheId.equals("")) {
            setText("<html><body><i>No Questions Available Reach Hotspot To get Questions </i></body></html>");
            return;
        }
        setText("<html><body><i>Loading...</i></body></html>");

        Geocache geocache = mDbFrontend.loadCacheFromId(mGeocacheId);
        GeocacheDetails details = mDbFrontend.getCacheDetails(mGeocacheId);
        List<Waypoint> waypoints = mDbFrontend.getRelatedWaypoints(mGeocacheId);
        List<GeocacheLog> logs = mDbFrontend.getGeocacheLogs(mGeocacheId);
        List<Travelbug> travelbugs = mDbFrontend.getTravelbugs(mGeocacheId);

        Waypoint waypoint = mDbFrontend.loadWaypointFromId(mWaypointId);

        setText(getDetailsHtml(geocache, details, logs, travelbugs, waypoints, waypoint));
    }

    private void setText(String text) {
        mWebView.loadDataWithBaseURL(null, text, "text/html", "utf-8", "about:blank");
    }

    private static Map<Pattern, String> getPatterns() {
        if (mPatterns != null)
            return mPatterns;

        mPatterns = new LinkedHashMap<Pattern, String>(30);
        String[] bbColours = {
                "blue", "red", "purple", "green", "maroon", "orange", "navy", "black", "pink",
                "brown", "white", "violet", "yellow"
        };
        for (String colour : bbColours) {
            mPatterns.put(Pattern.compile("(?i)\\[" + colour + "\\]"), "<font color=\"" + colour
                    + "\">");
            mPatterns.put(Pattern.compile("(?i)\\[/" + colour + "\\]"), "</font>");
        }

        mPatterns.put(Pattern.compile("(?i)\\[br\\]"), "<br/>");
        mPatterns.put(Pattern.compile("(?i)\\[(\\/?[ib])\\]"), "<$1>");
        mPatterns.put(Pattern.compile("(?i)\\[size=([^\\]]+)\\]"), "<font size=\"$1\">");
        mPatterns.put(Pattern.compile("(?i)\\[/size\\]"), "</font>");
        mPatterns.put(Pattern.compile("(?i)\\[code\\]"), "<pre>");
        mPatterns.put(Pattern.compile("(?i)\\[/code\\]"), "</pre>");
        mPatterns.put(Pattern.compile("(?i)\\[url=([^\\]]+)\\]"),
                "<a href=\"$1\" target=\"_blank\">");
        mPatterns.put(Pattern.compile("(?i)\\[/url\\]"), "</a>");

        /* Emoticons: */
        for (String smiley : GeocacheDetails.GeocacheLog.SMILEY_TO_ICON.keySet()) {
            String smileyImage = GeocacheDetails.GeocacheLog.SMILEY_TO_ICON.get(smiley);
            mPatterns.put(Pattern.compile("\\[" + Pattern.quote(smiley) + "\\]"),
                    "<img src=\"file:///android_asset/" + smileyImage
                            + "\" border=\"0\" align=\"baseline\" height=\"15\" width=\"15\" />");
        }

        return mPatterns;
    }

    private static String bbcodeToHTML(String text) {
        String convertedText = text;

        for (Entry<Pattern, String> entry : getPatterns().entrySet()) {
            convertedText = entry.getKey().matcher(convertedText).replaceAll(entry.getValue());
        }

        return convertedText;
    }

    private static String getDetailsHtml(Geocache geocache, GeocacheDetails details,
            List<GeocacheLog> logs, List<Travelbug> travelbugs, List<Waypoint> waypoints,
            Waypoint currentWaypoint) {
        StringBuilder htmlOut = new StringBuilder();

        htmlOut.append("<html>\n");
        htmlOut.append("<head>\n");
        htmlOut.append("<script type=\"text/javascript\" src=\"file:///android_asset/details.js\"></script>\n");
        htmlOut.append("</head>\n");
        htmlOut.append("<body>\n");

        printAttributes(geocache, details, htmlOut);

        printDescription(details, htmlOut);

        printHint(details, htmlOut);

        printWaypoints(waypoints, htmlOut, geocache, currentWaypoint);

        printLogs(logs, htmlOut);
        printTravelbugs(travelbugs, htmlOut);

        htmlOut.append("</body>\n");
        htmlOut.append("</html>\n");

        return htmlOut.toString();
    }

    private static void printTravelbugs(List<Travelbug> travelbugs, StringBuilder htmlOut) {
        if (travelbugs != null && travelbugs.size() > 0) {
            switch (travelbugs.size()) {
                case 1:
                    htmlOut.append("<br><b>1 TravelBug:</b><br/>\n");
                    break;
                default:
                    htmlOut.append("<br><b>").append(travelbugs.size()).append(" TravelBugs:")
                            .append("</b><br/>\n");
            }
            for (GeocacheDetails.Travelbug bug : travelbugs) {
                htmlOut.append("<hr/>").append(bug.mName).append("<br/>\n");
                htmlOut.append("<b>id:</b> ").append(bug.mId).append("<br/>\n");
                htmlOut.append("<b>ref:</b> ").append(bug.mRef).append("<br/>\n");
            }
        }
    }

    private static void printLogs(List<GeocacheLog> logs, StringBuilder htmlOut) {
        if (logs != null && logs.size() > 0) {
            switch (logs.size()) {
                case 1:
                    htmlOut.append("<b>1 log:</b><br/>\n");
                    break;
                default:
                    htmlOut.append("<br><b>").append(logs.size()).append(" logs:</b><br/>\n");
            }

            htmlOut.append("<table>");

            for (GeocacheLog log : logs) {
                htmlOut.append("<tr><td>");
                htmlOut.append("<hr/>");
                String logImage = GeocacheLog.LOGTYPE_TO_ICON.get(log.mLogType);
                if (null == logImage) {
                    logImage = "log_writenote.gif";
                }
                htmlOut.append("<img height=\"16px\" width=\"16px\" src=\"file:///android_asset/")
                        .append(logImage).append("\" /> ");
                htmlOut.append(log.mDate).append(" ");

                htmlOut.append(" by ").append(log.mFinderName).append("<br/>\n");
                htmlOut.append(bbcodeToHTML(log.mText)).append("<br/>\n");

                htmlOut.append("</td></tr>");
            }

            htmlOut.append("</table>");

        }
    }

    private static void printAttributes(Geocache geocache, GeocacheDetails details,
            StringBuilder htmlOut) {
        htmlOut.append(geocache.getId());
        htmlOut.append("<br/>\n<b>");
        htmlOut.append(geocache.getLatitude());
        htmlOut.append(", ");
        htmlOut.append(geocache.getLongitude());
        htmlOut.append("</b><br/>\n");

        htmlOut.append(convertDecimalCoordinatesToMinutes(geocache.getLatitude(),
                geocache.getLongitude()));
        htmlOut.append("<br/>\n");

        htmlOut.append("<b>Name:</b> ").append(geocache.getName()).append("<br/>\n");

        if (!details.mOwner.equals("")) {
            htmlOut.append("<b>Placed by:</b> ").append(details.mPlacedBy).append("<br/>\n");
        }
        htmlOut.append("<b>Type:</b> ").append(geocache.getCacheType().getLabel())
                .append("<br/>\n");
        if (geocache.getContainer() != 0) {
            String container = GeocacheTypeFactory.containerFromInt(geocache.getContainer());
            htmlOut.append("<b>Size:</b> ").append(container).append("<br/>\n");
        } else {
            htmlOut.append("<b>Size:</b> Not specified<br/>\n");
        }
        htmlOut.append("<b>Difficulty / Terrain:</b> ").append(geocache.getFormattedAttributes())
                .append("<br/>\n");
        if (!details.mCreationDate.equals("")) {
            htmlOut.append("<b>Placed:</b> ").append(details.mCreationDate).append("<br/>\n");
        }
    }

    private static void printDescription(GeocacheDetails details, StringBuilder htmlOut) {
        if (!(details.mShortDescription.equals("") && details.mLongDescription.equals(""))) {
            htmlOut.append("<hr>");
        }
        if (!details.mShortDescription.equals("")) {
            htmlOut.append(bbcodeToHTML(details.mShortDescription)).append("<br/><br/>\n");
        }
        htmlOut.append(bbcodeToHTML(details.mLongDescription)).append("<br/>\n");
    }

    private static void printHint(GeocacheDetails details, StringBuilder htmlOut) {
        if (!details.mEncodedHints.equals("")) {
            htmlOut.append("<input type=\"button\" value=\"Show hint\" onclick=\"javascript:showHideHint(this, 'cacheHint');\"/><div id=\"cacheHint\" style=\"display: none;\"><b>Hint:</b> ");
            htmlOut.append("<input type=\"hidden\" id=\"showHideFlag\" value=\"0\" />");
            htmlOut.append(details.mEncodedHints).append("</div>\n");
        } else {
            htmlOut.append("<i>no hint</i>");
        }
    }

    private static void printWaypoints(List<Waypoint> waypoints, StringBuilder htmlOut,
            Geocache currentCache, Waypoint currentWP) {
        if (waypoints != null && waypoints.size() > 0) {
            htmlOut.append("<br><b>Additional waypoints:</b><br/>\n");
            String onclickGC = "onclick=\"return window.waypoint.selectGeocache('"
                    + currentCache.getId() + "');\"";
            String currentCacheStyle = currentWP == null ? "style='font-weight: bold;'" : "";
            htmlOut.append("<a " + currentCacheStyle + " " + onclickGC + ">Current geocache: "
                    + currentCache.getId() + "</a><br/>\n");
            htmlOut.append("<ul>\n");
            String currentStyle = "";
            for (Waypoint wp : waypoints) {
                htmlOut.append("<li>");
                if (currentWP != null && wp.equals(currentWP))
                    currentStyle = " style='font-weight: bold;' ";
                else
                    currentStyle = "";
                String wpUrl = "TreasureHunter://electromedica.in/location.php";// +
                // wp.getId();
                String onclickWP = "onclick=\"return window.waypoint.selectWaypoint('" + wp.getId()
                        + "');\"";
                htmlOut.append("<a " + onclickWP + " " + currentStyle + " href=\"" + wpUrl + "\">");
                htmlOut.append(wp.getName() + ": " + wp.getLatitude() + ", " + wp.getLongitude());
                htmlOut.append("</a>");

                String editOnclick = "return window.waypoint.edit('" + wp.getId() + "');";
                htmlOut.append(" (<b><a onclick=\"" + editOnclick + "\">Edit</a></b>)");

                String deleteOnclick = "return window.waypoint.deleteWp('" + wp.getId() + "');";
                htmlOut.append(" (<b><a onclick=\"" + deleteOnclick + "\">Delete</a></b>)");
                htmlOut.append("</li>");
            }
            htmlOut.append("</ul>\n");
        }
    }

    private static String convertDecimalCoordinatesToMinutes(double latitude, double longitude) {
        StringBuffer result = new StringBuffer(100);

        double absLatitude = Math.abs(latitude);
        int latDegrees = ((Double)absLatitude).intValue();

        double minutes = 60.0 * (absLatitude - latDegrees);
        result.append(((latitude < 0.0) ? "S " : "N ")).append(latDegrees);
        result.append("&deg; ").append(String.format("%.3f", minutes)).append(" ");

        double absLongitude = Math.abs(longitude);
        int lonDegrees = ((Double)absLongitude).intValue();

        minutes = 60.0 * (absLongitude - lonDegrees);
        result.append(((longitude < 0.0) ? "W " : "E ")).append(lonDegrees);
        result.append("&deg; ").append(String.format("%.3f", minutes));

        return result.toString();
    }

    @Override
    public void onDataViewChanged(GeocacheFilter filter, boolean isTabActive) {
        mGeocacheId = "";
        if (isTabActive)
            updateHtml();
    }

    private class SelectGeoobjectJSInterface {
        public boolean selectGeocache(String id) {
            final Geocache geocache = mDbFrontend.loadCacheFromId(id);
            mGuiState.getUiUpdater().post(new Runnable() {
                @Override
                public void run() {
                    mGuiState.showCompass(geocache);
                }
            });
            mError = true;
            return false;
        }

        public boolean selectWaypoint(String id) {
            final Waypoint waypoint = mDbFrontend.loadWaypointFromId(id);
            mGuiState.getUiUpdater().post(new Runnable() {
                @Override
                public void run() {
                    mGuiState.showCompass(waypoint);
                }
            });
            mError = true;
            return false;
        }

        public boolean edit(String id) {
            // Log.i("TreasureHunter", "Edit waypoint: "+id);
            Intent intent = new Intent(mActivity, EditCacheActivity.class);
            intent.putExtra(Waypoint.ID, id);
            mActivity.startActivityForResult(intent, 0);
            mError = true;
            return false;
        }

        private String mDeleteId = null;

        public boolean deleteWp(final String id) {
            // Log.i("TreasureHunter", "Delete waypoint: "+id);

            mDeleteId = id;

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(R.string.waypoint_delete_confirm).setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            mGuiState.getUiUpdater().post(new Runnable() {
                                @Override
                                public void run() {
                                    mDbFrontend.getCacheWriter().deleteWaypoint(mDeleteId);
                                    mGuiState.notifyDataViewChanged();
                                }
                            });
                            mError = true;
                            updateHtml();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return false;

        }
    }
}
