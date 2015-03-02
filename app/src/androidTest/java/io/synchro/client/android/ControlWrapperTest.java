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

    public void testGetColorByRRGGBB()
    {
        ControlWrapper.ColorARGB color = ControlWrapper.getColor("#FFDEAD");
        assertEquals((byte) 0xFF, color.getA());
        assertEquals((byte) 0xFF, color.getR());
        assertEquals((byte) 0xDE, color.getG());
        assertEquals((byte) 0xAD, color.getB());
    }

    public void testGetColorByAARRGGBB()
    {
        ControlWrapper.ColorARGB color = ControlWrapper.getColor("#80FFDEAD");
        assertEquals((byte) 0x80, color.getA());
        assertEquals((byte) 0xFF, color.getR());
        assertEquals((byte) 0xDE, color.getG());
        assertEquals((byte) 0xAD, color.getB());
    }

    public void testStarWithStarOnly()
    {
        int stars = ControlWrapper.GetStarCount("*");
        assertEquals(1, stars);
    }

    public void testStarWithNumStar()
    {
        int stars = ControlWrapper.GetStarCount("69*");
        assertEquals(69, stars);
    }

    public void testStarWithNum()
    {
        int stars = ControlWrapper.GetStarCount("69");
        assertEquals(0, stars);
    }

    public void testStarWithEmpty()
    {
        int stars = ControlWrapper.GetStarCount("");
        assertEquals(0, stars);
    }

    public void testStarWithNull()
    {
        int stars = ControlWrapper.GetStarCount(null);
        assertEquals(0, stars);
    }
}
