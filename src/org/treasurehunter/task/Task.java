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

import org.treasurehunter.xmlimport.AbortFlag;

import android.os.Handler;
import android.util.Log;

/**
 * A Task is something to be done asynchronously. Better than AsyncTask since
 * the tasks are remembered by a TaskRunner and can all be aborted at once.
 */
public abstract class Task implements AbortFlag {
    protected boolean mIsAborted = false;

    public boolean isAborted() {
        return mIsAborted;
    }

    /**
     * Called on the GUI thread. Must not block. Make the worker thread's
     * doInBackground() finish as soon as possible.
     */
    public void abort() {
        Log.d("geohunter", "abort for Task " + getClass().toString());
        mIsAborted = true;
    }

    protected abstract void doInBackground(Handler handler);
}
