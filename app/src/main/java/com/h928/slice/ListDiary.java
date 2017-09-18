package com.h928.slice;

import android.app.FragmentManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.h928.adapter.ListAdapter;
import com.h928.extend.LoadListView;
import com.h928.util.DbManager;
import com.h928.view.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListDiary extends BaseFragment
        implements IFragment,AdapterView.OnItemClickListener
{

    FragmentManager fm;
    private LoadListView mListView =null;

    //listView的数据填充器
    private ListAdapter adapter=null;
    //listView中数据的集合
    private List<Map<String, Object>> listData =new ArrayList<>();
    private int total = 0;
    private int step = 10;
    private int add = 0;
    private Bundle params;
    //数据库操作对象
    private SQLiteDatabase dbObj=null;
    private String ListCondition="";
    private String ListOrder="order by id desc";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.slice_list_diary, container, false);
        params=getArguments();
        mListView=(LoadListView) view.findViewById(R.id.load_list_content);

        //初始化数据
        dbObj= DbManager.get("diary.db");
        total=getListTotal();

        fm=getFragmentManager();
        //获取将要绑定的数据设置到data中
        this.setAdapter();
        this.getListData();

        mListView.setOnLoadListener(new LoadListView.ILoadListener(){
            @Override
            public void onLoad(){
                int current = mListView.getAdapter().getCount();
                if (current < total) {
                    getListData();
                }else {
                    mListView.loadComplete();
                }
            }
        });

        //点击每行的事件
        mListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String itemId=listData.get(position).get("id").toString();
        switchFragment(R.id.navigation_write,itemId);
    }

    @Override
    public boolean doFromParent(String ...args) {
        String kw=args[0];
        if(!TextUtils.isEmpty(kw)){
            kw=kw.replace("'","''");
            ListCondition="where title like '%"+kw+"%' or subtitle like '%"+kw+
                    "%' or content like '%"+kw+"%' or weather like '%"+kw+"%'";
        }else{
            ListCondition="";
        }
        add=0;
        listData.clear();
        total=getListTotal();
        getListData();
        return false;
    }


    ///////////////////私有方法////////////////////

    //设置数据接口
    private void setAdapter(){
        adapter = new ListAdapter(getActivity());
        adapter.setData(listData);
        mListView.setAdapter(adapter);
    }

    //获取日记总数
    private int getListTotal(){
        Cursor cursor=dbObj.rawQuery("select count(*) as total from diary "+ListCondition,null);
        int total=0;
        while (cursor.moveToNext()) {
            total = cursor.getInt(cursor.getColumnIndex("total"));
        }
        cursor.close();
        return total;
    }

    //获取数据
    private void getListData() {
        Map<String, Object> map;
        String sql="select * from diary "+ListCondition+" "+ListOrder+
                " limit "+String.valueOf(add)+","+String.valueOf(step);
        Cursor cursor=dbObj.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            map = new HashMap<>();
            map.put("id", cursor.getInt(cursor.getColumnIndex("id")));
            map.put("img", getEmote(cursor.getInt(cursor.getColumnIndex("emote"))));
            map.put("title", cursor.getString(cursor.getColumnIndex("title")));
            map.put("info", cursor.getString(cursor.getColumnIndex("subtitle")));
            map.put("weather", cursor.getString(cursor.getColumnIndex("weather")));
            listData.add(map);
        }
        cursor.close();
        add += step;

        //通知更新
        adapter.notifyDataSetChanged();
        mListView.loadComplete();
    }

    //获取表情
    private int getEmote(int index){
        int emotes[]={
                R.drawable.face01_plain,
                R.drawable.face02_smile,
                R.drawable.face03_laughing,
                R.drawable.face04_gloat,
                R.drawable.face05_sad,
                R.drawable.face06_crying,
                R.drawable.face07_surprise,
                R.drawable.face08_embarrassed,
                R.drawable.face09_asleep,
                R.drawable.face10_angry
        };
        if(index>=0 || index<emotes.length){
            return emotes[index];
        }
        return emotes[0];
    }


    ///////////////////静态方法/////////////////////

    //获取对象示例
    public static ListDiary newInstance(String args[]){
        ListDiary listDiary=new ListDiary();
        int indexKey=0;
        Bundle bundle=new Bundle();
        for (String arg : args) {
            bundle.putString(ARG_PREFIX+String.valueOf(indexKey++),arg);
        }
        listDiary.setArguments(bundle);
        return listDiary;
    }

}
