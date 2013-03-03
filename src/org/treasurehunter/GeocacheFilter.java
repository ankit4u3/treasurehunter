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

package org.treasurehunter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A GeocacheFilter determines which of all geocaches that should be visible in
 * the list and map views. It can be translated into a SQL constraint for
 * accessing the database. Mutable class but the design would be better if it
 * was immutable.
 */
public class GeocacheFilter {
    private final Activity mActivity;

    /** The name of this filter as visible to the user */
    private String mName;

    /** The string used in Preferences to identify this filter */
    public final String mId;

    /** The name of this filter as visible to the user */
    public String getName() {
        return mName;
    }

    public static interface FilterGui {
        public boolean getBoolean(int id);

        public String getString(int id);

        public void setBoolean(int id, boolean value);

        public void setString(int id, String value);
    }

    private static class BooleanOption {
        public final String PrefsName;

        public final String SqlClause;

        public boolean Selected;

        public int ViewResource;

        public BooleanOption(String prefsName, String sqlClause, int viewResource) {
            PrefsName = prefsName;
            SqlClause = sqlClause;
            Selected = true;
            ViewResource = viewResource;
        }
    }

    private final BooleanOption[] mTypeOptions = {
            new BooleanOption("Traditional", "CacheType = 1", R.id.ToggleButtonTrad),
            new BooleanOption("Multi", "CacheType = 2", R.id.ToggleButtonMulti),
            new BooleanOption("Unknown", "CacheType = 3", R.id.ToggleButtonMystery),
            new BooleanOption("MyLocation", "CacheType = 4", R.id.ToggleButtonMyLocation),
            new BooleanOption("Others", "CacheType = 0 OR (CacheType >= 5 AND CacheType <= 14)",
                    R.id.ToggleButtonOthers),
    };

    // These SQL are to be applied when the option is deselected!
    private final BooleanOption[] mSizeOptions = {
            new BooleanOption("Micro", "Container != 1", R.id.ToggleButtonMicro),
            new BooleanOption("Small", "Container != 2", R.id.ToggleButtonSmall),
            new BooleanOption("UnknownSize", "Container != 0", R.id.ToggleButtonUnknownSize),
    };

    private String mFilterString;

    private static final Set<Integer> EMPTY_SET = new HashSet<Integer>();

    /**
     * Limits the filter to only include geocaches with this tag. Zero means no
     * limit.
     */
    private Set<Integer> mRequiredTags = EMPTY_SET;

    /**
     * Caches with this tag are not included in the results no matter what. Zero
     * means no restriction.
     */
    private Set<Integer> mForbiddenTags = EMPTY_SET;

    public GeocacheFilter(String id, Activity activity) {
        mId = id;
        mActivity = activity;
        SharedPreferences preferences = mActivity.getSharedPreferences(mId, 0);
        loadFromPreferences(preferences);
    }

    public GeocacheFilter(String id, Activity activity, SharedPreferences sharedPreferences) {
        mId = id;
        mActivity = activity;
        loadFromPreferences(sharedPreferences);
    }

    /**
     * Load the values from SharedPreferences.
     * 
     * @return true if any value in the filter was changed
     */
    private void loadFromPreferences(SharedPreferences preferences) {
        for (BooleanOption option : mTypeOptions) {
            option.Selected = preferences.getBoolean(option.PrefsName, true);
        }
        for (BooleanOption option : mSizeOptions) {
            option.Selected = preferences.getBoolean(option.PrefsName, true);
        }
        mFilterString = preferences.getString("FilterString", null);

        String required;
        try {
            required = preferences.getString("FilterTags", "");
        } catch (ClassCastException ex) {
            // Work-around for a bug in 1.9.1
            required = "";
        }
        mRequiredTags = StringToIntegerSet(required);

        String forbidden;
        try {
            forbidden = preferences.getString("FilterForbiddenTags", "");
        } catch (ClassCastException ex) {
            // Work-around for a bug in 1.9.1
            forbidden = "";
        }
        mForbiddenTags = StringToIntegerSet(forbidden);

        mName = preferences.getString("FilterName", "Unnamed");
    }

    public void saveToPreferences() {
        SharedPreferences preferences = mActivity.getSharedPreferences(mId, 0);
        SharedPreferences.Editor editor = preferences.edit();
        for (BooleanOption option : mTypeOptions) {
            editor.putBoolean(option.PrefsName, option.Selected);
        }
        for (BooleanOption option : mSizeOptions) {
            editor.putBoolean(option.PrefsName, option.Selected);
        }
        editor.putString("FilterString", mFilterString);
        editor.putString("FilterTags", SetToString(mRequiredTags));
        editor.putString("FilterForbiddenTags", SetToString(mForbiddenTags));
        editor.putString("FilterName", mName);
        editor.commit();
    }

    /** Converts a set of integers to a string with space between the numbers */
    private static String SetToString(Set<Integer> set) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (int i : set) {
            if (first)
                first = false;
            else
                buffer.append(' ');
            buffer.append(i);
        }
        return buffer.toString();
    }

    private static Set<Integer> StringToIntegerSet(String string) {
        if (string.equals(""))
            return EMPTY_SET;
        Set<Integer> set = new HashSet<Integer>();
        String[] parts = string.split(" ");
        for (String part : parts) {
            set.add(Integer.decode(part));
        }
        return set;
    }

    /**
     * @return A number of conditions separated by AND, or an empty string if
     *         there isn't any limit
     */
    public String getSqlWhereClause() {
        int count = 0;
        for (BooleanOption option : mTypeOptions) {
            if (option.Selected)
                count++;
        }

        StringBuilder result = new StringBuilder();
        boolean isFirst = true;

        if (count != mTypeOptions.length && count != 0) {
            for (BooleanOption option : mTypeOptions) {
                if (!option.Selected)
                    continue;
                if (isFirst) {
                    result.append("(");
                    isFirst = false;
                } else {
                    result.append(" OR ");
                }
                result.append(option.SqlClause);
            }
            result.append(")");
        }

        if (mFilterString != null && !mFilterString.equals("")) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append(" AND ");
            }

            String quoted = DatabaseUtils.sqlEscapeString("%" + mFilterString + "%");
            if (containsUppercase(mFilterString)) {
                // Do case-sensitive query
                result.append("(Id LIKE " + quoted + " OR Description LIKE " + quoted + ")");
            } else {
                // Do case-insensitive search
                quoted = quoted.toLowerCase();
                result.append("(lower(Id) LIKE " + quoted + " OR lower(Description) LIKE " + quoted
                        + ")");
            }
        }

        for (BooleanOption option : mSizeOptions) {
            if (!option.Selected) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    result.append(" AND ");
                }
                result.append(option.SqlClause);
            }
        }

        return result.toString();
    }

    public Set<Integer> getRequiredTags() {
        return mRequiredTags;
    }

    private static boolean containsUppercase(String string) {
        return !string.equals(string.toLowerCase());
    }

    public void loadFromGui(FilterGui provider) {
        String newName = provider.getString(R.id.NameOfFilter);
        if (!newName.trim().equals("")) {
            mName = newName;
        }
        for (BooleanOption option : mTypeOptions) {
            option.Selected = provider.getBoolean(option.ViewResource);
        }
        for (BooleanOption option : mSizeOptions) {
            option.Selected = provider.getBoolean(option.ViewResource);
        }
        mFilterString = provider.getString(R.id.FilterString);

        mRequiredTags = new HashSet<Integer>();
        mForbiddenTags = new HashSet<Integer>();

        if (provider.getBoolean(R.id.CheckBoxRequireFavorites))
            mRequiredTags.add(Tags.FAVORITE);
        else if (provider.getBoolean(R.id.CheckBoxForbidFavorites))
            mForbiddenTags.add(Tags.FAVORITE);

        if (provider.getBoolean(R.id.CheckBoxRequireFound))
            mRequiredTags.add(Tags.FOUND);
        else if (provider.getBoolean(R.id.CheckBoxForbidFound))
            mForbiddenTags.add(Tags.FOUND);

        if (provider.getBoolean(R.id.CheckBoxRequireDNF))
            mRequiredTags.add(Tags.DNF);
        else if (provider.getBoolean(R.id.CheckBoxForbidDNF))
            mForbiddenTags.add(Tags.DNF);

        if (provider.getBoolean(R.id.CheckBoxRequireNew))
            mRequiredTags.add(Tags.NEW);
        else if (provider.getBoolean(R.id.CheckBoxForbidNew))
            mForbiddenTags.add(Tags.NEW);

        if (provider.getBoolean(R.id.CheckBoxRequireMine)) {
            mRequiredTags.add(Tags.MINE);
        } else if (provider.getBoolean(R.id.CheckBoxForbidMine)) {
            mForbiddenTags.add(Tags.MINE);
        }
    }

    /** Set up the view from the values in this CacheFilter. */
    public void pushToGui(FilterGui provider) {
        provider.setString(R.id.NameOfFilter, mName);
        for (BooleanOption option : mTypeOptions) {
            provider.setBoolean(option.ViewResource, option.Selected);
        }
        for (BooleanOption option : mSizeOptions) {
            provider.setBoolean(option.ViewResource, option.Selected);
        }
        String filter = mFilterString == null ? "" : mFilterString;
        provider.setString(R.id.FilterString, filter);
        provider.setBoolean(R.id.CheckBoxRequireFavorites, mRequiredTags.contains(Tags.FAVORITE));
        provider.setBoolean(R.id.CheckBoxForbidFavorites, mForbiddenTags.contains(Tags.FAVORITE));
        provider.setBoolean(R.id.CheckBoxRequireFound, mRequiredTags.contains(Tags.FOUND));
        provider.setBoolean(R.id.CheckBoxForbidFound, mForbiddenTags.contains(Tags.FOUND));
        provider.setBoolean(R.id.CheckBoxRequireDNF, mRequiredTags.contains(Tags.DNF));
        provider.setBoolean(R.id.CheckBoxForbidDNF, mForbiddenTags.contains(Tags.DNF));
        provider.setBoolean(R.id.CheckBoxRequireNew, mRequiredTags.contains(Tags.NEW));
        provider.setBoolean(R.id.CheckBoxForbidNew, mForbiddenTags.contains(Tags.NEW));
        provider.setBoolean(R.id.CheckBoxRequireMine, mRequiredTags.contains(Tags.MINE));
        provider.setBoolean(R.id.CheckBoxForbidMine, mForbiddenTags.contains(Tags.MINE));
    }

    public Set<Integer> getForbiddenTags() {
        return mForbiddenTags;
    }

    public String getSql(double latLow, double lonLow, double latHigh, double lonHigh) {
        return getSql("Latitude >= " + latLow + " AND Latitude < " + latHigh + " AND Longitude >= "
                + lonLow + " AND Longitude < " + lonHigh);
    }

    public String getSql() {
        return getSql("");
    }

    /** Returns a complete SQL query except from 'SELECT x' */
    private String getSql(String limits) {
        ArrayList<String> where = new ArrayList<String>(4);
        if (!limits.equals("")) {
            where.add(limits);
        }

        String filter = getSqlWhereClause();
        // Log.d("TreasureHunter", "getSqlWhereClause() is " + filter);
        if (!filter.equals(""))
            where.add(filter);

        String join = "";
        if (mForbiddenTags.size() > 0) {
            StringBuffer forbidden = new StringBuffer();
            boolean first = true;
            for (Integer tagId : mForbiddenTags) {
                if (first) {
                    first = false;
                } else {
                    forbidden.append(" or ");
                }
                forbidden.append("TagId=" + tagId);
            }
            join = " left outer join (select CacheId from cachetags where " + forbidden
                    + ") as FoundTags on caches.Id = FoundTags.CacheId";
            where.add("FoundTags.CacheId is NULL");
        }

        StringBuffer tables = new StringBuffer("FROM CACHES");
        int ix = 1;
        for (Integer tagId : mRequiredTags) {
            String table = "tags" + ix;
            tables.append(", CACHETAGS " + table);
            where.add(table + ".TagId=" + tagId + " AND " + table + ".CacheId=Id");
            ix++;
        }

        StringBuffer completeSql = tables; // new StringBuffer(tables);
        completeSql.append(join);
        boolean first = true;
        for (String part : where) {
            if (first) {
                completeSql.append(" WHERE ");
                first = false;
            } else {
                completeSql.append(" AND ");
            }
            completeSql.append(part);
        }

        String sql = completeSql.toString();
        // Log.d("TreasureHunter", "CacheFilter created sql " + sql);
        return sql;
    }

}
