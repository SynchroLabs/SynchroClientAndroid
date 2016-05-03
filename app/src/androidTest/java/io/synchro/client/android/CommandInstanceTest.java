package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

import io.synchro.json.JObject;
import io.synchro.json.JTokenType;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/15/16.
 */
public class CommandInstanceTest extends TestCase
{
    JObject viewModel;

    public void setUp()
            throws IOException
    {
        viewModel = (JObject) JObject.parse(
                "{" +
                        "\"serial\" : 69," +
                        "\"title\" : \"The Title\"," +
                        "\"board\" :" +
                            "[" +
                            "[ \"s00\", \"s01\" ]," +
                            "[ \"s10\", \"s11\" ]," +
                            "]" +
                        "}");
    }

    public void testResolveParameters()
    {
        CommandInstance cmdInst = new CommandInstance("TestCmd");
        BindingContext bindingCtx = new BindingContext(viewModel);

        cmdInst.SetParameter("Literal", new JValue("literal"));
        cmdInst.SetParameter("Serial", new JValue("{serial}"));
        cmdInst.SetParameter("Title", new JValue("{title}"));
        cmdInst.SetParameter("Empty", new JValue(""));
        cmdInst.SetParameter("Obj", new JValue("{board[0][1]}"));
        cmdInst.SetParameter("NULL", new JValue((String) null));          // This can't happen in nature, but just for fun...
        cmdInst.SetParameter("Parent", new JValue("{$parent}")); // Token that can't be resolved ($parent from root)
        cmdInst.SetParameter("Nonsense", new JValue("{foo}"));   // Token that can't be resolved

        JObject resolvedParams = cmdInst.GetResolvedParameters(bindingCtx);
        assertEquals("literal", resolvedParams.get("Literal").asString());
        assertEquals(69, resolvedParams.get("Serial").asLong());
        assertEquals("The Title", resolvedParams.get("Title").asString());
        assertEquals("", resolvedParams.get("Empty").asString());
        assertEquals(resolvedParams.get("Obj"), viewModel.get("board").selectToken("0", false).selectToken("1", false));
        assertEquals(JTokenType.Null, resolvedParams.get("NULL").getType());
        assertEquals(JTokenType.Null, resolvedParams.get("Parent").getType());
        assertEquals(JTokenType.Null, resolvedParams.get("Nonsense").getType());
    }
}
