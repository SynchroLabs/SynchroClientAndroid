package io.synchro.client.android;

import junit.framework.TestCase;

import io.synchro.client.android.controls.AndroidControlWrapper;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 12/15/15.
 */
public class AndroidControlWrapperTest extends TestCase
{
    private JObject viewModelObj;
    private StateManager stateManager = null;
    private AndroidPageView pageView = null;

    class TestFontSetter implements ControlWrapper.IFontSetter
    {
        public boolean                     bold     = false;
        public ControlWrapper.FontFaceType faceType = ControlWrapper.FontFaceType.FONT_DEFAULT;
        public boolean                     italic   = false;
        public double                      size     = 12.0f;

        @Override
        public void SetFaceType(ControlWrapper.FontFaceType faceType)
        {
            this.faceType = faceType;
        }

        @Override
        public void SetSize(double size)
        {
            this.size = size;
        }

        @Override
        public void SetBold(boolean bold)
        {
            this.bold = bold;
        }

        @Override
        public void SetItalic(boolean italic)
        {
            this.italic = italic;
        }
    }

    class TestThickness extends AndroidControlWrapper.ThicknessSetter
    {
        public int left;
        public int top;
        public int right;
        public int bottom;

        @Override
        public void SetThicknessLeft(int thickness)
        {
            left = thickness;
        }

        @Override
        public void SetThicknessTop(int thickness)
        {
            top = thickness;
        }

        @Override
        public void SetThicknessRight(int thickness)
        {
            right = thickness;
        }

        @Override
        public void SetThicknessBottom(int thickness)
        {
            bottom = thickness;
        }
    }

    class AndroidTestControlWrapper extends AndroidControlWrapper
    {
        public String attr1;
        public String attr2;
        public TestThickness thickness = new TestThickness();
        public TestFontSetter fontSetter = new TestFontSetter();

        public AndroidTestControlWrapper(ControlWrapper parent, BindingContext bindingContext, JObject controlSpec)
        {
            super(parent, bindingContext, controlSpec);
            processElementProperty(
                    controlSpec, "attr1", new ISetViewValue()
                    {
                        @Override
                        public void SetViewValue(JToken value)
                        {
                            attr1 = ToString(value, "");
                        }
                    }
                                  );
            processElementProperty(
                    controlSpec, "attr2", new ISetViewValue()
                    {
                        @Override
                        public void SetViewValue(JToken value)
                        {
                            attr2 = ToString(value, "");
                        }
                    }
                                  );
            processThicknessProperty(
                    controlSpec, "thicknessAttr", thickness);
            processFontAttribute(controlSpec, fontSetter);
        }
    }

    public void setUp()
    {
        viewModelObj = new JObject();
        viewModelObj.put("num", new JValue(1));
        viewModelObj.put("str", new JValue("Words words words"));

        {
            JObject style1Object = new JObject();

            style1Object.put("attr1", new JValue("attr1fromStyle1"));

            {
                JObject thicknessObject = new JObject();

                thicknessObject.put("bottom", new JValue(9));

                style1Object.put("thicknessAttr", thicknessObject);
            }

            {
                JObject fontObject = new JObject();

                fontObject.put("face", new JValue("SanSerif"));
                fontObject.put("bold", new JValue(true));
                fontObject.put("italic", new JValue(true));

                style1Object.put("font", fontObject);
            }

            style1Object.put("fontsize", new JValue(24));

            viewModelObj.put("testStyle1", style1Object);
        }

        {
            JObject style2Object = new JObject();

            style2Object.put("attr1", new JValue("attr1fromStyle2"));
            style2Object.put("attr2", new JValue("attr2fromStyle2"));
            style2Object.put("thicknessAttr", new JValue(10));

            {
                JObject fontObject = new JObject();

                fontObject.put("size", new JValue(26));

                style2Object.put("font", fontObject);
            }

            viewModelObj.put("testStyle2", style2Object);
        }

    }

    public void testStyleExplicitNoStyle()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("attr1", new JValue("attr1val"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals("attr1val", testControl.attr1);
        assertEquals(null, testControl.attr2);
    }

    public void testStyleExplicitWithStyle()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle1"));
        controlSpec.put("attr1", new JValue("attr1val"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals("attr1val", testControl.attr1);
        assertEquals(null, testControl.attr2);
    }

    public void testStyleFromStyle()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle1"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals("attr1fromStyle1", testControl.attr1);
        assertEquals(null, testControl.attr2);
    }

    public void testStyleFromStyles()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle1, testStyle2"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals("attr1fromStyle1", testControl.attr1);
        assertEquals("attr2fromStyle2", testControl.attr2);
    }

    public void testStyleFromStylesPriority()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle2, testStyle1"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals("attr1fromStyle2", testControl.attr1);
        assertEquals("attr2fromStyle2", testControl.attr2);
    }

    public void testStyleExplicitThicknessNoStyle()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("thicknessAttr", new JValue(5));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(5, testControl.thickness.top);
        assertEquals(5, testControl.thickness.left);
        assertEquals(5, testControl.thickness.bottom);
        assertEquals(5, testControl.thickness.right);
    }

    public void testStyleExplicitThicknessObjNoStyle()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        {
            JObject thicknessObject = new JObject();

            thicknessObject.put("top", new JValue(5));
            thicknessObject.put("left", new JValue(6));
            thicknessObject.put("bottom", new JValue(7));
            thicknessObject.put("right", new JValue(8));

            controlSpec.put("thicknessAttr", thicknessObject);
        }
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(5, testControl.thickness.top);
        assertEquals(6, testControl.thickness.left);
        assertEquals(7, testControl.thickness.bottom);
        assertEquals(8, testControl.thickness.right);
    }

    public void testStyleExplicitThicknessObjAndStyles()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle1, testStyle2"));
        {
            JObject thicknessObject = new JObject();

            thicknessObject.put("top", new JValue(5));
            thicknessObject.put("left", new JValue(6));

            controlSpec.put("thicknessAttr", thicknessObject);
        }
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(5, testControl.thickness.top);
        assertEquals(6, testControl.thickness.left);
        assertEquals(9, testControl.thickness.bottom);
        assertEquals(10, testControl.thickness.right);
    }

    public void testStyleExplicitFontSize()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("fontsize", new JValue(20));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(20.0, testControl.fontSetter.size);
    }

    public void testStyleExplicitFontSizeFromObject()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        {
            JObject fontObject = new JObject();

            fontObject.put("size", new JValue(22));

            controlSpec.put("font", fontObject);
        }
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(22.0, testControl.fontSetter.size);
    }

    public void testStyleFontFromStyle()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle1, testStyle2"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(24.0, testControl.fontSetter.size);
        assertEquals(true, testControl.fontSetter.bold);
        assertEquals(true, testControl.fontSetter.italic);
        assertEquals(ControlWrapper.FontFaceType.FONT_SANSERIF, testControl.fontSetter.faceType);
    }

    public void testStyleFontFromStylePriority()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle2, testStyle1"));
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(26.0, testControl.fontSetter.size);
        assertEquals(true, testControl.fontSetter.bold);
        assertEquals(true, testControl.fontSetter.italic);
        assertEquals(ControlWrapper.FontFaceType.FONT_SANSERIF, testControl.fontSetter.faceType);
    }

    public void testStyleFontFromStyleExplicitOverride()
    {
        ViewModel viewModel = new ViewModel();
        viewModel.InitializeViewModelData(viewModelObj);

        AndroidControlWrapper rootControl = new AndroidControlWrapper(pageView, stateManager, viewModel, viewModel.getRootBindingContext(), null);

        JObject controlSpec = new JObject();
        controlSpec.put("style", new JValue("testStyle1"));
        {
            JObject fontObject = new JObject();

            fontObject.put("size", new JValue(28));
            fontObject.put("italic", new JValue(false));

            controlSpec.put("font", fontObject);
        }
        AndroidTestControlWrapper testControl = new AndroidTestControlWrapper(rootControl, rootControl.getBindingContext(), controlSpec);
        assertEquals(28.0, testControl.fontSetter.size);
        assertEquals(true, testControl.fontSetter.bold);
        assertEquals(false, testControl.fontSetter.italic);
        assertEquals(ControlWrapper.FontFaceType.FONT_SANSERIF, testControl.fontSetter.faceType);
    }
}
