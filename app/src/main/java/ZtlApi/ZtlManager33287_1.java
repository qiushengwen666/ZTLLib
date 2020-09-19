package ZtlApi;

import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.app.AlarmManager;


import android.app.PendingIntent;

public class ZtlManager33287_1 extends ZtlManager {
    String POWER_ON_TIME = "persist.sys.powerontime";
    String IS_OPEN_ALARM = "persist.sys.isopenalarm";
    String POWER_OFF_ALARM = "persist.sys.poweroffalarm";

    String ALARM_ON = "1";
    String ALARM_OFF = "0";

    String targetTime = "2019-5-4 18:00:00";

    private boolean DEBUG_ZTL = false;
    private String TAG = "ZTLManager3328_7.1";

    /*
     *	注意：调用休眠和休眠唤醒接口，需要声明使用权限，并且需要系统签名
     *	android:sharedUserId="android.uid.system
     *	<permission android:name="android.permission.DEVICE_POWER"></permission>
     */
    //休眠
    @Override
    public void goToSleep() {
        Intent mIntent = new Intent("com.ztl.action.boardstate");
        mIntent.putExtra("state", 0);
        mContext.sendBroadcast(mIntent);
/*
        PowerManager powerManager= (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("goToSleep", new Class[]{long.class}).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
*/
    }

    //休眠唤醒
    @Override
    public void wakeUp() {
        Intent mIntent = new Intent("com.ztl.action.boardstate");
        mIntent.putExtra("state", 1);
        mContext.sendBroadcast(mIntent);
/* 
       PowerManager powerManager= (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("wakeUp", new Class[]{long.class}).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
*/
    }


    //获取U盘路径	1
    @Override
    public String getUsbStoragePath() {
        String usbPath = null;
        String usbBasePath = "/storage/";

        File file = new File(usbBasePath);
        try {
            if (file.exists() && file.isDirectory()) { //open usb_storage
                File[] files = file.listFiles();
                //		if( files.length > 0){
                for (int i = 0; i < files.length; i++) {
                    usbPath = files[i].getAbsolutePath();
                    LOGD("shx : get file path " + usbPath);
                    if (usbPath.contains("udisk")) { //open USB_DISK
                        LOGD("shx : open " + usbPath);
                        File usbFile = new File(usbPath); //shx 3399 7.1OS maybe /storage/udisk0
                        if (usbFile.exists() && usbFile.isDirectory()) {
                            usbPath = usbFile.getAbsolutePath();    //udisk0
                            LOGD("shx : usbPath " + usbPath);
                            break;
                        }
                    }//end open USB_DISK
                }

            }//end open usb_storage
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return usbPath;
    }

    //设置系统的时区是否自动获取
    @Override
    public int setAutoTimeZone(int checked) {
        Intent mIntent = new Intent("com.ztl.action.autotimezone");
        mIntent.putExtra("checked", checked);
        mContext.sendBroadcast(mIntent);
        return 0;
/*		try{
		  android.provider.Settings.Global.putInt(mContext.getContentResolver(),
		          android.provider.Settings.Global.AUTO_TIME_ZONE, checked);
		   }catch(Exception e){
		   		e.printStackTrace();
		      return -1;
		   }
		   
		   return 0;
*/
    }

    //设置系统的时间是否需要自动获取
    @Override
    public int setAutoDateTime(int checked) {
        Intent mIntent = new Intent("com.ztl.action.autodatetime");
        mIntent.putExtra("checked", checked);
        mContext.sendBroadcast(mIntent);
/*
		 try {
		  android.provider.Settings.Global.putInt(mContext.getContentResolver(),
		          android.provider.Settings.Global.AUTO_TIME, checked);
		  } catch (Exception e) {
		   	e.printStackTrace();
		      return -1;
		  }
*/
        return 0;
    }


    //增大音量，音量+1	1
    @Override
    public int setRaiseSystemVolume() {
/*		try {
		 AudioManager am=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		// am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
		am.adjustVolume(AudioManager.ADJUST_RAISE, 0);
		 } catch (Exception e) {
		   	e.printStackTrace();
		      return -1;
		 }
*/
        int curVolume = getSystemCurrenVolume();
        return setSystemVolumeIndex(curVolume + 1);
    }

    //减小音量，音量-1	1
    @Override
    public int setLowerSystemVolume() {
/*		try {
			AudioManager am=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		//	am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
			am.adjustVolume(AudioManager.ADJUST_LOWER, 0); 
		} catch (Exception e) {
		   	e.printStackTrace();
		      return -1;
		 }
*/
        int curVolume = getSystemCurrenVolume();
        return setSystemVolumeIndex(curVolume - 1);
    }


    //设置系统亮度值(需支持pwm设置)	1
    @Override
    public int setSystemBrightness(int brightness) {
        Intent mIntent = new Intent("com.ztl.action.setbrightness");
        mIntent.putExtra("brightness", brightness);
        mContext.sendBroadcast(mIntent);
/*
    	try {
			if(brightness >=0 && brightness <= 255){
				try{  
				   Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
				   Uri uri = Settings.System.getUriFor("screen_brightness");
				   mContext.getContentResolver().notifyChange(uri, null);
				}catch (Exception localException){  
				   localException.printStackTrace();  
				}  
			}else{
				LOGD("brightness index 0~255 , please check it");
				return -1;
			}
		} catch (Exception e) {
		   	e.printStackTrace();
		      return -1;
		}
*/
        return 0;
    }

    /*
     *	注意：恢复出厂设置需要系统签名，另外需要声明权限
     *	android:sharedUserId="android.uid.system">
     *	<uses-permission android:name="android.permission.MASTER_CLEAR"/>
     */
    //恢复出厂设置	1
    @Override
    public void recoverySystem() {
        Intent clearIntent = new Intent("com.ztl.action.recovery");
        mContext.sendBroadcast(clearIntent);
/*
    	Intent clearIntent = new Intent("android.intent.action.MASTER_CLEAR");
		clearIntent.putExtra("isReformate", true);
		mContext.sendBroadcast(clearIntent);
*/
    }


    //获取屏幕方向	1
    @Override
    public int getDisplayOrientation() {
        String state = getSystemProperty("persist.sys.ztlOrientation", "0");
        return Integer.valueOf(state).intValue();
    }

    //获取USB调试状态	1

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public int getUsbDebugState() {
        String state = getSystemProperty("persist.usb.mode", "1");    //1 : disconnect to pc  0: connect to pc
        if (state.equals("0") || state.equals("2")) {
            state = "1";
        } else {
            state = "0";
        }
        return Integer.valueOf(state).intValue();
    }

    //获取状态栏状态	1
    @Override
    public int getSystemBarState() {
        String state = getSystemProperty("persist.sys.barState", "1");
        return Integer.valueOf(state).intValue();
    }

    //使能左右分屏功能
    @Override
    public void setSplitScreenLeftRightEnable(boolean isEnable) {
/*		if(isEnable){
			setSystemProperty("persist.sys.leftRightEnable","true");
		}else{
			setSystemProperty("persist.sys.leftRightEnable","false");
		}
*/
    }

    //使能上下分屏功能
    @Override
    public void setSplitScreenUpDownEnable(boolean isEnable) {
/*		if(isEnable){
			setSystemProperty("persist.sys.upDownEnable","true");
		}else{
			setSystemProperty("persist.sys.upDownEnable","false");
		}
*/
    }

    //显示-获取支持的分辨率列表
    @Override
    public String[] getScreenModes() {
        String displayModes;
        displayModes = getSystemProperty("persist.sys.displaymdoes", "");
        String modes[] = displayModes.split(",");
        return modes;
    }

    //设置分辨率		1
    @Override
    public void setScreenMode(String mode) {

        Intent setModeIntent = new Intent("android.ztl.action.SET_SCREEN_MODE");
        setModeIntent.putExtra("mode", mode);
        mContext.sendBroadcast(setModeIntent);

/*		int index = 0;
		int i=0;
		String framebufferMode = getSystemProperty("persist.sys.framebuffer.main","1920x1080");
		String modes[] = {"1920x1080","1600x900","1440x900","1366x768","1280x720","1280x1024","1024x768","800x600"};

		for(i = 0; i < modes.length; i++){
			if(mode.equals(modes[i])){
				framebufferMode = mode;
				break;
			}
		}

		LOGD("steve set framebuffer "+framebufferMode);
		setSystemProperty("persist.sys.framebuffer.main",framebufferMode);
		setSystemProperty("persist.sys.framebuffer.aux",framebufferMode);
	
		execRootCmdSilent("reboot");   
*/
    }

}
