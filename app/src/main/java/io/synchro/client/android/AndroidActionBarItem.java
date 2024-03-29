package io.synchro.client.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;

import java.lang.reflect.Field;

import io.synchro.client.android.controls.AndroidControlWrapper;

/**
 * Created by blake on 3/9/15.
 */
public class AndroidActionBarItem
{
    public static final String TAG = AndroidActionBarItem.class.getSimpleName();
    protected Context _context;

    protected String   _title;
    protected String   _iconName;
    protected int      _iconResourceId;
    protected Drawable _icon;
    protected Drawable _iconDisabled;
    protected boolean _enabled = true;
    protected IOnItemSelected _onItemSelected;
    protected int _showAsAction = MenuItem.SHOW_AS_ACTION_NEVER;  // [Always, Never, IfRoom] | WithText
    protected MenuItem _menuItem;

    public AndroidActionBarItem(Context context)
    {
        _context = context;
    }

    public String getTitle()
    {
        return _title;
    }
    public void setTitle(String value)
    {
        _title = value;
    }

    public int getShowAsAction()
    {
        return _showAsAction;
    }
    public void setShowAsAction(int value)
    {
        _showAsAction = value;
        if (_menuItem != null)
        {
            _menuItem.setShowAsAction(_showAsAction);
        }
    }

    public interface IOnItemSelected
    {
        public void OnItemSelected();
    }

    protected void updateIconOnMenuItem()
    {
        if ((_menuItem != null) && (_icon != null))
        {
            if (_enabled)
            {
                if (_menuItem.getIcon() != _icon)
                {
                    _menuItem.setIcon(_icon);
                }
            }
            else
            {
                if (_iconDisabled == null)
                {
                    // !!! This is probably not the best way to show the icon as disabled, but it works for now...
                    _iconDisabled = AndroidControlWrapper.getIconDrawable(_context, _iconName);
                    _iconDisabled.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                }

                if (_menuItem.getIcon() != _iconDisabled)
                {
                    _menuItem.setIcon(_iconDisabled);
                }
            }
        }
    }

    public String getIcon()
    {
        return _iconName;
    }

    public void setIcon(String value)
    {
        Log.d(TAG, String.format("setIcon(\"%s\")", value));
        _iconName = value;
        _icon = AndroidControlWrapper.getIconDrawable(_context, _iconName);
        updateIconOnMenuItem();
    }

    public boolean isEnabled()
    {
        return _enabled;
    }

    public void setEnabled(boolean value)
    {
        _enabled = value;
        if (_menuItem != null)
        {
            updateIconOnMenuItem();
            _menuItem.setEnabled(_enabled);
        }
    }

    public IOnItemSelected getOnItemSelected()
    {
        return _onItemSelected;
    }

    public void setOnItemSelected(IOnItemSelected value)
    {
        _onItemSelected = value;
    }

    public MenuItem getMenuItem()
    {
        return _menuItem;
    }
    public void setMenuItem(MenuItem value)
    {
        _menuItem = value;
        _menuItem.setShowAsAction(_showAsAction);
        updateIconOnMenuItem();
        _menuItem.setEnabled(_enabled);
    }
}