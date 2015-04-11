package io.synchro.client.android.controls;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.IGetViewValue;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;
import io.synchro.client.android.PropertyValue;
import io.synchro.client.android.ValueBinding;

/**
 * Created by blake on 3/29/15.
 */
public class AndroidPickerWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidPickerWrapper.class.getSimpleName();

    boolean _selectionChangingProgramatically = false;
    JToken _localSelection;

    int _lastSelectedPosition = Spinner.INVALID_POSITION;

    static String[] Commands = new String[] { CommandName.getOnSelectionChange().getAttribute() };

    public static class BindingContextListItem
    {
        BindingContext _bindingContext;
        String         _itemContent;

        public BindingContextListItem(BindingContext bindingContext, String itemContent)
        {
            _bindingContext = bindingContext;
            _itemContent = itemContent;
        }

        public BindingContext getBindingContext()
        {
            return _bindingContext;
        }

        public JToken GetValue()
        {
            return _bindingContext.Select("$data").GetValue();
        }

        public JToken GetSelection(String selectionItem)
        {
            return _bindingContext.Select(selectionItem).GetValue().deepClone();
        }

        @Override
        public String toString()
        {
            return PropertyValue.ExpandAsString(_itemContent, _bindingContext);
        }
    }

    public static class BindingContextPickerAdapter extends BaseAdapter implements SpinnerAdapter
    {
        protected Context _context;
        protected List<View> _views = new ArrayList<>();

        protected List<BindingContextListItem> _listItems = new ArrayList<>();

        public BindingContextPickerAdapter(Context context)
        {
            _context = context;
        }

        public void SetContents(BindingContext bindingContext, String itemContent)
        {
            _listItems.clear();

            List<BindingContext> itemBindingContexts = bindingContext.SelectEach("$data");
            for (BindingContext itemBindingContext : itemBindingContexts)
            {
                _listItems.add(new BindingContextListItem(itemBindingContext, itemContent));
            }
        }

        public BindingContextListItem GetItemAtPosition(int position)
        {
            return _listItems.get(position);
        }

        private View GetCustomView(int position, View convertView, ViewGroup parent, boolean dropdown)
        {
            BindingContextListItem item = _listItems.get(position);

            LayoutInflater inflater = LayoutInflater.from(_context);
            View view = (convertView != null) ? convertView : (inflater.inflate((dropdown ? android.R.layout.simple_spinner_dropdown_item : android.R.layout.simple_spinner_item), parent, false));

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            if (text != null)
                text.setText(item.toString());

            if (!_views.contains(view))
                _views.add(view);

            return view;
        }

        private void ClearViews()
        {
            for (View view : _views)
            {
//                view.Dispose();
            }
            _views.clear();
        }

//        protected override void Dispose(bool disposing)
//        {
//            ClearViews();
//            base.Dispose(disposing);
//        }

        @Override
        public int getCount()
        {
            return _listItems.size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(
                int position, View convertView, ViewGroup parent
                           )
        {
            return GetCustomView(position, convertView, parent, false);
        }

        @Override
        public View getDropDownView(
                int position, View convertView, ViewGroup parent
                                   )
        {
            return GetCustomView(position, convertView, parent, true);
        }
    }

    public AndroidPickerWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                               )
    {
        super(parent, bindingContext);

        Log.d(TAG, "Creating picker element");
        final Spinner picker = new Spinner(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = picker;

        BindingContextPickerAdapter adapter = new BindingContextPickerAdapter(((AndroidControlWrapper)parent).getControl().getContext());
        picker.setAdapter(adapter);

        applyFrameworkElementDefaults(picker);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "items", Commands);
        ProcessCommands(bindingSpec, Commands);

        if (bindingSpec.get("items") != null)
        {
            final String itemContent = (bindingSpec.get("itemContent") != null) ? bindingSpec.get("itemContent").asString() : "{$data}";

            processElementBoundValue(
                    "items",
                    bindingSpec.get("items").asString(),
                    new IGetViewValue()
                    {
                        @Override
                        public JToken GetViewValue()
                        {
                            return getPickerContents(picker);
                        }
                    },
                    new AndroidUiThreadSetViewValue((Activity) picker.getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            AndroidPickerWrapper.this.setPickerContents(
                                    picker, GetValueBinding("items").getBindingContext(),
                                    itemContent);

                        }
                    });
        }

        if (bindingSpec.get("selection") != null)
        {
            final String selectionItem = (bindingSpec.get("selectionItem") != null) ? bindingSpec.get("selectionItem").asString() : "$data";

            processElementBoundValue(
                    "selection",
                    bindingSpec.get("selection").asString(),
                    new IGetViewValue()
                    {
                        @Override
                        public JToken GetViewValue()
                        {
                            return getPickerSelection(picker, selectionItem);
                        }
                    },
                    new AndroidUiThreadSetViewValue((Activity) picker.getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            AndroidPickerWrapper.this.setPickerSelection(
                                    picker, selectionItem, value
                                                                        );
                        }
                    });
        }

        picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                                         {
                                             @Override
                                             public void onItemSelected(
                                                     AdapterView<?> parent, View view, int position,
                                                     long id
                                                                       )
                                             {
                                                 picker_ItemSelected(parent, view, position, id);
                                             }

                                             @Override
                                             public void onNothingSelected(AdapterView<?> parent)
                                             {

                                             }
                                         });
    }

    public JToken getPickerContents(Spinner picker)
    {
        Log.d(TAG, "Getting picker contents - NOOP");
        throw new UnsupportedOperationException();
    }

    public void setPickerContents(Spinner picker, BindingContext bindingContext, String itemContent)
    {
        Log.d(TAG, "Setting picker contents");

        _selectionChangingProgramatically = true;

        BindingContextPickerAdapter adapter = (BindingContextPickerAdapter)picker.getAdapter();
        adapter.SetContents(bindingContext, itemContent);
        adapter.notifyDataSetChanged();

        ValueBinding selectionBinding = GetValueBinding("selection");
        if (selectionBinding != null)
        {
            selectionBinding.UpdateViewFromViewModel();
        }
        else if (_localSelection != null)
        {
            // If there is not a "selection" value binding, then we use local selection state to restore the selection when
            // re-filling the list.
            //
            this.setPickerSelection(picker, "$data", _localSelection);
        }

        _selectionChangingProgramatically = false;
    }

    public JToken getPickerSelection(Spinner picker, String selectionItem)
    {
        BindingContextPickerAdapter adapter = (BindingContextPickerAdapter)picker.getAdapter();
        if (picker.getSelectedItemPosition() >= 0)
        {
            BindingContextListItem item = adapter.GetItemAtPosition(picker.getSelectedItemPosition());
            return item.GetSelection(selectionItem);
        }
        return new JValue(false); // This is a "null" selection
    }

    public void setPickerSelection(Spinner picker, String selectionItem, JToken selection)
    {
        _selectionChangingProgramatically = true;

        BindingContextPickerAdapter adapter = (BindingContextPickerAdapter)picker.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++)
        {
            BindingContextListItem item = adapter.GetItemAtPosition(i);
            if (selection.equals(item.GetSelection(selectionItem)))
            {
                _lastSelectedPosition = i;
                picker.setSelection(i);
                break;
            }
        }

        _selectionChangingProgramatically = false;
    }

    void picker_ItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(TAG, "Picker selection changed");
        Spinner picker = (Spinner)parent;

        ValueBinding selectionBinding = GetValueBinding("selection");
        if (selectionBinding != null)
        {
            updateValueBindingForAttribute("selection");
        }
        else if (!_selectionChangingProgramatically)
        {
            _localSelection = this.getPickerSelection(picker, "$data");
        }

        // ItemSelected gets called (at least) once during construction of the view.  In order to distinguish this
        // call from an actual user click we will test to see of the new selected item is different than the last
        // one we set programatically.
        //
        if ((!_selectionChangingProgramatically) && (_lastSelectedPosition != position))
        {
            _lastSelectedPosition = position;
            CommandInstance command = GetCommand(CommandName.getOnSelectionChange());
            if (command != null)
            {
                Log.d(TAG, String.format("Picker item click with command: %s", command.getCommand()));

                // The item click command handler resolves its tokens relative to the item clicked (not the list view).
                //
                BindingContextPickerAdapter adapter = (BindingContextPickerAdapter)picker.getAdapter();
                BindingContextListItem listItem = adapter.GetItemAtPosition(position);
                getStateManager().sendCommandRequestAsync(command.getCommand(), command.GetResolvedParameters(listItem.getBindingContext()));
            }
        }
    }}
