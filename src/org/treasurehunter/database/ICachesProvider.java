
package org.treasurehunter.database;

import org.treasurehunter.GeocacheList;

/**
 * Interface to access a subset of the cache database. Used to form a Decorator
 * pattern.
 */
public interface ICachesProvider {

    /**
     * Returns true if the result of getCaches() may have changed since the last
     * call to resetChanged()
     */
    public boolean hasChanged();

    /** Reset the change flag (never done from within the class) */
    public void resetChanged();

    public int getCount();

    /**
     * If the count is bigger than maxRelevantCount, the implementation may
     * return maxRelevantCount if that is more efficient.
     */
    // public int getCount(int maxRelevantCount);

    /** The returned list is considered immutable */
    public GeocacheList getCaches();
}
