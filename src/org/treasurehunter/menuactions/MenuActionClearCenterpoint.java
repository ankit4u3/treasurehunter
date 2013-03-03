
package org.treasurehunter.menuactions;

import org.treasurehunter.R;
import org.treasurehunter.activity.cachelist.CacheListUpdater;

import android.app.Activity;

public class MenuActionClearCenterpoint implements MenuAction {
    private final Activity mActivity;

    private final CacheListUpdater mCacheListUpdater;

    public MenuActionClearCenterpoint(Activity activity, CacheListUpdater cacheListUpdater) {
        mActivity = activity;
        mCacheListUpdater = cacheListUpdater;
    }

    @Override
    public void act() {
        mCacheListUpdater.setCacheAsCenter(null);
    }

    @Override
    public String getLabel() {
        return mActivity.getResources().getString(R.string.clear_centerpoint);
    }

    @Override
    public boolean isEnabled() {
        return mCacheListUpdater.isCenterpointEnabled();
    }

}
