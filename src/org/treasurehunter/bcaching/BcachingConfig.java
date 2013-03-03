
package org.treasurehunter.bcaching;

import org.treasurehunter.task.TaskQueueRunner;

import android.content.SharedPreferences;
import android.util.Log;

public class BcachingConfig {
    private final SharedPreferences mSharedPreferences;

    /**
     * The TaskRunner that executes all tasks that relate to bcaching. This
     * prevents multiple access of bcaching at once.
     */
    private final TaskQueueRunner mTaskRunner;

    public BcachingConfig(SharedPreferences sharedPreferences, TaskQueueRunner taskRunner) {
        mSharedPreferences = sharedPreferences;
        mTaskRunner = taskRunner;
    }

    public TaskQueueRunner getTaskRunner() {
        return mTaskRunner;
    }

    /** @return the set bcaching.com username or "" */
    public String getUsername() {
        return mSharedPreferences.getString("bcaching_username", "");
    }

    /** @return the set bcaching.com password or "" */
    public String getPassword() {
        return mSharedPreferences.getString("bcaching_password", "");
    }

    public boolean isSetup() {
        if (getUsername().equals(""))
            return false;
        if (getPassword().equals(""))
            return false;
        return true;
    }

    /**
     * Returns the time in a format that can be sent to as bcaching a=list
     * parameter 'since'
     */
    public String getLastUpdate() {
        return mSharedPreferences.getString("bcaching_lastupdate", "");
    }

    public void setLastUpdate(long time) {
        Log.d("TreasureHunter", "Setting bcaching_lastupdate to " + time);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("bcaching_lastupdate", Long.toString(time));
        editor.commit();
    }

    public void setLastUpdateToNow() {
        setLastUpdate(System.currentTimeMillis());
    }

    public void clearLastUpdate() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("bcaching_lastupdate", "");
        editor.commit();
    }

    public boolean shouldDownloadMyFinds() {
        return mSharedPreferences.getBoolean("bcaching_myfinds", true);
    }

    public boolean shouldDownloadMyHides() {
        return mSharedPreferences.getBoolean("bcaching_myhides", true);
    }
}
