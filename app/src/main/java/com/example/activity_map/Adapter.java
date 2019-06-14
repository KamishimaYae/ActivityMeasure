package com.example.activity_map;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

public class Adapter extends RealmBaseAdapter<Schedule> {//データベースとリストビューの連携
    private static class ViewHolder{
        TextView date;
        TextView Activity1;
    }
    public Adapter(@Nullable OrderedRealmCollection<Schedule> data) {
        super(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView= LayoutInflater.from(parent.getContext()).inflate(
                    android.R.layout.simple_list_item_2,parent,false
            );
            viewHolder=new ViewHolder();
            viewHolder.date=convertView.findViewById(android.R.id.text1);
            viewHolder.Activity1=convertView.findViewById(android.R.id.text2);

            convertView.setTag(viewHolder);
        }else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        Schedule schedule=adapterData.get(position);
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss  EEE/dd/MM");
        String formatDate=sdf.format(schedule.getDate());
        viewHolder.date.setText(formatDate);
        viewHolder.Activity1.setText(schedule.getActivity1()+schedule.getLocationname());//
        return convertView;
    }

}
