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

public class Source {
    static final int GPX = 0;

    static final int LOC = 3;

    public static final Source MY_LOCATION = new Source(1, "mylocation");

    public static final Source WEB_URL = new Source(2, "intent");

    public static final Source BCACHING = new Source(4, "bcaching");

    /** The filename without path, or "" if the source wasn't a specific file */
    public final String mFilename;

    public final int mType;

    /** Used to identify the source in the database */
    public final String mUnique;

    public Source(int type, String unique) {
        mType = type;
        mUnique = unique;
        mFilename = "";
    }

    public Source(int type, String unique, String filename) {
        mType = type;
        mUnique = unique;
        mFilename = filename;
    }

    @Override
    public String toString() {
        return mUnique;
    }

    /**
     * @return The filename without path, or "" if the source wasn't a specific
     *         file
     */
    public String getFilename() {
        return mFilename;
    }

    public boolean isFile() {
        return mType == GPX || mType == LOC;
    }

    public boolean isGpx() {
        return mType == GPX;
    }

    public boolean isLoc() {
        return mType == LOC;
    }
}
