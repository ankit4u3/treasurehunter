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

package org.treasurehunter;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;

public class ErrorDisplayer {
    static class DisplayErrorRunnable implements Runnable {
        private final Builder mAlertDialogBuilder;

        DisplayErrorRunnable(Builder alertDialogBuilder) {
            mAlertDialogBuilder = alertDialogBuilder;
        }

        @Override
        public void run() {
            mAlertDialogBuilder.create().show();
        }
    }

    private final Activity mActivity;

    private final OnClickListener mOnClickListener;

    public ErrorDisplayer(Activity activity, OnClickListener onClickListener) {
        mActivity = activity;
        mOnClickListener = onClickListener;
    }

    public void displayError(int resId, Object... args) {
        final Builder alertDialogBuilder = new Builder(mActivity);
        alertDialogBuilder.setMessage(String.format((String)mActivity.getText(resId), args));
        alertDialogBuilder.setNeutralButton("Ok", mOnClickListener);

        mActivity.runOnUiThread(new DisplayErrorRunnable(alertDialogBuilder));
    }
}
