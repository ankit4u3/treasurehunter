
package org.treasurehunter.activity.main;

import org.treasurehunter.Geocache;
import org.treasurehunter.activity.cachelist.CacheListTab;

import android.app.SearchManager;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class SearchIntentHandler {

    private CacheListTab mCacheListTab;

    public SearchIntentHandler(CacheListTab cacheListTab) {
        mCacheListTab = cacheListTab;
    }

    public void handleSearchIntent(Intent intent) {
        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query == null || query.length() < 1) {
                return;
            }
            LinearLayout l = ((LinearLayout)mCacheListTab.getContentView());
            ListView lv = (ListView)l.getChildAt(0);
            for (int i = 1; i < lv.getCount(); ++i) {
                Geocache cache = (Geocache)lv.getItemAtPosition(i);
                String name = cache.getName().toString();
                String id = cache.getId().toString();
                Log.d("TreasureHunter", "i: " + i + ", name: " + name + ", id: " + id);
                if (name.toLowerCase().indexOf(query) > -1 || id.toLowerCase().indexOf(query) > -1) {
                    lv.setSelection(i);
                    Log.d("TreasureHunter",
                            "Selected: i: " + i + ", name: "
                                    + ((Geocache)lv.getItemAtPosition(i)).getName());
                    return;
                }
            }
            // Send notification to user:
            Toast toast = Toast.makeText(mCacheListTab.getContentView().getContext()
                    .getApplicationContext(), "No caches found in the list for: " + query,
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
