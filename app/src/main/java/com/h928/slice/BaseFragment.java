package com.h928.slice;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.h928.view.Index;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by xiechunping on 2017/6/27.
 */

public class BaseFragment extends Fragment {

    //从父对象传递过来的参数前缀
    protected final static String ARG_PREFIX="arg";
    protected final static String ARG_ONE="arg0";
    protected final static String ARG_TWO="arg1";

    //提示
    protected void alert(String content){
        Toast.makeText(getActivity(),content,Toast.LENGTH_SHORT).show();
    }

    //切换Fragment
    public void switchFragment(int itemId, String ...args){
        Index index=(Index) getActivity();
        index.switchFragment(itemId,args);
    }

    //设置配置
    public void setConfig(String name, Map<String,Object> data){
        SharedPreferences preferences = getActivity().getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        if(data!=null){
            for(String key : data.keySet()){
                setConfigEditor(editor,key,data.get(key));
            }
        }else{
            editor.clear();
        }
        editor.commit();
    }

    //根据键值设置
    public void setConfig(String name, String key, Object value){
        SharedPreferences preferences = getActivity().getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        setConfigEditor(editor,key,value);
        editor.commit();
    }

    //设置编辑器值（内部调用）
    private void setConfigEditor(SharedPreferences.Editor editor, String key, Object value){
        if(value==null){
            editor.remove(key);
        }else if(value instanceof Integer){
            editor.putInt(key,(Integer)value);
        }else if(value instanceof String){
            editor.putString(key,(String)value);
        }else if(value instanceof Boolean){
            editor.putBoolean(key,(Boolean)value);
        }else if(value instanceof Float){
            editor.putFloat(key,(Float)value);
        }else if(value instanceof Long){
            editor.putLong(key,(Long)value);
        }else{
            editor.putString(key,value.toString());
        }
    }

    //读取配置
    public Map getAllConfig(String name){
        SharedPreferences preferences = getActivity().getSharedPreferences(name, MODE_PRIVATE);
        return preferences.getAll();
    }

    //获取配置集合
    public SharedPreferences config(String name){
        return getActivity().getSharedPreferences(name, MODE_PRIVATE);
    }

}
