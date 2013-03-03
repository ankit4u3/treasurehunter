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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/** * CURRENTLY NOT USED *
 * Can reduce the time to parse XML by
 * avoiding running XmlFiniteStateMachine.parseProperties() */
class FsmInput {
    public static final int IO_ERROR = -2;
    public static final int PARSER_ERROR = -3;
    private final XmlPullParser mXpp;
    private boolean mNextAlreadyCalled = false;
    private int mEventType;
    public FsmInput(XmlPullParser xpp) {
        mXpp = xpp;
        try {
            mEventType = xpp.getEventType();
        } catch (XmlPullParserException e) {
            mEventType = PARSER_ERROR;
        }
    }

    /** After calling this, it's no longer possible to call getAttribute() */
    public String nextText() {
        if (!mNextAlreadyCalled) {
            mNextAlreadyCalled = true;
            try {
                mEventType = mXpp.next();
            } catch (XmlPullParserException e) {
                mEventType = PARSER_ERROR;
            } catch (IOException e) {
                mEventType = IO_ERROR;
            }
        }
        if (mEventType == XmlPullParser.TEXT)
            return mXpp.getText();
        return "";
    }

    /** Advances to the next tag unless getText() was called.
     * @return The type of the next event
     */
    public int next() throws XmlPullParserException, IOException {
        if (mNextAlreadyCalled) {
            mNextAlreadyCalled = false;
        } else {
            mEventType = mXpp.next();
        }
        return mEventType;
    }
    
    public int getEventType() throws XmlPullParserException {
        return mEventType;
    }
    
    public String getAttribute(String name) {
        int count = mXpp.getAttributeCount();
        for (int i = 0; i < count; i++)
            if (mXpp.getAttributeName(i).equals(name))
                return mXpp.getAttributeValue(i);
        return null;
    }
}

