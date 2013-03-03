
package org.treasurehunter.cacheactions;

import org.treasurehunter.Clock;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheType;
import org.treasurehunter.R;
import org.treasurehunter.Source;
import org.treasurehunter.Waypoint;
import org.treasurehunter.activity.edit.EditCacheActivity;
import org.treasurehunter.database.CacheWriter;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;

public class CacheActionAddWaypoint extends StaticLabelCache implements CacheAction {

    private final Activity mActivity;

    private final DbFrontend mDbFrontend;

    public CacheActionAddWaypoint(Activity activity, Resources resources, DbFrontend dbFrontend) {
        super(resources, R.string.menu_add_waypoint);
        mActivity = activity;
        mDbFrontend = dbFrontend;
    }

    @Override
    public void act(Geocache parentCache, Waypoint waypoint) {
        if (parentCache == null)
            return;

        CacheWriter cw = mDbFrontend.getCacheWriter();
        // TODO generate id
        CharSequence id = String.format("ML%1$tk%1$tM%1$tS", System.currentTimeMillis());
        cw.insertAndUpdateWaypoint(id, "Waypoint", parentCache.getLatitude(),
                parentCache.getLongitude(), Source.MY_LOCATION, GeocacheType.WAYPOINT,
                Clock.getCurrentStringTime(), parentCache.getId());

        Intent intent = new Intent(mActivity, EditCacheActivity.class);
        intent.putExtra(Waypoint.ID, id);
        intent.putExtra("creating", true);
        mActivity.startActivityForResult(intent, 0);
    }
}
