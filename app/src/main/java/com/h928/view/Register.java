package com.h928.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.h928.util.NetUtils;
import com.h928.util.ViewAnno;
import com.h928.util.ViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.h928.util.Constants.APP_API;

public class Register extends BaseView implements NetUtils.IRequestListener,View.OnClickListener{

    //视图字段
    @ViewAnno(R.id.submitBtn)
    private Button loginBtn;
    @ViewAnno(R.id.resetBtn)
    private Button resetBtn;
    @ViewAnno(R.id.username)
    private EditText username;
    @ViewAnno(R.id.password)
    private EditText password;
    @ViewAnno(R.id.email)
    private EditText email;
    @ViewAnno(R.id.status)
    private TextView status;
    @ViewAnno(R.id.logo)
    private ImageView logo;

    private int user_id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_register);
        ViewUtils.bindSubViews(this);

        this.setTitle(R.string.register_title);
        Bundle bundle = this.getIntent().getExtras();
        user_id=bundle.getInt("user_id");

        //按钮事件绑定
        loginBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);

        //显示返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    //处理登录和注册请求结果
    @Override
    public void onComplete(String result) {
        try {
            System.out.print(result);
            JSONObject jsonObject=new JSONObject(result);
            int error_code=jsonObject.getInt("error_code");
            String error_msg=jsonObject.getString("error_msg");
            if(error_code!=0){
                alert(error_msg);
            }else {
                int user_id = jsonObject.getInt("id");
                String auth_time = jsonObject.getString("auth_time");
                String auth_token = jsonObject.getString("auth_token");
                String avatar = jsonObject.getString("avatar");
                String user_name = jsonObject.getString("user_name");
                String last_login_time = jsonObject.getString("last_login_time");

                Intent intent = new Intent();
                intent.putExtra("id", user_id);
                intent.putExtra("auth_time", auth_time);
                intent.putExtra("auth_token", auth_token);
                intent.putExtra("avatar", avatar);
                intent.putExtra("user_name", user_name);
                intent.putExtra("last_login_time", last_login_time);

                setResult(103, intent);
                finish();
            }
        }catch (JSONException e){
            System.out.println(e.toString());
            alert("网络错误！请稍候再试！");
        }
    }

    @Override
    public String onResume(int index, String url, String result) {
        return url;
    }

    //登录&注册按钮点击
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //提交
            case R.id.submitBtn:
                if(checkInput()) {
                    HashMap<String, String> userInfo = new HashMap<>();
                    userInfo.put("action", "register");
                    userInfo.put("username", this.username.getText().toString());
                    userInfo.put("password", this.password.getText().toString());
                    userInfo.put("email", this.email.getText().toString());
                    userInfo.put("signature", NetUtils.signature(userInfo));
                    NetUtils.post(APP_API, userInfo, this); //网络请求
                }
                break;
            //重置
            case R.id.resetBtn:
                this.username.setText("");
                this.password.setText("");
                break;
        }
    }

    //输入检查
    private boolean checkInput(){
        if(this.username.getText().toString().trim().equals("")){
            alert("请输入用户名！");
            return false;
        }
        if(this.password.getText().toString().trim().equals("")){
            alert("请输入密码！");
            return false;
        }
        if(this.email.getText().toString().trim().equals("")){
            alert("请输入邮箱地址！");
            return false;
        }
        return true;
    }
}
