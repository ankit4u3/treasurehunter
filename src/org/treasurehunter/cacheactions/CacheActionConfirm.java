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
import org.treasurehunter.Waypoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/** Adds a confirm dialog before carrying out the nested CacheAction */
public class CacheActionConfirm implements CacheAction {

    private final Activity mActivity;

    private final CacheAction mCacheAction;

    // May Builder only be used once?
    private final AlertDialog.Builder mBuilder;

    private final String mTitle;

    private final String mBody;

    public CacheActionConfirm(Activity activity, AlertDialog.Builder builder,
            CacheAction cacheAction, String title, String body) {
        mActivity = activity;
        mBuilder = builder;
        mCacheAction = cacheAction;
        mTitle = title;
        mBody = body;
    }

    private AlertDialog buildAlertDialog(final Geocache cache, final Waypoint waypoint) {
        final String title = String.format(mTitle, cache.getId());
        final String message = String.format(mBody, cache.getId(), cache.getName());
        mBuilder.setTitle(title);
        mBuilder.setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mCacheAction.act(cache, waypoint);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.setOwnerActivity(mActivity);
        return alertDialog;
    }

    @Override
    public void act(Geocache cache, Waypoint waypoint) {
        AlertDialog alertDialog = buildAlertDialog(cache, waypoint);
        alertDialog.show();
    }

    @Override
    public String getLabel(Geocache geocache) {
        return mCacheAction.getLabel(geocache);
    }

}
