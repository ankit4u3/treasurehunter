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

package org.treasurehunter.activity.cachelist;

public interface BearingFormatter {

    public abstract String formatBearing(float absBearing, float myHeading);

    public class Absolute implements BearingFormatter {
        private static final String[] LETTERS = {
                "N", "NE", "E", "SE", "S", "SW", "W", "NW"
        };

        @Override
        public String formatBearing(float absBearing, float myHeading) {
            return LETTERS[((((int)(absBearing) + 22 + 720) % 360) / 45)];
        }
    }

    public class Relative implements BearingFormatter {
        private static final String[] ARROWS = {
                "^", ">", "v", "<",
        };

        @Override
        public String formatBearing(float absBearing, float myHeading) {
            return ARROWS[((((int)(absBearing - myHeading) + 45 + 720) % 360) / 90)];
        }
    }

}
