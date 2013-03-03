
package org.treasurehunter;

public interface GeoFixProvider {

    public void startUpdates();

    public void stopUpdates();

    public GeoFix getLocation();

    public void addObserver(Refresher refresher);

    public void removeObserver(Refresher refresher);

    public boolean isProviderEnabled();

    public float getAzimuth();
}
