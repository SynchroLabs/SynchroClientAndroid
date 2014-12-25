package io.synchro.client.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by blake on 12/24/14.
 */
// http://stackoverflow.com/questions/2109271/can-any-one-provide-me-example-of-two-line-list-item-in-android
public abstract class TwoLineArrayAdapter<T> extends ArrayAdapter<T>
{
    private int mListItemLayoutResId;

    public TwoLineArrayAdapter(Context context, T[] ts)
    {
        this(context, android.R.layout.two_line_list_item, ts);
    }

    public TwoLineArrayAdapter(
            Context context,
            int listItemLayoutResourceId,
            T[] ts)
    {
        super(context, listItemLayoutResourceId, ts);
        mListItemLayoutResId = listItemLayoutResourceId;
    }

    @Override
    public android.view.View getView(
            int position,
            View convertView,
            ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater)getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItemView = convertView;
        if (null == convertView)
        {
            listItemView = inflater.inflate(
                    mListItemLayoutResId,
                    parent,
                    false);
        }

        // The ListItemLayout must use the standard text item IDs.
        TextView lineOneView = (TextView)listItemView.findViewById(
                android.R.id.text1);
        TextView lineTwoView = (TextView)listItemView.findViewById(
                android.R.id.text2);

        T t = getItem(position);
        lineOneView.setText(getLineOneText(t));
        lineTwoView.setText(getLineTwoText(t));

        return listItemView;
    }

    public abstract String getLineOneText(T t);

    public abstract String getLineTwoText(T t);
}
