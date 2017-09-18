package com.h928.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
    * Assets数据库管理器
    * 首次使用将会把数据库拷贝到"/data/data/[应用包名]/database"下面
    *
    * 示例:
    * DbManager.init(getApplication()); // 此方法只需要一次调用
    * SQLiteDatabase db1 = DbManager.get("db1.db");  // 从assets目录下的db1.db获取SQLiteDatabase对象
    * 或者直接使用：
    * SQLiteDatabase db1 = DbManager.get("db1.db",getApplication());
*/

public class DbManager{
    private static String tag = "ST";
    private static String configName=DbManager.class.getName();
    private static String databasePath = "/data/data/%s/databases"; // %s 是包名
    private Map<String, SQLiteDatabase> databases = new HashMap<>(); //数据库对象集合
    private Context context = null;
    private static DbManager mInstance = null;
    private static String cDbFile=null;

    public DbManager(Context context){
        this.context=context;
    }

    /**
     * 初始化DbManager
     * @param context 应用程序上下文对象
     */
    public static void init(Context context){
        if(mInstance == null){
            mInstance = new DbManager(context);
        }
    }

    public static SQLiteDatabase get(String dbFile){
        cDbFile=dbFile;
        return mInstance!=null ? mInstance.getDatabase(dbFile) : null;
    }

    public static SQLiteDatabase get(String dbFile,Context context){
        if(mInstance == null && context!=null){
            log("get(String dbFile,Context context)");
            mInstance = new DbManager(context);
        }
        return get(dbFile);
    }

    /**
     * 获取assets数据库，如果数据库已经打开则返回已打开的实例对象
     * @param dbFile assets数据库文件名称，位于assets目录下
     * @return 如果成功返回SQLiteDatabase对象，否则返回null
     */
    private SQLiteDatabase getDatabase(String dbFile) {
        //如果实例缓存中存在，则直接返回
        if(databases.get(dbFile) != null){
            return databases.get(dbFile);
        }

        if(context==null) return null;

        String sPath = getDbFilepath();
        String sFile = getDbFile(dbFile);
        File file = new File(sFile);
        SharedPreferences dbs = context.getSharedPreferences(configName, 0);
        boolean flag = dbs.getBoolean(dbFile, false);
        File desFile=new File(sFile);
        if(!flag || !file.exists()){
            file = new File(sPath);
            if(!file.exists() && !file.mkdirs()){
                log("Create \""+sPath+"\" fail!");
                return null;
            }
            if(!copyAssetsToFilesystem(dbFile, desFile)){
                log(String.format("Copy %s to %s fail!", dbFile, sFile));
                return null;
            }
            dbs.edit().putBoolean(dbFile, true).commit();
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(sFile, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        if(db != null){
            databases.put(dbFile, db);
        }
        return db;
    }

    private String getDbFilepath(){
        return String.format(databasePath, context.getApplicationInfo().packageName);
    }

    private String getDbFile(String dbFile){
        return getDbFilepath()+"/"+dbFile;
    }

    /**
     * 数据库文件拷贝
     * @param assetsSrc Assets数据库文件名
     * @param desFile 目标路径，为：/data/data/[应用包名]/database/
     * */
    private boolean copyAssetsToFilesystem(String assetsSrc, File desFile){
        InputStream istream = null;
        OutputStream ostream = null;
        try{

            AssetManager am = context.getAssets();
            istream = am.open(assetsSrc);

            ostream = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = istream.read(buffer))>0){
                ostream.write(buffer, 0, length);
            }
            istream.close();
            ostream.close();
        }catch(Exception e){
            e.printStackTrace();
            try{
                if(istream!=null)istream.close();
                if(ostream!=null)ostream.close();
                //如果目标文件存在且输入assets文件不存在
                if(desFile.exists() && istream==null){
                    log("Use dbFile");
                    return true;
                }
            }catch(Exception ee){
                ee.printStackTrace();
            }
            return false;
        }
        return true;
    }


    public static void resetFlag(String dbFile){
        if(mInstance!=null) {
            SharedPreferences dbs = mInstance.context.getSharedPreferences(configName, 0);
            dbs.edit().putBoolean(dbFile,false).commit();
        }
    }

    public static String getLastId(String table){
        return getLastId(cDbFile,table);
    }

    public static String getLastId(String dbFile,String table){
        SQLiteDatabase db=get(dbFile);
        int id=-1;
        if(db!=null){
            Cursor cursor=db.rawQuery("select id from "+table+" order by id desc limit 0,1",null);
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex("id"));
            }
            cursor.close();
        }
        return id!=-1 ? String.valueOf(id) : null;
    }

    /**
     * 关闭assets数据库
     * @param dbFile 需要关闭的数据库文件
     * @return 返回关闭状态
     */
    public static boolean close(String dbFile){
        if(mInstance != null && dbFile != null) {
            SQLiteDatabase db=get(dbFile);
            if (db != null) {
                db.close();
                mInstance.databases.remove(dbFile);
                return true;
            }
        }
        return false;
    }

    /**
     * 关闭所有数据库
     */
    public static void close(){
        if(mInstance != null){
            for(int i=0; i<mInstance.databases.size(); ++i){
                if(mInstance.databases.get(i)!=null){
                    mInstance.databases.get(i).close();
                }
            }
            mInstance.databases.clear();
        }
    }

    public static void log(String info){
        System.out.println(info);
    }

}
