
package org.treasurehunter.activity.filterlist;

import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.Tags;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.ArrayList;

public class FilterTypeCollection {
    private static final String FILTER_PREFS = "Filter";

    private final Activity mActivity;

    private ArrayList<GeocacheFilter> mFilterTypes = new ArrayList<GeocacheFilter>();

    public FilterTypeCollection(Activity activity) {
        mActivity = activity;
        load();
    }

    /** Loads/reloads the list of filters from the Preferences */
    private void load() {
        mFilterTypes.clear();
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        String ids = prefs.getString("FilterList", "");
        String[] idArray = ids.split(", ");
        if (idArray.length == 1 && idArray[0].equals("")) {
            // No filters were registered. Setup the default ones
            firstSetup();
        } else {
            for (String id : idArray) {
                mFilterTypes.add(new GeocacheFilter(id, mActivity));
            }
        }
    }

    private void firstSetup() {
        Log.i("TreasureHunter", "FilterTypeCollection first setup");
        {
            FilterPreferences pref = new FilterPreferences("All caches");
            add(new GeocacheFilter("All", mActivity, pref));
        }

        {
            FilterPreferences favoritesPref = new FilterPreferences("Favorites");
            favoritesPref.setString("FilterTags", Integer.toString(Tags.FAVORITE));
            add(new GeocacheFilter("Favorites", mActivity, favoritesPref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Newly updated");
            pref.setString("FilterTags", Integer.toString(Tags.NEW));
            add(new GeocacheFilter("New", mActivity, pref));
        }

        {
            FilterPreferences foundPref = new FilterPreferences("Found");
            foundPref.setString("FilterTag", Integer.toString(Tags.FOUND));
            add(new GeocacheFilter("Found", mActivity, foundPref));
        }

        {
            FilterPreferences foundPref = new FilterPreferences("Not Found");
            foundPref.setString("FilterForbiddenTags", Integer.toString(Tags.FOUND));
            add(new GeocacheFilter("Not Found", mActivity, foundPref));
        }

        {
            FilterPreferences dnfPref = new FilterPreferences("Did Not Find");
            dnfPref.setString("FilterTag", Integer.toString(Tags.DNF));
            add(new GeocacheFilter("DNF", mActivity, dnfPref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Custom 1");
            add(new GeocacheFilter("Filter1", mActivity, pref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Custom 2");
            add(new GeocacheFilter("Filter2", mActivity, pref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Custom 3");
            add(new GeocacheFilter("Filter3", mActivity, pref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Custom 4");
            add(new GeocacheFilter("Filter4", mActivity, pref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Custom 5");
            add(new GeocacheFilter("Filter5", mActivity, pref));
        }

        {
            FilterPreferences pref = new FilterPreferences("Custom 6");
            add(new GeocacheFilter("Filter6", mActivity, pref));
        }

        String filterList = null;
        for (GeocacheFilter cacheFilter : mFilterTypes) {
            cacheFilter.saveToPreferences();
            if (filterList == null)
                filterList = cacheFilter.mId;
            else
                filterList = filterList + ", " + cacheFilter.mId;
        }
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        Editor editor = prefs.edit();
        editor.putString("FilterList", filterList);
        editor.commit();
    }

    private void add(GeocacheFilter cacheFilter) {
        mFilterTypes.add(cacheFilter);
    }

    private GeocacheFilter getFromId(String id) {
        for (GeocacheFilter cacheFilter : mFilterTypes)
            if (cacheFilter.mId.equals(id))
                return cacheFilter;
        return null;
    }

    public GeocacheFilter getActiveFilter() {
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        String id = prefs.getString("ActiveFilter", "All");
        return getFromId(id);
    }

    public void setActiveFilter(GeocacheFilter cacheFilter) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        Editor editor = prefs.edit();
        editor.putString("ActiveFilter", cacheFilter.mId);
        editor.commit();
    }

    public int getCount() {
        return mFilterTypes.size();
    }

    public GeocacheFilter get(int position) {
        return mFilterTypes.get(position);
    }

    public int getIndexOf(GeocacheFilter cacheFilter) {
        return mFilterTypes.indexOf(cacheFilter);
    }
}
