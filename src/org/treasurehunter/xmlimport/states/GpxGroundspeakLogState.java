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

import org.treasurehunter.Tags;
import org.treasurehunter.xmlimport.GeocacheData;
import org.treasurehunter.xmlimport.GeocacheDetails;

import java.util.Properties;

public class GpxGroundspeakLogState extends XmlState {
    public static final String TAGNAME = "groundspeak:log";

    private final GpxGroundspeakLogsState mParentState;

    private GeocacheData mGeocacheData;

    private final String mUsername;

    private String mLastLogType = "";

    private GeocacheDetails.GeocacheLog mDetailLog;

    public GpxGroundspeakLogState(GpxGroundspeakLogsState parent, GeocacheData geocacheData,
            String username, String logId) {
        mParentState = parent;
        mGeocacheData = geocacheData;
        mUsername = username;
        mDetailLog = new GeocacheDetails.GeocacheLog();
        mDetailLog.mId = Long.parseLong(logId);
    }

    @Override
    public XmlState handleEndTag(String name) {
        if ((-2 == mDetailLog.mId) && ("GSAK".equalsIgnoreCase(mDetailLog.mFinderName))
                && (GeocacheDetails.GeocacheLog.OTHER_LOG_TYPE == mDetailLog.mLogType)) {
            mDetailLog.mLogType = GeocacheDetails.GeocacheLog.GSAKNOTE_LOG_TYPE;
            mGeocacheData.mUserNotes.add(mDetailLog);
        } else {
            mGeocacheData.mLogs.add(mDetailLog);
        }
        if (name.equals(TAGNAME)) {
            return mParentState;
        }
        mDetailLog = null;
        return this;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        if (name.equals("groundspeak:finder")) {
            if (mGeocacheData.mMyFindTag == Tags.NULL && text.equals(mUsername)) {
                if (mLastLogType.equals("Found it")) {
                    mGeocacheData.mMyFindTag = Tags.FOUND;
                } else if (mLastLogType.equals("Didn't find it")) {
                    mGeocacheData.mMyFindTag = Tags.DNF;
                }
            }
            mDetailLog.mFinderName = text;
        } else if (name.equals("groundspeak:type")) {
            mLastLogType = text;
            Integer logType = GeocacheDetails.GeocacheLog.STRING_TO_LOGTYPE.get(text);
            if (null == logType) {
                logType = -1;
            }
            mDetailLog.mLogType = logType;
        } else if (name.equals("groundspeak:text")) {
            mDetailLog.mText = text;
            String encoded = attributes.getProperty("encoded");
            if (null != encoded) {
                mDetailLog.mIsTextEncoded = encoded.equalsIgnoreCase("true");
            } else {
                mDetailLog.mIsTextEncoded = false;
            }
        } else if (name.equals("groundspeak:date")) {
            mDetailLog.mDate = text.substring(0, 10);
        }

        return this;
    }
}
