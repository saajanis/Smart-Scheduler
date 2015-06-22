package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by apoorva on 4/4/15.
 */
public class MyAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public MyAdapter(Context context, List<String> values) {
        super(context, R.layout.list_item_event, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_event, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.event_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.event_logo);
        textView.setText(values.get(position));

        String s = values.get(position);
        System.out.println("Position: " + position);
        ListItem selectedItem = null;
        String currentStatus = null;

        if (EventActivity.ListItemsListGlobal!=null) {
            if (EventActivity.ListItemsListGlobal.size() > position) {
                selectedItem = EventActivity.ListItemsListGlobal.get(position);
                currentStatus = selectedItem.getStatus();
                System.out.println("Current Status: "+currentStatus);
            }
        }
        //if s== "something"
        if (currentStatus!=null) {
            if (currentStatus.equals("N/A")) {
                imageView.setImageResource(R.drawable.question_mark_icon);
            }
            else  if (currentStatus.equals("new")) {
                imageView.setImageResource(R.drawable.dot_icon);//new request, yet to accept
            }
            else if (currentStatus.equals("accepted")) {
                imageView.setImageResource(R.drawable.check_mark_icon);//new request, yet to accept
            }
            else if (currentStatus.equals("declined")) {
                imageView.setImageResource(R.drawable.cross_mark_icon);//new request, yet to accept
            }
            else if (currentStatus.equals("scheduled")) {
                imageView.setImageResource(R.drawable.doublecheckmarkicon);//all participants have accepted
            }


        }


        // if null then default
        else if (position==1){
            imageView.setImageResource(R.drawable.question_mark_icon);//accepted, not scheduled yet
        }
        else if (position==2){
            imageView.setImageResource(R.drawable.check_mark_icon);//scheduled
        }
        else if (position==3){
            imageView.setImageResource(R.drawable.cross_mark_icon);//declined
        }
        return rowView;
    }
}
