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

import java.util.Properties;

/**
 * Handles states under <gpx><wpt><groundspeak:cache><groundspeak:logs> Used by
 * GPX files from geocaching.com
 */
public class GpxGroundspeakLogsState extends XmlState {
    public static final String TAGNAME = "groundspeak:logs";

    private final GpxGroundspeakCacheState mParentState;

    private GeocacheData mGeocacheData;

    private final String mUsername;

    public GpxGroundspeakLogsState(GpxGroundspeakCacheState parent, GeocacheData geocacheData,
            String username) {
        mParentState = parent;
        mGeocacheData = geocacheData;
        mUsername = username;
    }

    @Override
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        return this;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals(GpxGroundspeakLogState.TAGNAME)) {
            String logId = attributes.getProperty("id");
            return new GpxGroundspeakLogState(this, mGeocacheData, mUsername, logId);
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
