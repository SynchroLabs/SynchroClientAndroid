package io.synchro.client.android;

import junit.framework.TestCase;

/**
 * Created by blake on 2/27/15.
 */
public class ControlWrapperTest extends TestCase
{
    public void testGetColorByName()
    {
        ControlWrapper.ColorARGB color = ControlWrapper.getColor("NavajoWhite");
        assertEquals((byte) 0xFF, color.getA());
        assertEquals((byte) 0xFF, color.getR());
        assertEquals((byte) 0xDE, color.getG());
        assertEquals((byte) 0xAD, color.getB());
    }
}
