package com.h928.util;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiechunping on 2017/6/27.
 */


public class ViewUtils {

    public static View getContentView(Activity ac){
        ViewGroup view = (ViewGroup)ac.getWindow().getDecorView();
        FrameLayout content = (FrameLayout)view.findViewById(android.R.id.content);
        return content.getChildAt(0);
    }

    /**
     * 将子视图和视图字段设置绑定.
     * @param object 根对象
     * @throws IllegalStateException 视图资源ID错误或没有调用setContentView()导致
     * 使用说明：
     * 在字段声明前注解
     * @InjectView(R.id.fetch_button)
     * private Button mFetchButton;
     *
     * 关联字段
     * ViewUtils.bindSubViews(this);
     */
    public static void bindSubViews(Object object) {
        View view=null;
        if(object instanceof Fragment){
            view=((Fragment)object).getView();
        }else if(object instanceof Activity){
            view=getContentView((Activity)object);
        }else if(object instanceof View){
            view=(View)object;
        }
        if(view!=null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation.annotationType().equals(ViewAnno.class)) {
                        try {
                            Class<?> fieldType = field.getType();
                            int idValue = ViewAnno.class.cast(annotation).value();
                            field.setAccessible(true);
                            Object injectedValue = fieldType.cast(view.findViewById(idValue));
                            if (injectedValue == null) {
                                throw new IllegalStateException("findViewById(" + idValue + ") gave null for " + field + ", can't inject");
                            }
                            field.set(object, injectedValue);
                            field.setAccessible(false);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置子视图事件
     *
     * @param view 当前视图
     * @param listener 监听器
     * @return List<View> 返回设置的视图
     * */
    public static List<View> setSubViewOnClickListener(View view,View.OnClickListener listener){
        return setSubViewOnClickListener(view,listener,false);
    }

    /**
     * 设置所有子视图事件
     *
     * @param view 当前视图
     * @param listener 监听器
     * @param isAll 是否设置所有视图
     * @return List<View> 返回设置的视图
     * */
    public static List<View> setSubViewOnClickListener(View view,View.OnClickListener listener,boolean isAll){
        List<View> subviews=ViewUtils.getSubViews(view,isAll);
        for(View subview : subviews){
            subview.setOnClickListener(listener);
        }
        return subviews;
    }


    /**
     * 获取子视图
     *
     * @param view 当前视图
     * @return 子视图列表
     * */
    public static List<View> getSubViews(View view){
        return getSubViews(view, false);
    }

    /**
     * 获取所有子视图
     *
     * @param view 当前视图
     * @param isAll 是否包含所有视图
     * @return 子视图列表
     * */
    public static List<View> getSubViews(View view, boolean isAll) {
        List<View> subViews = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View subView = vp.getChildAt(i);
                subViews.add(subView);
                if(isAll) {
                    subViews.addAll(getSubViews(subView,isAll));
                }
            }
        }
        return subViews;
    }
}
