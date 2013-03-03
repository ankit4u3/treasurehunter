
package org.treasurehunter.xmlimport;

import org.treasurehunter.Source;

public interface IProcessStatus {
    boolean isFileAlreadyLoaded(Source source, String sqlDate);

    void abortedBeforeStart();

    void onStartLoadingSource(Source source);

    void onParsedGeocache(GeocacheData data);

    void onFinishedLoadingSource(String gpxDate);
}
