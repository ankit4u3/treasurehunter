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

package org.treasurehunter;

import java.util.ArrayList;
import java.util.List;

public class SourceFactory {

    private List<Source> mSources;

    public SourceFactory() {
        mSources = new ArrayList<Source>(10);
        mSources.add(Source.MY_LOCATION);
        mSources.add(Source.WEB_URL);
        mSources.add(Source.BCACHING);
    }

    public Source locFile(String filename) {
        for (Source source : mSources)
            if (source.mType == Source.LOC && source.mFilename.equals(filename))
                return source;
        Source newSource = new Source(Source.LOC, "loc/" + filename, filename);
        mSources.add(newSource);
        return newSource;
    }

    public Source gpxFile(String filename) {
        for (Source source : mSources)
            if (source.mType == Source.GPX && source.mFilename.equals(filename))
                return source;
        Source newSource = new Source(Source.GPX, "gpx/" + filename, filename);
        mSources.add(newSource);
        return newSource;
    }

    public Source fromString(String sourceString) {
        for (Source source : mSources)
            if (source.mUnique.equals(sourceString))
                return source;
        if (sourceString.startsWith("gpx/"))
            return gpxFile(sourceString.substring(4));
        if (sourceString.startsWith("loc/"))
            return locFile(sourceString.substring(4));
        return null; // Error
    }

    public Source fromFile(String filename) {
        if (filename.toLowerCase().endsWith(".loc"))
            return locFile(filename);
        // if (filename.endsWith(".gpx")) //There's no other option
        return gpxFile(filename);
    }
}
