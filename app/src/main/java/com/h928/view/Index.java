package com.h928.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.h928.slice.IFragment;

import com.h928.slice.*;

public class Index extends BaseView
        implements BottomNavigationView.OnNavigationItemSelectedListener
{

    private SearchView mSearchView;
    private Toolbar toolbar;
    private int currentFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置主题及顶部标题栏操作
        setContentView(R.layout.view_index);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        // 设置首页显示
        switchFragment(R.id.navigation_diary);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //////////////////////////底部导航菜单///////////////////////////

    //获取菜单
    public Menu getToolbarMenu(){
        return toolbar.getMenu();
    }

    //底部导航菜单点击事件
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item!=null){
            switchFragment(item.getItemId());
            return true;
        }
        return false;
    }

    //切换显示Fragment
    public void switchFragment(int itemId, String ...args){
        FragmentManager fm=getFragmentManager();
        FragmentTransaction transaction=fm.beginTransaction();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        switch (itemId) {
            case R.id.navigation_diary:
                transaction.replace(R.id.fragment_content, ListDiary.newInstance(args),"listDiary");
                break;
            case R.id.navigation_write:
                transaction.replace(R.id.fragment_content, WriteDiary.newInstance(args),"writeDiary");
                if(args.length>0) {
                    transaction.addToBackStack(null);
                }
                break;
            case R.id.navigation_setting:
                transaction.replace(R.id.fragment_content, Setting.newInstance(args),"setting");
                break;
        }
        transaction.commit();
        switchOptionsMenu(itemId,args);
    }

    //切换顶部操作菜单
    public void switchOptionsMenu(int itemId , String ...args){
        currentFragmentId=itemId;
        Menu menu=getToolbarMenu();
        MenuItem searchItem=menu.findItem(R.id.menu_action_search);
        MenuItem editItem=menu.findItem(R.id.menu_action_edit);
        MenuItem saveItem=menu.findItem(R.id.menu_action_save);
        MenuItem helpItem=menu.findItem(R.id.menu_action_help);

        if(searchItem!=null)searchItem.collapseActionView();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        switch (itemId) {
            case R.id.navigation_diary:
                setTitle(R.string.title_diary);
                if(searchItem!=null)searchItem.setVisible(true);
                if(saveItem!=null)saveItem.setVisible(false);
                if(helpItem!=null)helpItem.setVisible(false);
                if(editItem!=null)editItem.setVisible(false);
                break;
            case R.id.navigation_write:
                if(args.length>0) { //查看日记
                    setTitle(R.string.title_view);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    if(editItem!=null)editItem.setVisible(true);
                    if(saveItem!=null)saveItem.setVisible(false);
                }else{ //写日记
                    setTitle(R.string.title_write);
                    if(editItem!=null)editItem.setVisible(false);
                    if(saveItem!=null)saveItem.setVisible(true);
                }
                if(searchItem!=null)searchItem.setVisible(false);
                if(helpItem!=null)helpItem.setVisible(false);
                break;
            case R.id.navigation_setting:
                setTitle(R.string.title_setting);
                if(searchItem!=null)searchItem.setVisible(false);
                if(saveItem!=null)saveItem.setVisible(false);
                if(helpItem!=null)helpItem.setVisible(true);
                if(editItem!=null)editItem.setVisible(false);
                break;
        }
    }

    //创建工具栏
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.toolbar, menu);

        menu.findItem(R.id.menu_action_save).setVisible(false);
        menu.findItem(R.id.menu_action_help).setVisible(false);
        final MenuItem searchItem = menu.findItem(R.id.menu_action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint("输入关键字...");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    // 输入法如果是显示状态，那么就隐藏输入法
                    imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                }
                mSearchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                IFragment iFragment=(IFragment)getFragmentManager().findFragmentByTag("listDiary");
                iFragment.doFromParent(newText);
                return true;
            }
        });

        return true;
    }

    //工具栏事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        FragmentManager fm = getFragmentManager();
        IFragment iFragment;
        int itemId=item.getItemId();
        switch (itemId){
            case android.R.id.home:
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                switchOptionsMenu(R.id.navigation_diary);
                break;
            case R.id.menu_action_edit: //进入编辑模式
                Menu menu=getToolbarMenu();
                menu.findItem(R.id.menu_action_edit).setVisible(false);
                menu.findItem(R.id.menu_action_save).setVisible(true);
                iFragment=(IFragment)fm.findFragmentByTag("writeDiary");
                iFragment.doFromParent("edit");
                break;
            case R.id.menu_action_save: //保存
                iFragment=(IFragment)fm.findFragmentByTag("writeDiary");
                iFragment.doFromParent("save");
                break;
            case R.id.menu_action_help: //帮助
                showHelp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(currentFragmentId!=R.id.navigation_diary){
            switchOptionsMenu(R.id.navigation_diary);
        }
        super.onBackPressed();
    }

    private void showHelp(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(Index.this);
        normalDialog.setIcon(R.mipmap.ic_launcher);
        normalDialog.setTitle(R.string.help_title);
        normalDialog.setMessage(R.string.help_content);
        normalDialog.setPositiveButton(R.string.help_close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }
}
