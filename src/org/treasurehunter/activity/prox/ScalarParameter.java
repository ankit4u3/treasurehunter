/**
 * 
 */

package org.treasurehunter.activity.prox;

import android.util.Log;

class ScalarParameter extends Parameter {

    public ScalarParameter(double accelConst, double attenuation, double init) {
        super(accelConst, attenuation, init);
    }

    public ScalarParameter(double accelConst, double attenuation) {
        super(accelConst, attenuation);
    }

    public ScalarParameter(double init) {
        super(init);
    }

    public ScalarParameter() {
        super();
    }

    @Override
    public void update(double deltaSec) {
        if (!mIsInited || (mTargetValue == mValue && mChangePerSec == 0))
            return;
        // First update mChangePerSec, then update mValue
        // There is an ideal mChangePerSec for every distance. Move towards it.
        double distance = mTargetValue - mValue;
        if (distance <= 15) {
            Log.d("Proximity is reached ", "Fire the Questions");

        }
        double accel = distance * mAccelConst;
        mValue += mChangePerSec * deltaSec + accel * deltaSec * deltaSec / 2.0;
        mChangePerSec += accel * deltaSec;
        mChangePerSec *= Math.pow(mAttenuation, deltaSec);
    }
}
