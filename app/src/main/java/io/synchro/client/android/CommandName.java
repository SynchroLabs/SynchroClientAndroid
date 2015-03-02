package io.synchro.client.android;

/**
 * Created by blake on 3/1/15.
 */
public class CommandName
{
    private String _attribute;

    private CommandName(String attribute)
    {
        _attribute = attribute;
    }

    public String getAttribute()
    {
        return _attribute;
    }

    @Override
    public String toString()
    {
        return _attribute;
    }

    public static CommandName getOnClick()
    {
        return new CommandName("onClick");
    }
    public static CommandName getOnItemClick()
    {
        return new CommandName("onItemClick");
    }
    public static CommandName getOnSelectionChange()
    {
        return new CommandName("onSelectionChange");
    }
    public static CommandName getOnToggle()
    {
        return new CommandName("onToggle");
    }
    public static CommandName getOnUpdate() {
        return new CommandName("onUpdate");
    }
}
