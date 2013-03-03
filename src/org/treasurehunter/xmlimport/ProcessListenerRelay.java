
package org.treasurehunter.xmlimport;

import android.os.Handler;
import android.os.Message;

/** Called by the worker thread to relay events to the main thread. */
class ProcessListenerRelay extends Handler implements ProcessStatusListener {
    private static final int MSG_PROGRESS = 0;

    private static final int MSG_STATUS = 1;

    private static final int MSG_MAXPROGRESS = 2;

    private static final int MSG_SUCCESS = 3;

    private static final int MSG_FAILURE = 4;

    private final ProcessStatusListener mListener;

    public ProcessListenerRelay(ProcessStatusListener listener) {
        mListener = listener;
    }

    /** Runs on the main thread */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_PROGRESS:
                mListener.onProcessProgress(msg.arg1);
                break;
            case MSG_STATUS:
                mListener.onProcessStatus((String)msg.obj);
                break;
            case MSG_MAXPROGRESS:
                mListener.onProcessNewMaxProgress(msg.arg1);
                break;
            case MSG_SUCCESS:
                mListener.onProcessFinished(true);
                break;
            case MSG_FAILURE:
                mListener.onProcessFinished(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProcessStatus(String status) {
        Message msg = new Message();
        msg.what = ProcessListenerRelay.MSG_STATUS;
        msg.obj = status;
        sendMessage(msg);
    }

    @Override
    public void onProcessProgress(int progress) {
        Message msg = new Message();
        msg.what = ProcessListenerRelay.MSG_PROGRESS;
        msg.arg1 = progress;
        sendMessage(msg);
    }

    @Override
    public void onProcessNewMaxProgress(int max) {
        Message msg = new Message();
        msg.what = ProcessListenerRelay.MSG_MAXPROGRESS;
        msg.arg1 = max;
        sendMessage(msg);
    }

    @Override
    public void onProcessFinished(boolean success) {
        Message msg = new Message();
        msg.what = success ? ProcessListenerRelay.MSG_SUCCESS : ProcessListenerRelay.MSG_FAILURE;
        sendMessage(msg);
    }
}
