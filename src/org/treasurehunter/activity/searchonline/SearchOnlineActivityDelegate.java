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

import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SearchOnlineActivityDelegate {

    private final GeoFixProvider mGeoFixProvider;

    private final WebView mWebView;

    public SearchOnlineActivityDelegate(WebView webView, GeoFixProvider geoFixProvider) {
        mGeoFixProvider = geoFixProvider;
        mWebView = webView;
    }

    public void configureWebView(JsInterface jsInterface) {
        mWebView.loadUrl("file:///android_asset/search.html");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.setBackgroundColor(Color.BLACK);
        mWebView.addJavascriptInterface(jsInterface, "gb");
    }

    public void onPause() {
        mGeoFixProvider.stopUpdates();
    }

    public void onResume() {
        mGeoFixProvider.startUpdates();
    }
}
