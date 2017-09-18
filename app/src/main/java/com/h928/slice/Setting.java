package com.h928.slice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.h928.util.picker.area.AreaPicker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.h928.util.ViewAnno;
import com.h928.view.Login;
import com.h928.view.R;
import com.h928.util.ViewUtils;

import java.util.HashMap;
import java.util.Map;

public class Setting extends BaseFragment implements View.OnClickListener{

    @ViewAnno(R.id.setting_items)
    private LinearLayout settingItems;
    @ViewAnno(R.id.user_avatar)
    private ImageView userAvatar;
    @ViewAnno(R.id.user_name)
    private TextView userName;
    @ViewAnno(R.id.account_tips)
    private TextView accountTips;
    @ViewAnno(R.id.switch_setting_sync)
    private Switch settingSync;
    @ViewAnno(R.id.setting_logout)
    private LinearLayout logout;
    @ViewAnno(R.id.area_name)
    private TextView areaName;


    private int user_id=0; //用户ID

    private AreaPicker picker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.slice_setting, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ViewUtils.bindSubViews(this);
        ViewUtils.setSubViewOnClickListener(settingItems,this);
        userAvatar.setOnClickListener(this);
        userName.setOnClickListener(this);
        settingSync.setOnClickListener(this);

        settingSync.setChecked(config("setting").getBoolean("auto_sync",false));
        areaName.setText(config("setting").getString("area_name",""));

        //设置地区选择控件
        picker=new AreaPicker(getActivity(),config("setting").getString("area_code",null));
        picker.setOnConfirmListener(new AreaPicker.IOnConfirmListener(){
            @Override
            public boolean onConfirm(String province, String city, String district, String code) {
                setConfig("setting","area_code",code);
                setConfig("setting","area_name",district);
                areaName.setText(district);
                return true;
            }
        });

        //用户信息初始化设置
        initUserInfo();

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        picker.dismiss();
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_area: //点击地区
                picker.show();
                break;
            case R.id.switch_setting_sync: //自动同步切换
                setConfig("setting","auto_sync",settingSync.isChecked());
                break;
            case R.id.user_avatar: //点击头像登录
                if(user_id!=0){
                    //TODO: 点击切换头像
                }else{
                    //登陆云账号
                    userLogin();
                }
                break;
            case R.id.user_name: //点击用户名登录
                if(user_id==0){
                    userLogin();
                }
                break;
            case R.id.setting_logout: //退出登录
                setConfig("user",null);
                initUserInfo();
                break;
        }
    }

    //登陆返回结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==requestCode+1 && data!=null){
            Map<String,Object> user=new HashMap<String, Object>();
            user_id = data.getIntExtra("id",0);
            user.put("id",user_id);
            user.put("avatar",data.getStringExtra("avatar"));
            user.put("user_name",data.getStringExtra("user_name"));
            user.put("last_login_time",data.getStringExtra("last_login_time"));
            setConfig("user",user);
            initUserInfo(); //设置到界面
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //用户信息设置
    private void initUserInfo(){
        Map<String,Object> user= getAllConfig("user");
        String user_avatar;
        String user_name;
        String last_login_time;
        if(user.size()>0){
            user_id=(int)user.get("id");
            user_avatar = (String)user.get("avatar");
            //如果用户无头像则使用默认头像
            if(user_avatar==null || user_avatar.isEmpty()){
                user_avatar="drawable://"+R.drawable.ic_account;
            }
            user_name = (String)user.get("user_name");
            last_login_time = "上次登录:"+(String)user.get("last_login_time");
            logout.setVisibility(View.VISIBLE);
            settingSync.setEnabled(true);
        }else{
            user_id=0;
            user_avatar = "drawable://"+R.drawable.ic_account;
            user_name = getString(R.string.account_login_tips);
            last_login_time = getString(R.string.account_login_help);
            logout.setVisibility(View.GONE);
            settingSync.setChecked(false);
            settingSync.setEnabled(false);
        }
        //加载用户头像
        ImageLoader.getInstance().displayImage(user_avatar,userAvatar);
        userName.setText(user_name);
        accountTips.setText(last_login_time);
    }

    //用户登录
    private void userLogin(){
        boolean isLogin=false;
        if(isLogin){
            Intent intent=new Intent(getActivity(),Login.class);
            intent.putExtra("user_id",user_id);
            startActivityForResult(intent,101);
        }else{
            alert("功能完善中...");
        }
    }

    ///////////////////静态方法/////////////////////

    //获取对象示例
    public static Setting newInstance(String args[]){
        Setting setting=new Setting();
        int indexKey=0;
        Bundle bundle=new Bundle();
        for (String arg : args) {
            bundle.putString(ARG_PREFIX+String.valueOf(indexKey++),arg);
        }
        setting.setArguments(bundle);
        return setting;
    }


}
