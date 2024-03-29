package io.synchro.client.android;

import android.test.AndroidTestCase;

/**
 * Created by blake on 10/12/15.
 */
public class SynchroDeviceMetricsTest extends AndroidTestCase
{
    public void testClientNameClientVersion()
    {
        AndroidSynchroDeviceMetrics androidSynchroDeviceMetrics = new AndroidSynchroDeviceMetrics(getContext());

        assertEquals("Synchro Explorer", androidSynchroDeviceMetrics.getClientName());
        assertEquals("1.4.1", androidSynchroDeviceMetrics.getClientVersion());
    }
}
