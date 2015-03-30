package io.synchro.client.android.controls;

import android.text.Editable;
import android.text.TextWatcher;
//import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.IGetViewValue;
import io.synchro.client.android.ISetViewValue;
import io.synchro.client.android.JObject;
import io.synchro.client.android.JToken;
import io.synchro.client.android.JValue;

/**
 * Created by blake on 3/17/15.
 */
public class AndroidTextBoxWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidTextBoxWrapper.class.getSimpleName();

    boolean _updateOnChange = false;

    public AndroidTextBoxWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                )
    {
        super(parent, bindingContext);

        // !!! value can be null below
//        Log.d(TAG, String.format("Creating text box element with value of: %s",controlSpec.get("value").asString()));

        final EditText editText = new EditText(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = editText;

        if (controlSpec.get("control").asString().equals("password"))
        {
            // You have to tell it it's text (in addition to password) or the password doesn't work...
            editText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        }

        applyFrameworkElementDefaults(editText);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "value", null);
        if (!processElementBoundValue("value", bindingSpec.get("value").asString(), new IGetViewValue()
                                      {
                                          @Override
                                          public JToken GetViewValue()
                                          {
                                              return new JValue(editText.getText().toString());
                                          }
                                      }, new ISetViewValue()
                                      {
                                          @Override
                                          public void SetViewValue(JToken value)
                                          {
                                              editText.setText(ToString(value, ""));
                                          }
                                      }))
        {
            processElementProperty(controlSpec.get("value"), new ISetViewValue()
                                   {
                                       @Override
                                       public void SetViewValue(JToken value)
                                       {
                                           editText.setText(ToString(value, ""));
                                       }
                                   });
        }

        // !!! sync can be null
        if (bindingSpec.get("sync") != null)
        {
            if (bindingSpec.get("sync").asString().equals("change"))
            {
                _updateOnChange = true;
            }
        }

        processElementProperty(controlSpec.get("placeholder"), new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       editText.setHint(ToString(value, ""));
                                   }
                               });

        editText.addTextChangedListener(new TextWatcher()
                                        {
                                            @Override
                                            public void beforeTextChanged(
                                                    CharSequence s, int start, int count, int after
                                                                         )
                                            {

                                            }

                                            @Override
                                            public void onTextChanged(
                                                    CharSequence s, int start, int before, int count
                                                                     )
                                            {
                                                // Edit controls have a bad habit of posting a text changed event, and there are cases where
                                                // this event is generated based on programmatic setting of text and comes in asynchronously
                                                // after that programmatic action, making it difficult to distinguish actual user changes.
                                                // This shortcut will help a lot of the time, but there are still cases where this will be
                                                // signalled incorrectly (such as in the case where a control with focus is the target of
                                                // an update from the server), so we'll do some downstream delta checking as well, but this
                                                // check will cut down most of the chatter.
                                                //
                                                if (editText.isFocused())
                                                {
                                                    updateValueBindingForAttribute("value");
                                                    if (_updateOnChange)
                                                    {
                                                        AndroidTextBoxWrapper.this.getStateManager().sendUpdateRequestAsync();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s)
                                            {

                                            }
                                        });
    }
}