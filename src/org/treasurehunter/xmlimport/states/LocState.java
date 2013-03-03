
package org.treasurehunter.xmlimport.states;

import org.treasurehunter.Clock;
import org.treasurehunter.xmlimport.GeocacheData;
import org.treasurehunter.xmlimport.IProcessStatus;

import java.util.Properties;

/**
 * Handles xml under <loc>
 */
public class LocState extends XmlState {

    public class LocWaypointState extends XmlState {
        @Override
        public XmlState handleTextElement(String name, Properties attributes, String text) {
            if (name.equals("name")) {
                // For some reason the id can contain regular text
                mGeocacheData.mId = attributes.getProperty("id").replaceAll("\'", "");
                String trimmed = text.trim();
                if (trimmed.equals("")) {
                    mGeocacheData.mName = mGeocacheData.mId;
                } else {
                    mGeocacheData.mName = trimmed;
                }
            } else if (name.equals("coord")) {
                mGeocacheData.mLatitude = attributes.getProperty("lat");
                mGeocacheData.mLongitude = attributes.getProperty("lon");
            } else if (name.equals("type")) {
                mGeocacheData.mCacheType = text;
            } else if (name.equals("link")) {
                String linkType = attributes.getProperty("text");
                if (linkType != null && linkType.equals("Cache Details")) {
                    // TODO: store URL somewhere
                }
            }
            return this;
        }

        @Override
        public XmlState handleEndTag(String name) {
            if (name.equals("waypoint")) {
                mProcessStatus.onParsedGeocache(mGeocacheData);
                return LocState.this; // Return to parent state
            }
            return this;
        }
    }

    private final LocWaypointState mLocWaypointState = new LocWaypointState();

    private final IProcessStatus mProcessStatus;

    private final XmlState mParent;

    GeocacheData mGeocacheData = new GeocacheData();

    public LocState(IProcessStatus processStatus, XmlState parent) {
        mProcessStatus = processStatus;
        mParent = parent;
    }

    @Override
    public XmlState handleEndTag(String name) {
        if (name.equals("loc"))
            return mParent;
        return this;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals("waypoint")) {
            // Reset all fields that might be set for loc waypoints
            mGeocacheData.mLastModified = Clock.getCurrentStringTime();
            mGeocacheData.mId = null;
            mGeocacheData.mName = null;
            mGeocacheData.mLatitude = null;
            mGeocacheData.mLongitude = null;
            return mLocWaypointState;
        }
        return this;
    }
}
