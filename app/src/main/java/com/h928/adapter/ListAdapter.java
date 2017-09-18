package com.h928.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.h928.view.R;

import java.util.List;
import java.util.Map;

/**
 * Created by xiechunping on 2017/6/23.
 */

public class ListAdapter extends BaseAdapter
{
    private Context context=null;
    private LayoutInflater mInflater = null;
    private List<Map<String, Object>> data;

    public ListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.context=context;
    }

    public void setData(List<Map<String, Object>> data){
        this.data=data;
    }

    @Override
    public int getCount() {
        // 在此适配器中所代表的数据集中的条目数
        return this.data.size();
    }

    @Override
    public Object getItem(int position) {
        // 获取数据集中与指定索引对应的数据项
        return position;
    }

    @Override
    public long getItemId(int position) {
        // 取在列表中与指定索引对应的行id
        return position;
    }

    //然后重写getView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        //如果缓存convertView为空，则需要创建View
        if(convertView == null){
            holder = new ViewHolder();
            //根据自定义的Item布局加载布局
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder.img = (ImageView)convertView.findViewById(R.id.item_img);
            holder.title = (TextView)convertView.findViewById(R.id.item_title);
            holder.info = (TextView)convertView.findViewById(R.id.item_info);
            holder.weather = (TextView)convertView.findViewById(R.id.item_weather);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.img.setBackgroundResource((Integer)data.get(position).get("img"));
        holder.title.setText((String)data.get(position).get("title"));
        holder.info.setText((String)data.get(position).get("info"));
        holder.weather.setText((String)data.get(position).get("weather"));
        return convertView;
    }

    //在外面先定义，ViewHolder静态类
    static class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView info;
        public TextView weather;
    }

}
