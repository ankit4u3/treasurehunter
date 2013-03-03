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

import org.treasurehunter.xmlimport.AbortFlag;
import org.treasurehunter.xmlimport.GeocacheData;
import org.treasurehunter.xmlimport.IProcessStatus;

import java.util.Properties;

/**
 * Handles states under <gpx><wpt> and <gpx><waypoint>
 */
public class GpxWaypointState extends XmlState {
    private final XmlState mParentState;

    private GeocacheData mGeocacheData;

    private final IProcessStatus mProcessStatus;

    private final AbortFlag mAbortFlag;

    private final String mUsername;

    public GpxWaypointState(XmlState parentState, GeocacheData geocacheData,
            IProcessStatus processStatus, AbortFlag abortFlag, String username) {
        mParentState = parentState;
        mGeocacheData = geocacheData;
        mProcessStatus = processStatus;
        mAbortFlag = abortFlag;
        mUsername = username;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        if (name.equals("desc")) {
            mGeocacheData.mName = text;
        } else if (name.equals("name")) {
            if (text.equals("")) {
                // For some reason the id can contain regular text
                mGeocacheData.mId = attributes.getProperty("id").replaceAll("\'", "");
            } else {
                // For some reason the id can contain regular text
                mGeocacheData.mId = text.replaceAll("\'", "");
            }
        } else if (name.equals("type")) {
            mGeocacheData.mCacheType = text;
        } else if (name.equals("sym")) {
            mGeocacheData.mSym = text;
        } else if (name.equals("cmt")) {
        } else if (name.equals("container")) {
            // Uncertain if this tag ever exists. Doesn't do any harm to check
            // for it...
            mGeocacheData.mContainer = text;
        } else if (name.equals("coord")) {
            mGeocacheData.mLatitude = attributes.getProperty("lat", null);
            mGeocacheData.mLongitude = attributes.getProperty("lon", null);
        } else if (name.equals("time")) {
            mGeocacheData.mDetails.mCreationDate = text;
        }
        return this;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals(GpxGroundspeakCacheState.TAGNAME)) {
            mGeocacheData.mAvailable = attributes.getProperty("available");
            mGeocacheData.mArchived = attributes.getProperty("archived");
            return new GpxGroundspeakCacheState(this, mGeocacheData, mUsername);
        } else if (name.equals("geocache")) {
            return new GpxWptGeocacheState(this, mGeocacheData, mUsername);
        }

        return this;
    }

    @Override
    public XmlState handleEndTag(String name) {
        if (name.equals("wpt") || name.equals("waypoint")) {
            if (mAbortFlag.isAborted())
                return new DoneState();
            mProcessStatus.onParsedGeocache(mGeocacheData);
            return mParentState;
        }
        return this;
    }
}
