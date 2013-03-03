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

import org.treasurehunter.Source;
import org.treasurehunter.xmlimport.AbortFlag;
import org.treasurehunter.xmlimport.GeocacheData;
import org.treasurehunter.xmlimport.IProcessStatus;

import java.util.Properties;

/** Handles states under <gpx> */
public class GpxState extends XmlState {
    private final IProcessStatus mProcessStatus;

    private final XmlState mParent;

    private final AbortFlag mAbortFlag;

    private final Source mSource;

    private final String mUsername;

    private String mSqlDate = "2000-01-01T12:00:00"; // Default

    public GpxState(IProcessStatus processStatus, XmlState parent, Source source,
            AbortFlag abortFlag, String username) {
        mProcessStatus = processStatus;
        mParent = parent;
        mSource = source;
        mAbortFlag = abortFlag;
        mUsername = username;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        if (name.equals("time")) {
            mSqlDate = isoTimeToSql(text);
            if (mSource.isFile()) {
                if (mProcessStatus.isFileAlreadyLoaded(mSource, mSqlDate)) {
                    return new DoneState();
                }
            }
            mProcessStatus.onStartLoadingSource(mSource);
        }
        return this;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals("wpt") || name.equals("waypoint")) {
            GeocacheData data = new GeocacheData();
            data.mLastModified = mSqlDate;
            data.mLatitude = attributes.getProperty("lat", null);
            data.mLongitude = attributes.getProperty("lon", null);
            return new GpxWaypointState(this, data, mProcessStatus, mAbortFlag, mUsername);
        }
        // Don't care about <name>
        return this;
    }

    @Override
    public XmlState handleEndTag(String name) {
        if (name.equals("gpx")) {
            mProcessStatus.onFinishedLoadingSource(mSqlDate);
            return mParent;
        }
        return this;
    }

    private static String isoTimeToSql(String gpxTime) {
        return gpxTime.substring(0, 10) + " " + gpxTime.substring(11, 19);
    }
}
