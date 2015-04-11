package io.synchro.client.android;

import junit.framework.TestCase;

import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 1/31/15.
 */
public class BindingTest extends TestCase
{
    public void testBindingHelperPromoteValue()
    {
        // For an edit control with a default binding attribute of "value" a binding of:
        //
        //     binding: "username"
        //
        JObject controlSpec = new JObject();
        controlSpec.put("binding", new JValue("username"));

        // becomes
        //
        //     binding: { value: "username" }
        //
        JObject expectedBindingSpec = new JObject();
        expectedBindingSpec.put("value", new JValue("username"));

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "value", null);
        assertEquals(expectedBindingSpec, bindingSpec);
    }

    public void testBindingHelperPromoteImplicitCommand()
    {
        // For commands:
        //
        //     binding: "doSomething"
        //
        JObject controlSpec = new JObject();
        controlSpec.put("binding", new JValue("doSomething"));

        // becomes
        //
        //     binding: { onClick: "doSomething" }
        //
        // becomes
        //
        //     binding: { onClick: { command: "doSomething" } }
        //
        JObject expectedBindingSpec = new JObject();
        JObject innerCommand = new JObject();

        innerCommand.put("command", new JValue("doSomething"));
        expectedBindingSpec.put("onClick", innerCommand);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "onClick", new String[]{"onClick"});
        assertEquals(expectedBindingSpec, bindingSpec);
    }

    public void testBindingHelperPromoteExplicitCommand()
    {
        // Also (default binding atttribute is 'onClick', which is also in command attributes list):
        //
        //     binding: { command: "doSomething" value: "theValue" }
        //
        JObject controlSpec = new JObject();
        JObject innerControlSpec = new JObject();

        innerControlSpec.put("command", new JValue("doSomething"));
        innerControlSpec.put("value", new JValue("theValue"));
        controlSpec.put("binding", innerControlSpec);

        // becomes
        //
        //     binding: { onClick: { command: "doSomething", value: "theValue" } }
        //
        JObject expectedBindingSpec = new JObject();
        JObject innerCommand = new JObject();

        innerCommand.put("command", new JValue("doSomething"));
        innerCommand.put("value", new JValue("theValue"));
        expectedBindingSpec.put("onClick", innerCommand);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "onClick", new String[]{"onClick"});
        assertEquals(expectedBindingSpec, bindingSpec);
    }

    public void testBindingHelperPromoteMultipleCommands()
    {
        // For multiple commands with implicit values...
        //
        //     binding: { onClick: "doClickCommand", onSelect: "doSelectCommand" }
        //
        JObject controlSpec = new JObject();
        JObject innerControlSpec = new JObject();

        innerControlSpec.put("onClick", new JValue("doClickCommand"));
        innerControlSpec.put("onSelect", new JValue("doSelectCommand"));
        controlSpec.put("binding", innerControlSpec);

        // becomes
        //
        //     binding: { onClick: { command: "doClickCommand" }, onSelect: { command: "doSelectCommand" } }
        //
        JObject expectedBindingSpec = new JObject();

        JObject innerCommand = new JObject();
        innerCommand.put("command", new JValue("doClickCommand"));
        expectedBindingSpec.put("onClick", innerCommand);

        innerCommand = new JObject();
        innerCommand.put("command", new JValue("doSelectCommand"));
        expectedBindingSpec.put("onSelect", innerCommand);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "onClick", new String[]{"onClick", "onSelect"});
        assertEquals(expectedBindingSpec, bindingSpec);
    }
}
