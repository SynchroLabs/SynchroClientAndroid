package io.synchro.client.android;

/**
 * Created by blake on 2/15/15.
 */
public abstract class SynchroDeviceMetrics
{
    protected SynchroDeviceClass _deviceClass = SynchroDeviceClass.PHONE;

    protected SynchroOrientation _naturalOrientation = SynchroOrientation.PORTRAIT;

    protected String _os = "Unknown"; // Short name for filtering, ie: Windows, WinPhone, iOS, Android
    protected String _osName = "Unknown";
    // !!! OS version would be nice

    protected String _deviceName = "Unknown";

    protected double _widthInches = 0;
    protected double _heightInches = 0;

    protected double _widthDeviceUnits = 0;
    protected double _heightDeviceUnits = 0;
    protected double _deviceScalingFactor = 1;

    protected double _scalingFactor = 1;

    protected String _clientName;
    protected String _clientVersion;

//    public SynchroDeviceMetrics()
//    {
//    }

    public String getClientName() { return _clientName; }
    public String getClientVersion() { return _clientVersion; }
    // Device details
    //
    public String getOS() { return _os; }
    public String getOSName() { return _osName; }
    public String getDeviceName() { return _deviceName; }

    // Device type
    //
    public SynchroDeviceClass getDeviceClass() { return _deviceClass; }
    public SynchroDeviceType getDeviceType()
    {
        return ((_deviceClass == SynchroDeviceClass.PHONE) || (_deviceClass == SynchroDeviceClass.PHABLET)) ? SynchroDeviceType.PHONE : SynchroDeviceType.TABLET;
    }

    public SynchroOrientation getNaturalOrientation() { return _naturalOrientation; }

    public abstract SynchroOrientation getCurrentOrientation();

    // Physical dimensions of device
    //
    public double getWidthInches() { return _widthInches; }
    public double getHeightInches() { return _heightInches; }

    // Logical dimensions of device
    //
    // "Device Units" is a general term to describe whatever units are used to position and size objects in the target environment.
    // In iOS this unit is the "point" (a term Apple uses, not to be confused with a typographic point).  In Android, this unit is
    // actually the physical pixel value.  In WinPhone this is the "view pixels" value (a virtual coordinate space).
    //
    public double getWidthDeviceUnits() { return _widthDeviceUnits; }
    public double getHeightDeviceUnits() { return _heightDeviceUnits; }

    // Device scaling factor is the ratio of device units to physical pixels.  This can be used to determine an appropriately sized
    // image resource, for example.
    //
    public double getDeviceScalingFactor() { return _deviceScalingFactor; }

    // Dimensions of device
    //
    public double getWidthUnits() { return _widthDeviceUnits / _scalingFactor; }
    public double getHeightUnits() { return _heightDeviceUnits / _scalingFactor; }

    // Scaling factor is the ratio of logic units to device units.
    //
    public double getScalingFactor() { return _scalingFactor; }

    // Coordinate space mapping
    //
    // Note: In the explanations below, all units attributed to a device or OS are "device units" (meaning whatever unit
    // coordinate/size metric is used on the device).  These are typically scaled or transformed in some way by the device
    // operating system to map to the underlying display pixels (and will in fact be scaled on most contemporary devices,
    // which will have displays with significantly higher actual native pixel resolutions).
    //
    // For "phone-like" (portrait-first) devices we will scale the display to be 480 Maaas units wide, and maintain the device
    // aspect ratio - meaning that the height in Maaas units will vary from 720 (3.5" iPhone/iPod) to 853 (16:9 Win/Android
    // phone).  This will work well as the Windows phones are already 480 logical units wide, and the iOS devices are
    // 320 (so it's a simple 1.5x transform).  The Android devices will use pixel widths, but will typically be a pretty
    // clean transform (the screens will tend to be 480, 720, or 1080 pixels).
    //
    // For "tablet-like" (landscape-first) devices we will scale the display to be 768 Maaas units tall, and maintain the device
    // aspect ratio - meaning that the width in Maaas units will vary from 1024 (iPad/iPad Mini) to 1368 (Surface), with other
    // tablets falling somewhere in this range.  This means we will not need to do any scaling on iOS or Windows, and that the
    // android transforms will be fairly clean.
    //
    // Note: Every device currently in existence has square pixels, so we don't need to track h/v scale independently.
    //
    protected void updateScalingFactor() // Call from derived constructor after device units set
    {
        if (this.getDeviceType() == SynchroDeviceType.PHONE)
        {
            _scalingFactor = _widthDeviceUnits / 480;
        }
        else
        {
            // On Windows devices, the device units are scaled, and sometimes due to rounding/multiplication errors, report
            // device unit sizes slightly different than the actual size.  So if we're in the ballpark, we just won't scale.
            //
            if (Math.abs(_heightDeviceUnits - 768) < 5)
            {
                _scalingFactor = 1;
            }
            else
            {
                _scalingFactor = _heightDeviceUnits / 768;
            }
        }
    }

    public double MaaasUnitsToDeviceUnits(double maaasUnits)
    {
        return maaasUnits * _scalingFactor;
    }

    // Font scaling - to convert font points (typographic points) to Maaas units, we need to normalize for all "phone" types
    // using a theoritical model phone with "average" dimensions.  The idea is that on all phone devices, fonts of a given
    // size should take up about the same relative amount of screen real estate (so that layouts will scale).
    //
    //     Model phone
    //     ===============================
    //     Screen size: 4.25"
    //     Aspect: 480x800 units (assume these are Maaas units)
    //     Diagnonal units (932.95) / Screen size in inches (4.25") = 219.52 units/inch
    //
    //     Since 72 (typographic points per inch) times 3 = 216, which is very close to the computed value above,
    //     we're just going to use a factor of 3x to convert from typographic points to Maaas units (this will also
    //     make it easy for Maaas UX designers to understand the relationship of typographic points to Maaas units).
    //
    public double TypographicPointsToMaaasUnits(double points)
    {
        // Convert typographic point values (72pt/inch) to Maaas units (219.52units/inch on model phone)
        //
        return points * 3;
    }
}
