package com.h928.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.h928.util.Constants;
import com.h928.util.DbManager;
import com.h928.util.NetUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context context = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db=DbManager.get("test.db",context);
        System.out.println(DbManager.getLastId("demo"));
        db.close();
    }

    @Test
    public void AppTest(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        SharedPreferences preferences = appContext.getSharedPreferences("setting", MODE_PRIVATE);
        System.out.println(preferences.getString("area_code",""));
    }
}
