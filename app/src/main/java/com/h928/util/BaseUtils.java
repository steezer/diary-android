package com.h928.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.view.View;
import android.view.View.MeasureSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseUtils {

	/**
	 * 获取时间
	 * @return 时间戳
	 */
	public static long getTime(){
		return (new Date()).getTime();
	}

	/**
	 * 更加格式获取时间
	 * @param format 时间格式
	 * @return 字符串格式时间
	 * 时间格式范例：
	 * yyyy年MM月dd日 E =》 2012年06月09日 星期六
	 * yyyy-MM-dd HH:mm:ss:SS -》2012-06-09 23:33:33:22
	 */
	public static String getTime(String format){
		Date date=new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}


	//计算md5值
	public static String md5(String string) {
		if (string==null || string.isEmpty()) {
			return "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(string.getBytes());
			String result = "";
			for (byte b : bytes) {
				String temp = Integer.toHexString(b & 0xff);
				if (temp.length() == 1) {
					temp = "0" + temp;
				}
				result += temp;
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	// 计算文件的 MD5 值
	public static String md5(File file) {
		if (file == null || !file.isFile() || !file.exists()) {
			return "";
		}
		FileInputStream in = null;
		String result = "";
		byte buffer[] = new byte[8192];
		int len;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer)) != -1) {
				md5.update(buffer, 0, len);
			}
			byte[] bytes = md5.digest();

			for (byte b : bytes) {
				String temp = Integer.toHexString(b & 0xff);
				if (temp.length() == 1) {
					temp = "0" + temp;
				}
				result += temp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(null!=in){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	// 判断是否有SD卡
	public static boolean hasSDCard() {
		String status = Environment.getExternalStorageState();
		return status.equals(Environment.MEDIA_MOUNTED);
	}

	//获取根目录路径
	public static String getRootFilePath() {
		if (hasSDCard()) {
			// filePath:/sdcard/
			return Environment.getExternalStorageDirectory().getAbsolutePath()+ "/";
		} else {
			// filePath:/data/data/
			return Environment.getDataDirectory().getAbsolutePath() + "/data/";
		}
	}


	//将视图转换为位图工具
	public Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	///////////////////图像像素转换/////////////////
	public int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	public int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
