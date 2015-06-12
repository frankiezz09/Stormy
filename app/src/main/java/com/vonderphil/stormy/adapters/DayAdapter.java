package com.vonderphil.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vonderphil.stormy.R;
import com.vonderphil.stormy.weather.Day;

/**
 * Created by Administrator on 6/5/2015.
 */
public class DayAdapter extends BaseAdapter {

    //Needs the context from where this class is called
    private Context mContext;
    private Day[] mDays;

    //Constructor to set the values
    public DayAdapter(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0; // we aren't going to use this. Tag items for easy reference.
    }

    //Gets called for each item on the list.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Best practice method container. See below method.
        ViewHolder holder;

        //convertView is the view object that we want to reuse. The first time getView is called, convertView is null. We need to create it if it is not null.
        //If it is not null, we have a view already available for use. All that we have to do is to reset the data.
        if (convertView == null) {
            //brand new
            //LayoutInflater is an Android object that takes XML layouts and turns them into views in code that we can use. We can get them from context then call the inflate method to find the custom layout. Null is a view group and is not needed.
            convertView = LayoutInflater.from(mContext).inflate((R.layout.daily_list_item), null);
            //Construct the ViewHolder object.
            holder = new ViewHolder();
            //Get inflated data from convertView.
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);
            //Sets the tag that we can reuse later.
            convertView.setTag(holder);
        }

        else {
            holder = (ViewHolder) convertView.getTag();
        }
        //position variable is the first parameter of the getView()
        Day day = mDays[position];

        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(day.getTemperatureMax() + "");

        if(position == 0) {
            holder.dayLabel.setText("Today");
        }
        else {
            holder.dayLabel.setText(day.getDayOfTheWeek());
        }

        return convertView;
    }

    //Contains all the element inside the list to store the data.
    private static class ViewHolder {
        ImageView iconImageView; //public by default. cause static.
        TextView temperatureLabel;
        TextView dayLabel;
    }
}
