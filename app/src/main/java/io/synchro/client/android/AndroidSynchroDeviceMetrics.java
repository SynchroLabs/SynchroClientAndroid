package io.synchro.client.android;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Created by blake on 2/17/15.
 */
public class AndroidSynchroDeviceMetrics extends SynchroDeviceMetrics
{
    private final Activity _activity;
    private final DisplayMetrics _metrics = new DisplayMetrics();

    public AndroidSynchroDeviceMetrics(Activity activity)
    {
        super();
        _activity = activity;
        _os = "Android";
        _osName = "Android";
        _deviceName = "Android Device"; // !!! Actual device manufaturer/model would be nice

        // Galaxy S3 - DisplayMetrics{density=2.0, width=720, height=1280, scaledDensity=2.0, xdpi=304.799, ydpi=306.716}
        //             DensityDpi: Xhigh
        //
        Display display = _activity.getWindowManager().getDefaultDisplay();
        display.getMetrics(_metrics);

        // !!! This could be a little more sophisticated - for now, largish is considered a "tablet", smaller is a "phone"
        //
        // !!! Also, _widthInches and _heightInches aren't defined yet
        double screenDiagonalInches = Math.sqrt(
                Math.pow(_widthInches, 2) + Math.pow(_heightInches, 2)
                                               );
        if (screenDiagonalInches > 6.5f)
        {
            _deviceClass = SynchroDeviceClass.TABLET;
            _naturalOrientation = SynchroOrientation.LANDSCAPE;
        }
        else
        {
            _deviceClass = SynchroDeviceClass.PHONE;
            _naturalOrientation = SynchroOrientation.PORTRAIT;
        }

        if (getCurrentOrientation() == _naturalOrientation)
        {
            _widthInches = _metrics.widthPixels / _metrics.xdpi;
            _heightInches = _metrics.heightPixels / _metrics.ydpi;
            _widthDeviceUnits = _metrics.widthPixels;
            _heightDeviceUnits = _metrics.heightPixels;
        }
        else
        {
            _widthInches = _metrics.heightPixels / _metrics.xdpi;
            _heightInches = _metrics.widthPixels / _metrics.ydpi;
            // Because we are not in the natural orientation, the axes are flipped. Suppress
            // the warning, the assignments are correct.
            //noinspection SuspiciousNameCombination
            _widthDeviceUnits = _metrics.heightPixels;
            //noinspection SuspiciousNameCombination
            _heightDeviceUnits = _metrics.widthPixels;
        }

        updateScalingFactor();
    }

    @Override
    public SynchroOrientation getCurrentOrientation()
    {
        return null;
    }
}
