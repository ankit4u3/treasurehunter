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

package org.treasurehunter.activity.cachelist;

import org.treasurehunter.Clock;
import org.treasurehunter.GeoFix;
import org.treasurehunter.GeoFixProvider;
import org.treasurehunter.R;
import org.treasurehunter.Refresher;

import android.content.Context;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GpsStatusWidgetDelegate implements Refresher {
    private final Context mContext;

    private DistanceFormatter mDistanceFormatter;

    private final GeoFixProvider mGeoFixProvider;

    private final TextView mProvider;

    private final TextView mStatus;

    private final TextView mLagTextView;

    private GeoFix mGeoFix;

    private final Clock mClock;

    private final TextView mAccuracyView;

    public GpsStatusWidgetDelegate(Context context, View gpsWidgetView,
            GeoFixProvider geoFixProvider, DistanceFormatter distanceFormatter, Clock clock) {
        mGeoFixProvider = geoFixProvider;
        mDistanceFormatter = distanceFormatter;
        final TextView accuracyView = (TextView)gpsWidgetView.findViewById(R.id.accuracy);
        final TextView lag = (TextView)gpsWidgetView.findViewById(R.id.lag);
        final TextView status = (TextView)gpsWidgetView.findViewById(R.id.status);
        final TextView provider = (TextView)gpsWidgetView.findViewById(R.id.provider);

        mProvider = provider;
        mContext = context;
        mStatus = status;
        mLagTextView = lag;
        mGeoFix = GeoFix.NO_FIX;
        mClock = clock;
        mAccuracyView = accuracyView;
    }

    // TODO: onStatusChanged should be called from GeoFixProvider
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                mStatus.setText(provider + " status: "
                        + mContext.getString(R.string.out_of_service));
                break;
            case LocationProvider.AVAILABLE:
                mStatus.setText(provider + " status: " + mContext.getString(R.string.available));
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mStatus.setText(provider + " status: "
                        + mContext.getString(R.string.temporarily_unavailable));
                break;
        }
    }

    /** Called when the location changed */
    @Override
    public void refresh() {
        mGeoFix = mGeoFixProvider.getLocation();
        // Log.d("TreasureHunter", "GpsStatusWidget onLocationChanged " +
        // mGeoFix);
        /*
         * if (!mGeoFixProvider.isProviderEnabled()) {
         * mTextLagUpdater.setDisabled(); return; }
         */
        mProvider.setText(mGeoFix.getProvider());
        mAccuracyView.setText(mDistanceFormatter.formatDistance(mGeoFix.getAccuracy()));
        mLagTextView.setText(mGeoFix.getLagString(mClock.getCurrentTime()));
    }

    public void updateLagText(long systemTime) {
        mLagTextView.setText(mGeoFix.getLagString(systemTime));
    }

    @Override
    public void forceRefresh() {
        refresh();
    }
}
