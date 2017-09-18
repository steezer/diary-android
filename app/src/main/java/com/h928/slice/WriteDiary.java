package com.h928.slice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.h928.util.BaseUtils;
import com.h928.util.DbManager;
import com.h928.util.NetUtils;
import com.h928.util.ViewAnno;
import com.h928.util.ViewUtils;
import com.h928.view.Index;
import com.h928.view.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WriteDiary extends BaseFragment
        implements IFragment,View.OnClickListener
{

    //界面字段设置
    @ViewAnno(R.id.diary_title)
    private TextView diaryTitle;
    @ViewAnno(R.id.diary_content)
    private EditText diaryContent;
    @ViewAnno(R.id.diary_subtitle)
    private EditText diarySubtitle;
    @ViewAnno(R.id.diary_emotes)
    private GridLayout diaryEmotes;
    @ViewAnno(R.id.diary_weather)
    private TextView diaryWeather;

    //表情序号
    private int diaryEmoteIndex=0;
    //日记ID
    private String diaryId=null;
    //数据库操作对象
    private SQLiteDatabase dbObj=null;
    //今日日记标记
    private boolean todayFlag=false;

    //参数
    private Bundle arguments=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        dbObj=DbManager.get("diary.db");
        return inflater.inflate(R.layout.slice_write_diary,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.bindSubViews(this);
        ViewUtils.setSubViewOnClickListener(diaryEmotes,this);
    }

    @Override
    public void onResume() {
        String todayId=getNowDiaryId();
        diaryId=arguments.getString(ARG_ONE);
        if(diaryId==null){
            //从数据库中查询今日写的日记ID
            diaryId=todayId;
        }
        todayFlag=!(diaryId!=null && todayId!=null && !todayId.equals(diaryId));
        initView();
        super.onResume();
    }

    @Override
    public void onPause() {
        doSave(); //保存日记
        super.onPause();
    }

    @Override
    public boolean doFromParent(String ...args) {
        if(args.length>0){
            switch (args[0]){
                case "edit": //设置为编辑模式
                    diaryContent.setFocusableInTouchMode(true);
                    diarySubtitle.setFocusableInTouchMode(true);
                    break;
                case "save": //保存修改或添加数据
                    doSave();
                    alert("已保存！");
                    break;
            }
        }
        return false;
    }

    //表情点击事件
    @Override
    public void onClick(View v) {
        List<View> emotes=ViewUtils.getSubViews(diaryEmotes);
        int curIndex=0;
        for (View item:emotes){
            if(v.equals(item)){
                diaryEmoteIndex =curIndex;
            }
            curIndex++;
        }
        setDiaryEmote(diaryEmoteIndex);
    }

    //参数设置
    @Override
    public void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    //界面初始化
    private void initView(){
        if(diaryId!=null){ //编辑模式
            if(todayFlag) {
                diaryContent.setFocusableInTouchMode(true);
                diarySubtitle.setFocusableInTouchMode(true);
                Menu menu=((Index)getActivity()).getToolbarMenu();
                menu.findItem(R.id.menu_action_edit).setVisible(false);
                menu.findItem(R.id.menu_action_save).setVisible(true);
            }else{
                diaryContent.setFocusableInTouchMode(false);
                diarySubtitle.setFocusableInTouchMode(false);
            }
            //获取编辑的日记
            Cursor cursor=dbObj.rawQuery("select * from diary where id="+diaryId+" limit 0,1",null);
            while (cursor.moveToNext()) {
                diaryTitle.setText(cursor.getString(cursor.getColumnIndex("title")));
                diarySubtitle.setText(cursor.getString(cursor.getColumnIndex("subtitle")));
                diaryContent.setText(cursor.getString(cursor.getColumnIndex("content")));
                diaryWeather.setText(cursor.getString(cursor.getColumnIndex("weather")));
                diaryEmoteIndex=cursor.getInt(cursor.getColumnIndex("emote"));
                setDiaryEmote(diaryEmoteIndex);
            }
            cursor.close();

        }else{ //添加模式
            diaryTitle.setText(BaseUtils.getTime("yyyy年MM月dd日 E"));
            //setConfig("setting",null);//测试：清空
            getWeather();
        }
    }

    //获取日记ID通过sync_id
    private String getNowDiaryId(){
        String syncId=BaseUtils.getTime("yyyyMMdd");
        Cursor cursor=dbObj.rawQuery("select id from diary where sync_id="+syncId+" limit 0,1",null);
        int id=-1;
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id==-1 ? null : String.valueOf(id);
    }

    //设置表情
    private void setDiaryEmote(int emote){
        int selectedColor=Color.parseColor("#dddddd");
        int normalColor=0;
        List<View> emotes=ViewUtils.getSubViews(diaryEmotes);
        int curIndex=0;
        for (View item:emotes){
            item.setBackgroundColor(curIndex==emote ? selectedColor : normalColor);
            curIndex++;
        }
    }

    //保存日记
    private void doSave(){
        ContentValues data=new ContentValues();
        data.put("title",diaryTitle.getText().toString());
        data.put("subtitle",diarySubtitle.getText().toString());
        data.put("content",diaryContent.getText().toString());
        data.put("weather",diaryWeather.getText().toString());
        data.put("emote",diaryEmoteIndex);
        data.put("edittime", BaseUtils.getTime());
        if(diaryId!=null){
            dbObj.update("diary",data,"id = ?",new String[]{diaryId});
        }else{
            data.put("sync_id", BaseUtils.getTime("yyyyMMdd"));
            data.put("addtime", BaseUtils.getTime());
            dbObj.insert("diary",null,data);
            diaryId=DbManager.getLastId("diary");
        }
    }

    //获取天气
    private void getWeather(){
        final String areaCode=config("setting").getString("area_code",null);
        NetUtils netUtils = new NetUtils();
        if(areaCode==null) {
            netUtils.addRemoteUrl("http://wgeo.weather.com.cn/ip/?_=" + String.valueOf(BaseUtils.getTime()));
            netUtils.addRemoteUrl("http://www.weather.com.cn/data/cityinfo/{code}.html");
        }else{
            netUtils.addRemoteUrl("http://www.weather.com.cn/data/cityinfo/101"+areaCode+".html");
        }
        netUtils.setHeader("Referer", "http://www.weather.com.cn/");
        netUtils.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        netUtils.setListener(new NetUtils.IRequestListener() {
            @Override
            public void onComplete(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
                    String weather=weatherInfo.getString("weather");
                    String cityId=weatherInfo.getString("cityid");
                    diaryWeather.setText(weather);
                    if(areaCode==null){
                        setConfig("setting","area_code",cityId.substring(3));
                        System.out.println("autoGet:"+cityId);
                    }
                    System.out.println("Get weather:"+weather+"("+cityId+")");
                    alert("天气获取成功");
                }catch (JSONException e){
                    System.out.println(e.toString());
                }
            }

            @Override
            public String onResume(int index,String url, String result) {
                if(index>0){
                    if(result.startsWith("var ip=")){
                        String[] strings=result.split("\";var ");
                        String code=strings[1].replace("id=\"","");
                        return url.replace("{code}",code);
                    }
                    return null;
                }
                return url;
            }
        });
        netUtils.execute();
    }

    ///////////////////静态方法/////////////////////

    //获取对象示例
    public static WriteDiary newInstance(String args[]){
        WriteDiary writeDiary = new WriteDiary();
        int indexKey=0;
        Bundle bundle=new Bundle();
        for (String arg : args) {
            bundle.putString(ARG_PREFIX+String.valueOf(indexKey++),arg);
        }
        writeDiary.setArguments(bundle);
        return writeDiary;
    }

}
