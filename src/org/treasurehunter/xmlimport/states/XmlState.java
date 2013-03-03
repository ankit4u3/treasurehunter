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

import java.util.Properties;

/**
 * Base class for all FSM states. The default behavior is to stay with the
 * current state.
 */
public class XmlState {
    /**
     * Structures on the form <tag>..text..</tag> or <tag></tag> or <tag/> are
     * sent to this method.
     */
    public XmlState handleTextElement(String name, Properties attributes, String text) {
        return this;
    }

    /**
     * When there's a sub-structure under a tag, this method is invoked to
     * handle the start tag.
     */
    public XmlState handleStartTag(String name, Properties attributes) {
        return this;
    }

    /**
     * If a start tag previously invoked handleStartTag, this method will be
     * invoked for the corresponding end tag.
     */
    public XmlState handleEndTag(String name) {
        return this;
    }

    public boolean isDone() {
        return false;
    }
}

class DoneState extends XmlState {
    @Override
    public boolean isDone() {
        return true;
    }
}
