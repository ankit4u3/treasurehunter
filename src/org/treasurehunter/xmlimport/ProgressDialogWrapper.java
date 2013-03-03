
package org.treasurehunter.xmlimport;

import org.treasurehunter.task.Task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/** Runs on the main thread, processing events from the BcachingProcess thread */
public class ProgressDialogWrapper implements ProcessStatusListener {
    public interface ProcessFinishedListener {
        public void onFinished(boolean success);
    }

    private final Context mContext;

    private final ProcessFinishedListener mFinishedListener;

    private ProgressDialog mProgressDialog;

    private Task mTask = null;

    public ProgressDialogWrapper(Context context, ProcessFinishedListener finishedListener) {
        mContext = context;
        mFinishedListener = finishedListener;
    }

    /** Points out the task that is to be aborted if the dialog is dismissed */
    public void setTask(Task task) {
        mTask = task;
    }

    /** Shows a dialog without progress bar */
    public void show(String title, String status) {
        mProgressDialog = ProgressDialog.show(mContext, title, status);

        if (mTask != null) {
            DialogInterface.OnCancelListener listener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        // This could happen if the user changed orientation,
                        // causing a new View to be created. The old one won't
                        // be
                        // available and dismiss() will fail.
                        e.printStackTrace();
                    }
                    mTask.abort();
                }
            };
            mProgressDialog.setOnCancelListener(listener);
            mProgressDialog.setCancelable(true);
        }
    }

    public void show(String title, String status, int maxProgress) {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(status);
        mProgressDialog.setMax(maxProgress);
        mProgressDialog.show();

        if (mTask != null) {
            DialogInterface.OnCancelListener listener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        // Ignore
                    }
                    mTask.abort();
                }
            };
            mProgressDialog.setOnCancelListener(listener);
            mProgressDialog.setCancelable(true);
        }
    }

    @Override
    public void onProcessFinished(boolean success) {
        Log.d("TreasureHunter", "onProcessFinished(" + (success ? "true" : "false") + ")");
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // Ignore
        }
        mFinishedListener.onFinished(success);
    }

    @Override
    public void onProcessStatus(String status) {
        mProgressDialog.setMessage(status);
    }

    @Override
    public void onProcessProgress(int progress) {
        mProgressDialog.setProgress(progress);
    }

    @Override
    public void onProcessNewMaxProgress(int max) {
        Log.d("TreasureHunter", "onProcessNewMaxProgress " + max);
        mProgressDialog.setMax(max);
    }
}
