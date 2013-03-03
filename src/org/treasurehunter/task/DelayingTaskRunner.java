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

/**
 * TaskRunner that only enqueues the most recently waiting Task. Waits a certain
 * amount of time before starting a Task, in case another task would be started
 * soon afterwards.
 */
public class DelayingTaskRunner implements ITaskRunner {
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

    private final int mDelayMs = 150;

    private final Handler mHandler = new Handler();

    private TaskThread mTaskThread;

    private Task mEnqueuedTask;

    private boolean mIsAborting = false;

    @Override
    public synchronized void runTask(Task task) {
        if (mIsAborting)
            return;
        if (mIsPaused || mTaskThread != null) {
            mEnqueuedTask = task;
        } else {
            mTaskStarter = new TaskStarter(task);
            mHandler.postDelayed(mTaskStarter, mDelayMs);
        }
    }

    private class TaskStarter implements Runnable {
        private Task mTask;

        public TaskStarter(Task task) {
            mTask = task;
        }

        @Override
        public void run() {
            synchronized (DelayingTaskRunner.this) {
                if (mTaskStarter == this) {
                    // No other Task enqueued more recently
                    mTaskStarter = null;
                    startCalculate(mTask);
                }
            }
        }
    }

    private TaskStarter mTaskStarter;

    private synchronized void startCalculate(Task task) {
        if (!mIsAborting) {
            mTaskThread = new TaskThread(task);
            mTaskThread.start();
        }
    }

    /** Called on the task's thread */
    private synchronized void onTaskFinished() {
        mTaskThread = null;
        if (!mIsPaused && mEnqueuedTask != null) {
            Task next = mEnqueuedTask;
            mEnqueuedTask = null;
            startCalculate(next);
        }
    }

    /**
     * Aborts the tasks without waiting for completion. The aborted task will be
     * allowed to finish before any future task is run again.
     */
    public synchronized void abort() {
        mEnqueuedTask = null;
        if (mTaskThread != null) {
            mTaskThread.mTask.abort();
            // The task will still call onTaskFinished()
        }
    }

    @Override
    public synchronized void abortAndJoin() {
        mIsAborting = true;
        mEnqueuedTask = null;
        if (mTaskThread != null) {
            mTaskThread.mTask.abort();
            try {
                mTaskThread.join();
            } catch (InterruptedException e) {
            }
            mTaskThread = null;
        }
        mIsAborting = false;
    }

    private boolean mIsPaused = false;

    /** Don't start new tasks until resume() is called. */
    public void pause() {
        mIsPaused = true;
    }

    public synchronized void resume() {
        if (!mIsPaused)
            return;
        mIsPaused = false;
        if (mTaskThread == null && mEnqueuedTask != null) {
            Task next = mEnqueuedTask;
            mEnqueuedTask = null;
            startCalculate(next);
        }
    }
}
