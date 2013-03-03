package org.treasurehunter;

import org.treasurehunter.database.DbFrontend;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/** As long as this wrapper class has the only reference to the 
 * GeocacheListCursor, it takes care of closing the cursor. */
class GeocacheListContainer extends GeocacheList {
    private GeocacheListCursor mGeocacheList;
    private int mRefcount;
    public GeocacheListContainer(GeocacheListCursor geocacheList) {
        mGeocacheList = geocacheList;
        mRefcount = 1;
    }
    public void set(GeocacheListCursor list) {
        if (mGeocacheList != null)
            mGeocacheList.close();
        mGeocacheList = list;
    }
    @Override
    public Iterator<Geocache> iterator() {
        return mGeocacheList.iterator();
    }
    @Override
    public Geocache get(int location) {
        return mGeocacheList.get(location);
    }
    @Override
    public int size() {
        return mGeocacheList.size();
    }
    public void incRefcount() {
        mRefcount++;
    }
    public void decRefcount() {
        if (mRefcount <= 0) {
            Log.e("TreasureHunter", "GeocacheListContainer decRefcount already without references");
            return;
        }
        mRefcount--;
        if (mRefcount == 0) {
            mGeocacheList.close();
            mGeocacheList = null;
        }
    }
}


/** GeocacheDbView: Manages a sqlite view as the already filtered list of geocaches.
 * The class fetches the geocaches from that view when first needed,
 * or loads them all at once when preloadAll() is called. */

/** Maintains a database Cursor to read out a geocache only when it is first needed.
 * Must close() when no longer using the list or the Cursor will remain open. */
public class GeocacheListCursor extends GeocacheList {
    /** Elements are Geocache or null for indices not loaded yet */
    private ArrayList<Geocache> mAlreadyLoaded;
    
    private final DbFrontend mDbFrontend;

    private final GeocacheFactory mGeocacheFactory;

    private int mSize;

    private Cursor mCursor;
    
    private final String mSqlQuery;
    
    private int mLoadedCount = 0;
    
    /** @param initialList The list of cacheIds (Strings) to load */
    public GeocacheListCursor(DbFrontend dbFrontend, GeocacheFactory geocacheFactory, 
            String sqlQuery) {
        mDbFrontend = dbFrontend;
        mGeocacheFactory = geocacheFactory;
        mSqlQuery = sqlQuery;

        refreshCursor();
    }

    public void refreshCursor() {
        mCursor = mDbFrontend.getCursor(mSqlQuery);
        mSize = mCursor.getCount();
        mAlreadyLoaded = new ArrayList<Geocache>(mSize);
        mLoadedCount = 0;
    }
    
    //Called from CachesProviderCenterThread
    //Assumes the lists have the same ordering!
    public boolean equals(GeocacheListCursor otherList) {
        if (mSize != otherList.mSize)
            return false;
        
        for (int i = 0; i < mSize; i++) {
            if (!getId(i).equals(otherList.getId(i))) {
                return false;
            }
        }
        
        return true;
    }

    private class CursorIterator implements Iterator<Geocache> {
        private int nextIx = 0;
        @Override
        public boolean hasNext() {
            return mSize > nextIx;
        }
        @Override
        public Geocache next() {
            Geocache geocache = get(nextIx);
            nextIx++;
            return geocache;
        }
        @Override
        public void remove() {
            //GeocacheLists are immutable
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public Iterator<Geocache> iterator() {
        return new CursorIterator();
    }

    @Override
    public int size() {
        return mSize;
    }

    public Geocache get(int position) {
        Geocache geocache = mAlreadyLoaded.get(position);
        if (geocache == null) {
            //Could check if cursor is null and if so refreshCursor()
            mCursor.moveToPosition(position);
            geocache = mGeocacheFactory.fromCursor(mCursor);
            mAlreadyLoaded.set(position, geocache);
            mLoadedCount++;
            if (mLoadedCount == mSize) {
                mCursor.close();
                mCursor = null;
            }
        }
        return geocache;
    }

    @Override
    protected void finalize() {
        if (mCursor != null)
            mCursor.close();
        mCursor = null;
    }

    public void close() {
        if (mCursor != null)
            mCursor.close();
        mCursor = null;
    }
    
    protected CharSequence getId(int position) {
        Geocache geocache = mAlreadyLoaded.get(position);
        if (geocache == null) {
            mCursor.moveToPosition(position);
            return mCursor.getString(2);
        }
        return geocache.getId();
    }
}
