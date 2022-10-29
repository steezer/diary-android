package com.h928.extend;

import android.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import com.h928.view.R;


public class LoadListView extends ListView implements OnScrollListener{
    View footer;//底部布局
    int totalItemCount;//总数量
    int firstVisibleItem;//第一个可见的Item
    int lastVisibleItem;//最后一个可见的Item
    boolean isLoading;//正在加载
    ILoadListener iLoadListener;
    public LoadListView(Context context) {
        super(context);
        initView(context);
    }

    public LoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    /**
     * 添加底部加载提示布局到listview
     * @param context
     */
    private void initView(Context context) {
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        footer=layoutInflater.inflate(R.layout.list_foot, null);
        footer.findViewById(R.id.load_list_footer).setVisibility(View.GONE);
        this.addFooterView(footer);
        this.setOnScrollListener(this);
        Log.i("initView","test");
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        this.firstVisibleItem=firstVisibleItem;
        this.lastVisibleItem=firstVisibleItem+visibleItemCount;
        this.totalItemCount=totalItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //项目显示完毕并且停止滚动
        if (totalItemCount==lastVisibleItem && scrollState==SCROLL_STATE_IDLE) {
            if (!isLoading) {
                Log.i("onScrollStateChanged","Load more...");
                isLoading=true;
                footer.findViewById(R.id.load_list_footer).setVisibility(View.VISIBLE);
                iLoadListener.onLoad();//加载更多数据
            }
        }else if(firstVisibleItem==0 && scrollState==SCROLL_STATE_IDLE){
            Log.i("onScrollStateChanged","startFresh121");
        }
    }

    //加载完毕
    public void loadComplete() {
        isLoading=false;
        footer.findViewById(R.id.load_list_footer).setVisibility(View.GONE);
    }

    //设置接口
    public void setOnLoadListener(ILoadListener iLoadListener) {
        this.iLoadListener=iLoadListener;
    }

    //加载更多数据的回调接口
    public interface ILoadListener
    {
        public void onLoad();
    }
}