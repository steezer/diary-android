package com.h928.view;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by xiechunping on 2017/6/27.
 */

public class BaseView extends AppCompatActivity {
    private static String TAG="ST";
    //提示
    protected void alert(String content){
        Toast.makeText(this,content,Toast.LENGTH_SHORT).show();
    }

    //输出调试日志
    protected static void log(String info){
        Log.d(TAG,info);
    }
}
