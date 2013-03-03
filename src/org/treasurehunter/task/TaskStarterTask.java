
package org.treasurehunter.task;

import android.os.Handler;

public class TaskStarterTask extends Task {
    private final Task mTaskToStart;

    private final ITaskRunner mTaskRunner;

    public TaskStarterTask(Task taskToStart, ITaskRunner taskRunner) {
        mTaskToStart = taskToStart;
        mTaskRunner = taskRunner;
    }

    @Override
    protected void doInBackground(Handler handler) {
        mTaskRunner.runTask(mTaskToStart);
    }

}
