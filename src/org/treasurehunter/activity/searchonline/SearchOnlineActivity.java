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

package org.treasurehunter.activity.searchonline;

import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.GeoFixProviderDI;
import org.treasurehunter.R;
import org.treasurehunter.activity.searchonline.JsInterface.JsInterfaceHelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class SearchOnlineActivity extends Activity {

    private SearchOnlineActivityDelegate mSearchOnlineActivityDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TreasureHunter", "SearchOnlineActivity onCreate");

        setContentView(R.layout.search);

        final GeoFixProvider geoFixProvider = GeoFixProviderDI.create(this);

        mSearchOnlineActivityDelegate = new SearchOnlineActivityDelegate(
                ((WebView)findViewById(R.id.help_contents)), geoFixProvider);

        final JsInterfaceHelper jsInterfaceHelper = new JsInterfaceHelper(this);
        final JsInterface jsInterface = new JsInterface(geoFixProvider, jsInterfaceHelper);

        mSearchOnlineActivityDelegate.configureWebView(jsInterface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TreasureHunter", "SearchOnlineActivity onResume");

        mSearchOnlineActivityDelegate.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TreasureHunter", "SearchOnlineActivity onPause");

        mSearchOnlineActivityDelegate.onPause();
    }
}
