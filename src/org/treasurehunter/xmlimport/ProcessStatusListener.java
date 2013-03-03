
package org.treasurehunter.xmlimport;

public interface ProcessStatusListener {
    void onProcessStatus(String status);

    void onProcessProgress(int progress);

    void onProcessNewMaxProgress(int max);

    void onProcessFinished(boolean success);
    // void onProcessAbort();
}
