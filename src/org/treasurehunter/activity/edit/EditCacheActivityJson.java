
package org.treasurehunter.activity.edit;

import org.treasurehunter.GeoObject;
import org.treasurehunter.Geocache;
import org.treasurehunter.GeocacheFactory;
import org.treasurehunter.R;
import org.treasurehunter.Waypoint;
import org.treasurehunter.database.DatabaseLocator;
import org.treasurehunter.database.DbFrontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;

public class EditCacheActivityJson extends Activity {
    private DbFrontend mDbFrontend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final GeocacheFactory geocacheFactory = new GeocacheFactory();
        DatabaseLocator databaseLocator = new DatabaseLocator(this);
        mDbFrontend = new DbFrontend(databaseLocator, geocacheFactory);
        setContentView(R.layout.cache_edit);
        final Intent intent = getIntent();
        final EditCacheActivityDelegate editCache = new EditCacheActivityDelegate(
                (EditText)findViewById(R.id.edit_id), (EditText)findViewById(R.id.edit_name),
                (EditText)findViewById(R.id.edit_latitude),
                (EditText)findViewById(R.id.edit_longitude), mDbFrontend);

        boolean creating = intent.getBooleanExtra("creating", false);
        if (creating)
            ((Button)findViewById(R.id.edit_cancel)).setText(R.string.dont_create);
        final EditCacheActivityDelegate.CancelButtonOnClickListener cancelButtonOnClickListener = new EditCacheActivityDelegate.CancelButtonOnClickListener(
                this, editCache, creating);

        EditCacheActivityDelegate.CacheSaverOnClickListener cacheSaver = new EditCacheActivityDelegate.CacheSaverOnClickListener(
                this, editCache);

        ((Button)findViewById(R.id.edit_set)).setOnClickListener(cacheSaver);
        ((Button)findViewById(R.id.edit_cancel)).setOnClickListener(cancelButtonOnClickListener);

        GeoObject geoobject = null;
        if (intent.getStringExtra(Waypoint.ID) != null) {
            geoobject = mDbFrontend.loadWaypointFromId(intent.getStringExtra(Waypoint.ID));
        } else {
            geoobject = mDbFrontend.loadCacheFromId(intent.getStringExtra(Geocache.ID));
        }
        editCache.setGeoObject(geoobject);
        ((Button)findViewById(R.id.edit_set)).performClick();
        // findViewById(R.id.edit_name).requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // From API 5, onBackPressed() could be overridden
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((Button)findViewById(R.id.edit_cancel)).performClick();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbFrontend.closeDatabase();
    }
}
