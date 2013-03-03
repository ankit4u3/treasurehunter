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
import org.treasurehunter.xmlimport.IProcessStatus;

import java.util.Properties;

/**
 * Handles the top-level elements in a XML stream, either calling a GPX or LOC
 * sub-state.
 */
public class XmlTopLevelState extends XmlState {
    private final IProcessStatus mProcessStatus;

    private final Source mSource;

    private final AbortFlag mAbortFlag;

    private final String mUsername;

    public XmlTopLevelState(IProcessStatus processStatus, Source source, AbortFlag abortFlag,
            String username) {
        mProcessStatus = processStatus;
        mSource = source;
        mAbortFlag = abortFlag;
        mUsername = username;
    }

    @Override
    public XmlState handleStartTag(String name, Properties attributes) {
        if (name.equals("gpx"))
            return new GpxState(mProcessStatus, this, mSource, mAbortFlag, mUsername);
        if (name.equals("loc")) {
            mProcessStatus.onStartLoadingSource(mSource);
            return new LocState(mProcessStatus, this);
        }
        return this;
    }
}
