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

package org.treasurehunter.xmlimport;

import java.util.HashMap;
import java.util.Map;

/** Data structure containing the details data absent from the Geocache class. */
public class GeocacheDetails {

    public static class GeocacheLog {
        public String mDate;

        /** Assign one of the constants in this class */
        public int mLogType;

        public String mFinderName;

        public String mText;

        public boolean mIsTextEncoded;

        /** Id unique for every Log */
        public long mId; //

        public static Integer FOUNDIT_LOG_TYPE = 1;

        public static Integer DNF_LOG_TYPE = 2;

        public static Integer WRITENOTE_LOG_TYPE = 3;

        public static Integer ARCHIVE_LOG_TYPE = 4;

        public static Integer NEEDSARCHIVED_LOG_TYPE = 5;

        public static Integer REVIEWERNOTE_LOG_TYPE = 6;

        public static Integer ENABLE_LOG_TYPE = 7;

        public static Integer NEEDSMAINT_LOG_TYPE = 8;

        public static Integer OWNERMAINT_LOG_TYPE = 9;

        public static Integer UPDATECOORDS_LOG_TYPE = 10;

        public static Integer DISABLE_LOG_TYPE = 11;

        public static Integer PUBLISH_LOG_TYPE = 12;

        public static Integer WILLATTEND_LOG_TYPE = 13;

        public static Integer ATTENDED_LOG_TYPE = 14;

        public static Integer OTHER_LOG_TYPE = 15;

        public static Integer GSAKNOTE_LOG_TYPE = 16;

        public static Integer USERNOTE_LOG_TYPE = 17;

        public static Map<Integer, String> LOGTYPE_TO_STRING = initLogTypeMap();

        public static Map<Integer, String> LOGTYPE_TO_ICON = initLogImageMap();

        public static Map<String, Integer> STRING_TO_LOGTYPE = initStringToLogTypeMap();

        public static Map<String, String> SMILEY_TO_ICON = initSmileyIconMap();

        private static Map<Integer, String> initLogTypeMap() {
            Map<Integer, String> logTypeMap = new HashMap<Integer, String>();
            logTypeMap.put(FOUNDIT_LOG_TYPE, "Found it");
            logTypeMap.put(DNF_LOG_TYPE, "Didn't find it");
            logTypeMap.put(WRITENOTE_LOG_TYPE, "Write note");
            logTypeMap.put(ARCHIVE_LOG_TYPE, "Archive");
            logTypeMap.put(NEEDSARCHIVED_LOG_TYPE, "Needs Archived");
            logTypeMap.put(REVIEWERNOTE_LOG_TYPE, "Reviewer note");
            logTypeMap.put(ENABLE_LOG_TYPE, "Enable Listing");
            logTypeMap.put(NEEDSMAINT_LOG_TYPE, "Needs Maintenance");
            logTypeMap.put(OWNERMAINT_LOG_TYPE, "Owner Maintenance");
            logTypeMap.put(UPDATECOORDS_LOG_TYPE, "Update Coordinates");
            logTypeMap.put(DISABLE_LOG_TYPE, "Temporarily Disable Listing");
            logTypeMap.put(PUBLISH_LOG_TYPE, "Publish Listing");
            logTypeMap.put(WILLATTEND_LOG_TYPE, "Will Attend");
            logTypeMap.put(ATTENDED_LOG_TYPE, "Attended");
            logTypeMap.put(OTHER_LOG_TYPE, "Other");
            logTypeMap.put(GSAKNOTE_LOG_TYPE, "GSAK Note");
            logTypeMap.put(USERNOTE_LOG_TYPE, "User Note");
            return logTypeMap;
        }

        private static Map<Integer, String> initLogImageMap() {
            Map<Integer, String> logImageMap = new HashMap<Integer, String>();
            logImageMap.put(FOUNDIT_LOG_TYPE, "log_foundit.gif");
            logImageMap.put(DNF_LOG_TYPE, "log_didntfindit.gif");
            logImageMap.put(WRITENOTE_LOG_TYPE, "log_writenote.gif");
            logImageMap.put(ARCHIVE_LOG_TYPE, "log_archive.gif");
            logImageMap.put(NEEDSARCHIVED_LOG_TYPE, "log_needsarchived.gif");
            logImageMap.put(REVIEWERNOTE_LOG_TYPE, "log_reviewernote.gif");
            logImageMap.put(ENABLE_LOG_TYPE, "log_enabled.gif");
            logImageMap.put(NEEDSMAINT_LOG_TYPE, "log_needsmaint.gif");
            logImageMap.put(OWNERMAINT_LOG_TYPE, "log_ownermaint.gif");
            logImageMap.put(UPDATECOORDS_LOG_TYPE, "log_updatecoord.gif");
            logImageMap.put(DISABLE_LOG_TYPE, "log_disabled.gif");
            logImageMap.put(PUBLISH_LOG_TYPE, "log_published.gif");
            logImageMap.put(WILLATTEND_LOG_TYPE, "log_willattend.gif");
            logImageMap.put(ATTENDED_LOG_TYPE, "log_attended.gif");
            logImageMap.put(OTHER_LOG_TYPE, "log_writenote.gif");
            logImageMap.put(GSAKNOTE_LOG_TYPE, "log_gsak.gif");
            logImageMap.put(USERNOTE_LOG_TYPE, "log_writenote.gif");
            return logImageMap;
        }

        private static Map<String, Integer> initStringToLogTypeMap() {
            Map<String, Integer> logTypeMap = new HashMap<String, Integer>();
            logTypeMap.put("Found it", FOUNDIT_LOG_TYPE);
            logTypeMap.put("Didn't find it", DNF_LOG_TYPE);
            logTypeMap.put("Write note", WRITENOTE_LOG_TYPE);
            logTypeMap.put("Archive", ARCHIVE_LOG_TYPE);
            logTypeMap.put("Needs Archived", NEEDSARCHIVED_LOG_TYPE);
            logTypeMap.put("Reviewer note", REVIEWERNOTE_LOG_TYPE);
            logTypeMap.put("Enable Listing", ENABLE_LOG_TYPE);
            logTypeMap.put("Needs Maintenance", NEEDSMAINT_LOG_TYPE);
            logTypeMap.put("Owner Maintenance", OWNERMAINT_LOG_TYPE);
            logTypeMap.put("Update Coordinates", UPDATECOORDS_LOG_TYPE);
            logTypeMap.put("Temporarily Disable Listing", DISABLE_LOG_TYPE);
            logTypeMap.put("Publish Listing", PUBLISH_LOG_TYPE);
            logTypeMap.put("Will Attend", WILLATTEND_LOG_TYPE);
            logTypeMap.put("Attended", ATTENDED_LOG_TYPE);
            logTypeMap.put("Other", OTHER_LOG_TYPE);
            logTypeMap.put("GSAK Note", GSAKNOTE_LOG_TYPE);
            logTypeMap.put("User Note", USERNOTE_LOG_TYPE);
            return logTypeMap;
        }
    }

    private static Map<String, String> initSmileyIconMap() {
        Map<String, String> smileyIconMap = new HashMap<String, String>();

        smileyIconMap.put(":)", "smile-smile.png");
        smileyIconMap.put(":D", "smile-big-smile.png");
        smileyIconMap.put("8D", "smile-cool.png");
        smileyIconMap.put(":I", "smile-blush.png");
        smileyIconMap.put(":P", "smile-tongue.png");
        smileyIconMap.put("}:)", "smile-evil.png");
        smileyIconMap.put(";)", "smile-wink.png");
        smileyIconMap.put(":o)", "smile-clown.png");
        smileyIconMap.put("B)", "smile-black-eye.png");
        smileyIconMap.put("8", "smile-eightball.png");
        smileyIconMap.put(":(", "smile-frown.png");
        smileyIconMap.put("8)", "smile-shy.png");
        smileyIconMap.put(":O", "smile-shocked.png");
        smileyIconMap.put(":(!", "smile-angry.png");
        smileyIconMap.put("xx(", "smile-dead.png");
        smileyIconMap.put("|)", "smile-sleepy.png");
        smileyIconMap.put(":X", "smile-kisses.png");
        smileyIconMap.put("^", "smile-approve.png");
        smileyIconMap.put("V", "smile-disapprove.png");
        smileyIconMap.put("?", "smile-question.png");

        return smileyIconMap;
    }

    public static class Travelbug {
        public String mId;

        public String mRef;

        public String mName;
    }

    public String mOwner;

    public String mPlacedBy;

    public String mShortDescription = "";

    public String mLongDescription = "";

    public String mEncodedHints = "";

    /** published / created date for cache / waypoint */
    public String mCreationDate;
}
