
package org.treasurehunter.activity.main;

import org.treasurehunter.GeocacheFilter;
import org.treasurehunter.menuactions.MenuActions;

import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

public abstract class TabBase {
    private final String mId;

    private final String mTitle;

    private final View mContentView;

    private final Drawable mIcon;

    private MenuActions mMenuActions;

    boolean mIsLoaded = false;

    public TabBase(String id, String title, View contentView, Drawable icon, MenuActions menuActions) {
        mId = id;
        mTitle = title;
        mContentView = contentView;
        mIcon = icon;
        mMenuActions = menuActions;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public View getContentView() {
        return mContentView;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public MenuActions getMenuActions() {
        return mMenuActions;
    }

    // /// Lifecycle methods /////
    public void onCreate() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    /**
     * Return false to allow normal context menu processing to proceed, true to
     * consume it here.
     */
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * Lets the tab know that the database changed (and possibly the active
     * filter)
     */
    public void onDataViewChanged(GeocacheFilter filter, boolean isTabActive) {
    }

}
