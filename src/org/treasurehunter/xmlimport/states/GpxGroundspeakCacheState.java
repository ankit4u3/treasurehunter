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

import java.util.Properties;

/**
 * Handles states under <gpx><wpt><groundspeak:cache> Used by GPX files from
 * geocaching.com
 */
public class GpxGroundspeakCacheState extends XmlState {
    public static final String TAGNAME = "groundspeak:cache";

    private final GpxWaypointState mParentState;

    private final String mUsername;

    private GeocacheData mGeocacheData;

    public GpxGroundspeakCacheState(GpxWaypointState parent, GeocacheData data, String username) {
        mParentState = parent;
        mGeocacheData = data;
        mUsername = username;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        if (name.equals("groundspeak:name")) {
            mGeocacheData.mName = text;
        } else if (name.equals("groundspeak:owner")) {
            if (text.equals(mUsername)) {
                mGeocacheData.mIsMine = true;
            }
            mGeocacheData.mDetails.mOwner = text;
        } else if (name.equals("groundspeak:encoded_hints")) {
            mGeocacheData.mDetails.mEncodedHints = text;
        } else if (name.equals("groundspeak:placed_by")) {
            mGeocacheData.mDetails.mPlacedBy = text;
        } else if (name.equals("groundspeak:container")) {
            mGeocacheData.mContainer = text;
        } else if (name.equals("groundspeak:difficulty")) {
            mGeocacheData.mDifficulty = text;
        } else if (name.equals("groundspeak:terrain")) {
            mGeocacheData.mTerrain = text;
        } else if (name.equals("groundspeak:type")) {
            mGeocacheData.mCacheType = text;
        } else if (name.equals("groundspeak:short_description")) {
            mGeocacheData.mDetails.mShortDescription = text;
        } else if (name.equals("groundspeak:long_description")) {
            mGeocacheData.mDetails.mLongDescription = text;
        }
        return this;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals(GpxGroundspeakLogsState.TAGNAME)) {
            return new GpxGroundspeakLogsState(this, mGeocacheData, mUsername);
        } else if (name.equals(GpxGroundspeakTravelbugs.TAGNAME)) {
            return new GpxGroundspeakTravelbugs(this, mGeocacheData);
        }
        return this;
    }

    @Override
    public XmlState handleEndTag(String name) {
        if (name.equals(TAGNAME))
            return mParentState;
        return this;
    }
}

class GpxGroundspeakTravelbugs extends XmlState {
    public static final String TAGNAME = "groundspeak:travelbugs";

    private final GpxGroundspeakCacheState mParentState;

    private final GeocacheData mGeocacheData;

    private GeocacheDetails.Travelbug mTravelbug = null;

    public GpxGroundspeakTravelbugs(GpxGroundspeakCacheState parentState, GeocacheData geocacheData) {
        mParentState = parentState;
        mGeocacheData = geocacheData;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals("groundspeak:travelbug")) {
            mTravelbug = new GeocacheDetails.Travelbug();
            mTravelbug.mId = attributes.getProperty("id");
            mTravelbug.mRef = attributes.getProperty("ref");
            mGeocacheData.mTravelbugs.add(mTravelbug);
        }
        return this;
    }

    @Override
    public XmlState handleEndTag(String name) {
        if (name.equals(TAGNAME))
            return mParentState;
        return this;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        if (name.equals("groundspeak:name") && mTravelbug != null) {
            mTravelbug.mName = text;
        }
        return this;
    }
}
