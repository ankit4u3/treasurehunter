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

package org.treasurehunter.xmlimport.states;

import org.treasurehunter.xmlimport.GeocacheData;
import org.treasurehunter.xmlimport.GeocacheDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Handles states under <gpx><wpt><geocache> Used by geocaching.com.au
 */
public class GpxWptGeocacheState extends XmlState {
    protected static Map<String, Integer> STRING_TO_LOGTYPE = initStringToLogTypeMap();

    private static Map<String, Integer> initStringToLogTypeMap() {
        Map<String, Integer> logTypeMap = new HashMap<String, Integer>();
        logTypeMap.put("Found", 1);
        logTypeMap.put("Not Found", 2);
        logTypeMap.put("Note", 3);
        logTypeMap.put("Other", 3);
        logTypeMap.put("Write note", 3); // Used by GSAK
        return logTypeMap;
    }

    class GpxGeocacheLogsState extends XmlState {

        private GeocacheDetails.GeocacheLog mLogEntry;

        @Override
        public XmlState handleStartTag(String name, Properties attributes) {
            if (name.equals("log")) {
                mLogEntry = new GeocacheDetails.GeocacheLog();
                mLogEntry.mId = Long.valueOf(attributes.getProperty("id"));
            }
            return this;
        }

        @Override
        public XmlState handleTextElement(String name, Properties attributes, String text) {
            if (mLogEntry == null)
                return this; // Some sort of syntax error in file

            if (name.equals("geocacher")) {
                mLogEntry.mFinderName = text;
            } else if (name.equals("type")) {
                Integer logType = STRING_TO_LOGTYPE.get(text);
                // 3 = write note
                mLogEntry.mLogType = (logType == null ? 3 : logType);
            } else if (name.equals("text")) {
                mLogEntry.mText = text;
            } else if (name.equals("time")) {
                mLogEntry.mDate = text;
            }
            return this;
        }

        @Override
        public XmlState handleEndTag(String name) {
            if (name.equals("log")) {
                if (mLogEntry.mId == -2) {
                    mGeocacheData.mUserNotes.add(mLogEntry);
                } else {
                    mGeocacheData.mLogs.add(mLogEntry);
                }
                mLogEntry = null;
            } else if (name.equals("logs")) {
                return GpxWptGeocacheState.this;
            }
            return this;
        }
    }

    private final GpxWaypointState mParentState;

    private final String mUsername;

    private GeocacheData mGeocacheData;

    public GpxWptGeocacheState(GpxWaypointState parentState, GeocacheData data, String username) {
        mParentState = parentState;
        mGeocacheData = data;
        mUsername = username;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        if (name.equals("owner")) {
            mGeocacheData.mDetails.mOwner = text;
            if (text.equals(mUsername)) {
                mGeocacheData.mIsMine = true;
            }
        } else if (name.equals("name")) {
            mGeocacheData.mName = text;
        } else if (name.equals("container")) {
            mGeocacheData.mContainer = text;
        } else if (name.equals("difficulty")) {
            mGeocacheData.mDifficulty = text;
        } else if (name.equals("terrain")) {
            mGeocacheData.mTerrain = text;
        } else if (name.equals("type")) {
            mGeocacheData.mCacheType = text;
        } else if (name.equals("hints")) {
            mGeocacheData.mDetails.mEncodedHints = text;
        } else if (name.equals("summary")) {
            mGeocacheData.mDetails.mShortDescription = text;
        } else if (name.equals("description")) {
            mGeocacheData.mDetails.mLongDescription = text;
        }
        return this;
    }

    private GpxGeocacheLogsState mGpxGeocacheLogsState = new GpxGeocacheLogsState();

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals("logs")) {
            return mGpxGeocacheLogsState;
        }
        return this;
    }

    @Override
    public XmlState handleEndTag(String name) {
        if (name.equals("geocache"))
            return mParentState;
        return this;
    }
}
