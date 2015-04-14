package com.mz.sticker.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mz.sticker.application.StickerApplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

public class ScreenUtil {

	public static final int KEEP_CURRENT_SCREEN_BRIGHTNESS = -1;
	
	/**
	 * {@see http://developer.android.com/guide/practices/screens_support.html#dips-pels} 
	 */
	public static int convertDpToPx(float dp) {
		float scale = StickerApplication.getAppContext().getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
	
	public static float convertPxToDp(int px) {
		float scale = StickerApplication.getAppContext().getResources().getDisplayMetrics().density;
		return px / scale;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
	public static Point getAppDisplayArea() {
		WindowManager wm = (WindowManager) StickerApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(size);
		}
		else {
			size.x = display.getWidth();
			size.y = display.getHeight();
		}
		return size;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Point getRealDisplayArea() {
		WindowManager wm = (WindowManager) StickerApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			display.getRealSize(size);
		}
		else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			try {
				Method mGetRawWidth = Display.class.getDeclaredMethod("getRawWidth");
				mGetRawWidth.setAccessible(true);
				Method mGetRawHeight = Display.class.getDeclaredMethod("getRawHeight");
				mGetRawHeight.setAccessible(true);
				size.x = (Integer) mGetRawWidth.invoke(display);
				size.y = (Integer) mGetRawHeight.invoke(display);
			} 
			catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		else {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			display.getMetrics(displayMetrics);
			size.x = displayMetrics.widthPixels;
			size.y = displayMetrics.heightPixels;
		}
		return size;
	}
	
	public static Point getAppDisplayAreaCenter() {
		Point screenSize = getAppDisplayArea();
		return new Point(screenSize.x / 2, screenSize.y / 2);
	}
	
	public static Point getRealDisplayAreaCenter() {
		Point screenSize = getRealDisplayArea();
		return new Point(screenSize.x / 2, screenSize.y / 2);
	}
	
	public static void enableKeepScreenOnForActivity(Activity activity, boolean keepScreenOn) {
		if(keepScreenOn) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		else {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
	/**
	 * NOTE: If you're having a problem with screen brightness change it can be connected with "Auto adjust screen tone" global setting.
	 * 		 If you disable it a problem can disappear.
	 * @param screenBrightness Value from 0.0 to 1.0
	 */
	public static void setScreenBrightnessForActivity(Activity activity, float screenBrightness) {
		Window window = activity.getWindow(); 
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.screenBrightness = screenBrightness;
		window.setAttributes(layoutParams);
	}
	
	/**
	 * Changes screen brightness globally. However screen won't be automatically refreshed to new brightness value.
	 * If this is a case one should create empty and transparent activity, set new value for window from it and close it.
	 * NOTE: If you're having a problem with screen brightness change it can be connected with "Auto adjust screen tone" global setting.
	 * 		 If you disable it a problem can disappear.
	 * @see //stackoverflow.com/questions/5032588/cant-apply-system-screen-brightness-programmatically-in-android
	 * @param screenBrightness Value between 0 and 255 or KEEP_CURRENT_SCREEN_BRIGHTNESS
	 */
	public static void setScreenBrightnessForSystem(int screenBrightness) {
		Context appContext = StickerApplication.getAppContext();
		if(screenBrightness == KEEP_CURRENT_SCREEN_BRIGHTNESS) {
			try {
				screenBrightness = Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			} 
			catch(SettingNotFoundException e) {
				e.printStackTrace();
			}
		}
		Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightness);
	}
	
	public static int getDeviceDefaultScreenOrientation() {
	    Context appContext = StickerApplication.getAppContext();
		WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
	    Configuration config = appContext.getResources().getConfiguration();
	    int rotation = windowManager.getDefaultDisplay().getRotation();
	    if(((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
	            config.orientation == Configuration.ORIENTATION_LANDSCAPE)
	        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&    
	            config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
	      return Configuration.ORIENTATION_LANDSCAPE;
	    }
	    else { 
	      return Configuration.ORIENTATION_PORTRAIT;
	    }
	}
	
	/**
	 * When a Drawable is attached to a view, the view is set as a callback on the drawable. <br/>
	 * If drawable's lifecycle is longer than view's lifecycle drawable will keep reference to view, which in turn will keep reference
	 * to activity context. It will cause memory leak. To avoid it we have to set drawable's callback as null. <br/>
	 * This method nullify callbacks for a given view and all of its children.
	 */
	public static void unbindDrawables(View view) {
        if(view.getBackground() != null) {
                view.getBackground().setCallback(null);
        }
        if(view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
	
}
