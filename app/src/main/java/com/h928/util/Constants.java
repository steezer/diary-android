package com.h928.util;

import android.os.Environment;
import android.os.StrictMode;


public class Constants {
	/**
	 ******************************************* 参数设置信息******************************************
	 */

	// 应用名称
	public static String APP_NAME = "";

	// 网络请求API地址
	public static String APP_API = "http://192.168.1.105/apps/test/diary.php";
	// 网络请求签名密钥
	public static String APP_KEY = "76d350ed07e75c9d3c31f3bedcafdc0b";


	// 保存参数文件夹名
	public static final String SHARED_PREFERENCE_NAME = "diary_prefs";

	// SDCard路径
	public static final String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

	// 图片存储路径
	public static final String BASE_PATH = SD_PATH + "/diary/";

	// 缓存图片路径
	public static final String BASE_IMAGE_CACHE = BASE_PATH + "cache/images/";

	/**
	 ******************************************* 参数设置信息结束 ******************************************
	 */
}
