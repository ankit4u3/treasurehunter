
package org.treasurehunter.cacheactions;

import org.treasurehunter.Geocache;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.cachelist.CacheListUpdater;

import android.app.Activity;

public class CacheActionSetCenterpoint implements CacheAction {
    private final Activity mActivity;

    private final CacheListUpdater mCacheListUpdater;

    public CacheActionSetCenterpoint(Activity activity, CacheListUpdater cacheListUpdater) {
        mActivity = activity;
        mCacheListUpdater = cacheListUpdater;
    }

    @Override
    public void act(Geocache geocache, Waypoint waypoint) {
        mCacheListUpdater.setCacheAsCenter(geocache);
    }

    @Override
    public String getLabel(Geocache geocache) {
        return mActivity.getResources().getString(R.string.set_cache_as_centerpoint);
    }

}
