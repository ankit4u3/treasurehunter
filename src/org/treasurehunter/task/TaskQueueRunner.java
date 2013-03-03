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

/** Runs a list of Tasks, one at a time */
public class TaskQueueRunner implements ITaskRunner {

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
            onTaskFinished();
        }
    }

    private TaskThread mRunningTaskThread;

    private List<Task> mQueue = new ArrayList<Task>();

    private boolean mIsAborting = false;

    private final Handler mHandler = new Handler();

    public TaskQueueRunner() {
    }

    /** Called on the worker thread */
    private synchronized void onTaskFinished() {
        mRunningTaskThread = null;
        if (!mIsAborting)
            runNext();
    }

    @Override
    public synchronized void abort() {
        if (mRunningTaskThread != null) {
            mRunningTaskThread.mTask.abort();
        }
    }

    @Override
    public synchronized void runTask(Task task) {
        mQueue.add(task);
        if (mRunningTaskThread == null)
            runNext();
    }

    /** Called on either the main or worker thread */
    private synchronized void runNext() {
        if (mQueue.isEmpty())
            return;
        Task task = mQueue.remove(0);
        mRunningTaskThread = new TaskThread(task);
        mRunningTaskThread.start();
    }

    @Override
    public synchronized void abortAndJoin() {
        if (mRunningTaskThread == null)
            return;

        mIsAborting = true;
        mRunningTaskThread.mTask.abort();
        try {
            mRunningTaskThread.join();
        } catch (InterruptedException e) {
        }
        mIsAborting = false;
    }

    public synchronized void clearEnqueued() {
        mQueue.clear();
    }
}
