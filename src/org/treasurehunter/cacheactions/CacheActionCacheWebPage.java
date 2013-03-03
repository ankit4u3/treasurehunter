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

package org.treasurehunter.cacheactions;

import org.treasurehunter.Geocache;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

public class CacheActionCacheWebPage extends StaticLabelCache implements CacheAction {
    private final Activity mActivity;

    private final Resources mResources;

    public CacheActionCacheWebPage(Activity activity, Resources resources) {
        super(resources, R.string.cache_page);
        mActivity = activity;
        mResources = resources;
    }

    @Override
    public void act(Geocache geocache, Waypoint waypoint) {
        CharSequence id = geocache.getId();
        if (id.length() <= 2)
            return;
        CharSequence shortId = id.subSequence(2, id.length());

        String uri = String.format(mResources.getStringArray(R.array.cache_page_url)[geocache
                .getContentProvider().toInt()], shortId);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

        mActivity.startActivity(intent);
    }
}
