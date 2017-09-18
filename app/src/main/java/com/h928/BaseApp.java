package com.h928;

import android.app.Application;
import android.content.res.Configuration;

//import com.example.ele_me.image.ImageLoaderConfig;
//import com.example.ele_me.util.Constants;

import com.h928.util.DbManager;
import com.h928.util.image.ImageLoaderConfig;
import com.h928.util.Constants;

public class BaseApp extends Application {
	private String jumpType="";
	private static BaseApp instance;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DbManager.init(this);
		ImageLoaderConfig.initImageLoader(this, Constants.BASE_IMAGE_CACHE);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		DbManager.close();
		super.onTerminate();
	}

	public String getJumpType() {
		return jumpType;
	}

	public void setJumpType(String jumpType) {
		this.jumpType = jumpType;
	}

}
