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

package org.treasurehunter.task;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Launches Tasks in separate threads. Keeps track of the threads so they can
 * all be aborted.
 */
public class ConcurrentTaskRunner implements ITaskRunner {

    private class TaskThread extends Thread {
        private final Task mTask;

        public TaskThread(Task worker) {
            super(worker.getClass().toString());
            mTask = worker;
        }

        @Override
        public void run() {
            mTask.mIsAborted = false;
            mTask.doInBackground(mHandler);
            onTaskFinished(this);
        }
    }

    private final Handler mHandler = new Handler();

    private List<TaskThread> mRunningTaskThreads = new ArrayList<TaskThread>();

    private boolean mIsAborting = false;

    /** Called on the task's thread */
    private synchronized void onTaskFinished(TaskThread taskThread) {
        mRunningTaskThreads.remove(taskThread);
    }

    @Override
    public synchronized void runTask(Task task) {
        if (!mIsAborting) {
            TaskThread taskThread = new TaskThread(task);
            mRunningTaskThreads.add(taskThread);
            taskThread.start();
        }
    }

    /** Aborts all workers, not returning until they all finished */
    @Override
    public synchronized void abortAndJoin() {
        mIsAborting = true;

        for (TaskThread workerThread : mRunningTaskThreads)
            workerThread.mTask.abort();

        for (TaskThread workerThread : mRunningTaskThreads) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
            }
        }
        mRunningTaskThreads.clear();
        mIsAborting = false;
    }

    @Override
    public synchronized void abort() {
        for (TaskThread workerThread : mRunningTaskThreads)
            workerThread.mTask.abort();
    }
}
