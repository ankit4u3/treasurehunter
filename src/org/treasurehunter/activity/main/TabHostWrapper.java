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

package org.treasurehunter.activity.main;

import org.treasurehunter.GeoFixProvider;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A TabHost customized to use TabBase instances as contents of the tabs.
 */
public class TabHostWrapper {
    public static final int LIST_TAB = 0;

    public static final int MAP_TAB = 1;

    public static final int COMPASS_TAB = 2;

    private final TabHost mTabHost;

    private final ArrayList<TabBase> mTabs = new ArrayList<TabBase>();

    private final GeoFixProvider mGeoFixProvider;

    private int mCurrentTabIx = -1;

    public TabHostWrapper(TabHost tabHost, GeoFixProvider geoFixProvider) {
        mTabHost = tabHost;
        mGeoFixProvider = geoFixProvider;
    }

    public TabBase getCurrentTab() {
        return mTabs.get(mCurrentTabIx);
    }

    private static class TabCreator implements TabHost.TabContentFactory {
        private final TabBase mTab;

        public TabCreator(TabBase tab) {
            mTab = tab;
        }

        @Override
        public View createTabContent(String tag) {
            return mTab.getContentView();
        }
    }

    public void addTab(TabBase tab) {
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tab.getId());
        tabSpec.setIndicator(tab.getTitle(), tab.getIcon());
        tabSpec.setContent(new TabCreator(tab));
        mTabHost.addTab(tabSpec);
        mTabs.add(tab);
        // mTabHost.getTabWidget().getChildAt(mTabs.size()-1).getLayoutParams().height
        // = 56;
    }

    private void resumeTab(int ix) {
        TabBase tab = mTabs.get(ix);
        if (!tab.mIsLoaded) {
            tab.mIsLoaded = true;
            tab.onCreate();
        }
        mCurrentTabIx = ix;
        tab.onResume();
    }

    /** Change tabs programmatically. */
    public void switchToTab(int tabIx) {
        if (mCurrentTabIx == tabIx)
            return;
        // getCurrentTab().onPause(); //Will be called by OnTabChangedListener
        // resumeTab(tabIx); //Will be called by OnTabChangedListener
        mTabHost.setCurrentTab(tabIx);
    }

    void onResume() {
        resumeTab(mCurrentTabIx);
        getCurrentTab().getContentView().setVisibility(View.VISIBLE);
        mGeoFixProvider.startUpdates();
    }

    void onPause() {
        mGeoFixProvider.stopUpdates();
        getCurrentTab().getContentView().setVisibility(View.INVISIBLE);
        getCurrentTab().onPause();
    }

    void onCreate(Bundle savedInstanceState) {
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (mCurrentTabIx != -1) {
                    mTabs.get(mCurrentTabIx).onPause();
                }
                resumeTab(mTabHost.getCurrentTab());
            }
        });

        if (savedInstanceState != null) {
            int newTab = savedInstanceState.getInt("currentTab", 0);
            switchToTab(newTab);
            resumeTab(newTab); // Not called by onTabChangeListener at this
                               // point?
        } else {
            // mCurrentTabIx = 0;
            switchToTab(0);
            resumeTab(0); // Not called by onTabChangeListener at this point?
        }
    }

    boolean onMenuOpened(int featureId, Menu menu) {
        if (menu == null)
            // A fail log indicates that null was sent here, for some reason
            return true;
        return getCurrentTab().getMenuActions().onMenuOpened(menu);
    }

    boolean onOptionsItemSelected(MenuItem item) {
        return getCurrentTab().getMenuActions().act(item.getItemId());
    }

    boolean onContextItemSelected(MenuItem item) {
        return getCurrentTab().onContextItemSelected(item);
    }

    boolean onKeyDown(int keyCode, KeyEvent event) {
        return getCurrentTab().onKeyDown(keyCode, event);
    }

    boolean onPrepareOptionsMenu(Menu menu) {
        if (!getCurrentTab().getMenuActions().onCreateOptionsMenu(menu))
            return false;
        return getCurrentTab().getMenuActions().onMenuOpened(menu);
    }

    void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentTab", mCurrentTabIx);
    }

    public List<TabBase> getTabs() {
        return mTabs;
    }
}
