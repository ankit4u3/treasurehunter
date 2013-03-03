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

import org.treasurehunter.Source;
import org.treasurehunter.xmlimport.states.XmlState;
import org.treasurehunter.xmlimport.states.XmlTopLevelState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Parses an input stream into XML tags and feeds this into a Finite State
 * Machine until it ends up in a DoneState or the stream reaches the end.
 */
public class XmlFiniteStateMachine {

    public static void importFromReader(Source source, Reader reader, IProcessStatus processStatus,
            AbortFlag abortFlag, String username) throws XmlPullParserException, IOException {
        Log.d("TreasureHunter", "importFromReader " + source.toString());

        XmlFiniteStateMachine machine = new XmlFiniteStateMachine();
        XmlTopLevelState state = new XmlTopLevelState(processStatus, source, abortFlag, username);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(reader);
        machine.parse(xpp, state);
    }

    public void parse(XmlPullParser xpp, XmlState startState) throws XmlPullParserException,
            IOException {

        XmlState state = startState;
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {

            } else if (eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                // Log.d("TreasureHunter", "StartTag " + name);
                Properties attributes = parseProperties(xpp);
                eventType = xpp.next();

                if (eventType == XmlPullParser.END_TAG) {
                    // The form <tag/> or <tag></tag> was found
                    state = state.handleTextElement(name, attributes, "");

                } else if (eventType == XmlPullParser.TEXT) {
                    String text = xpp.getText();
                    eventType = xpp.next();
                    if (eventType == XmlPullParser.END_TAG) {
                        // The form "<tag>text</tag>" was found
                        state = state.handleTextElement(name, attributes, text);
                    } else {
                        // Found "<tag>text<tag2>" - Ignore text
                        state = state.handleStartTag(name, attributes);
                        // <tag2> handled next iteration of the loop
                    }
                } else {
                    // Found <tag><tag2>
                    state = state.handleStartTag(name, attributes);
                }

            } else if (eventType == XmlPullParser.END_TAG) {
                state = state.handleEndTag(xpp.getName());
            }

            if (state == null) {
                Log.e("TreasureHunter", "Error parsing XML");
                throw new XmlPullParserException("XML structure not recognized");
            }

            if (state.isDone()) {
                Log.d("TreasureHunter", "DoneState reported");
                break;
            }

            // next() already called for START_TAG
            if (eventType != XmlPullParser.START_TAG)
                eventType = xpp.next();
        }
    }

    private static Properties EmptyProperties = new Properties();

    private static Properties StaticProperties = new Properties();

    private static Properties parseProperties(XmlPullParser xpp) {
        int count = xpp.getAttributeCount();
        if (count == 0)
            return EmptyProperties;
        StaticProperties.clear();
        for (int ix = 0; ix < count; ix++) {
            String name = xpp.getAttributeName(ix);
            String value = xpp.getAttributeValue(ix);
            StaticProperties.setProperty(name, value);
        }
        return StaticProperties;
    }
}
