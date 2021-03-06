package ZtlApi;


import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;

import android.util.Log;

import android.os.SystemProperties;

import android.media.AudioManager;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.util.Map;


public class ZtlManager3128 extends ZtlManager {

    private String TAG = "Arctan";

    static final String SYSTEM_BAR_STATE = "persist.sys.systemBar";
    static final String SYSTEM_BAR_SHOW = "show";
    static final String SYSTEM_BAR_HIDE = "hide";

    private final static String SYS_NODE_VGA_MODES =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-VGA-1/modes";
    private final static String SYS_NODE_VGA_MODE =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-VGA-1/mode";

    private List<String> readStrListFromFile(String pathname) throws IOException {

        List<String> fileStrings = new ArrayList<>();
        File filename = new File(pathname);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            fileStrings.add(line);
        }
        Log.d(TAG, "readStrListFromFile - " + fileStrings.toString());
        return fileStrings;
    }

    private String readStrFromFile(String filename) throws IOException {
        Log.d(TAG, "readStrFromFile - " + filename);
        File f = new File(filename);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(f));
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        return line;
    }

    public void LwlTest(int a) {

        Log.d(TAG, "22LLLLL ----> " + a);
        try {
            readStrListFromFile(SYS_NODE_VGA_MODES);
            readStrFromFile(SYS_NODE_VGA_MODE);
            Log.d(TAG, getDisplayMode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ZtlManager3128() {
        DEBUG_ZTL = getSystemProperty("persist.sys.ztl.debug", "false").equals("true");
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
                if (files.length > 0) {
                    usbPath = files[0].getAbsolutePath();
                    LOGD("steve : get file path " + usbPath);
                    if (usbPath.contains("USB_DISK")) { //open USB_DISK
                        LOGD("steve : open " + usbPath);
                        File usbFile = new File(usbPath); //steve 5.1OS maybe /usbPath + /udisk0
                        if (usbFile.exists() && usbFile.isDirectory()) {
                            File[] usbFiles = usbFile.listFiles();
                            if (usbFiles.length > 0) {
                                usbPath = usbFiles[0].getAbsolutePath();    //udisk0
                                LOGD("steve : usbPath " + usbPath);
                            }
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

    //设置系统日期	1
    @Override
    public void setSystemDate(int year, int month, int day) {
        LOGD("set system Date " + year + "/" + month + "/" + day);
        Intent mIntent = new Intent("com.ztl.action.setdate");
        mIntent.putExtra("year", year);
        mIntent.putExtra("month", month);
        mIntent.putExtra("day", day);
        mContext.sendBroadcast(mIntent);
		/*
		  Calendar c = Calendar.getInstance();
		  c.set(Calendar.YEAR, year);
		  c.set(Calendar.MONTH, month-1);
		  c.set(Calendar.DAY_OF_MONTH, day);
		  long when = c.getTimeInMillis();
		  if(when / 1000 < Integer.MAX_VALUE){
		      ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		  }
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
        try {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    //减小音量，音量-1	1
    @Override
    public int setLowerSystemVolume() {
        try {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    //设置系统亮度值(需支持pwm设置)	1
    @Override
    public int setSystemBrightness(int brightness) {
        Log.d("Arctan", "ztl enter set ");
        try {
            if (brightness >= 0 && brightness <= 255) {
                try {
                    //   Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                    //   Uri uri = Settings.System.getUriFor("screen_brightness");
                    //   mContext.getContentResolver().notifyChange(uri, null);
                    Log.d("Arctan", "before send brodcast");
                    Intent mIntent = new Intent("ZTL.ACTION.SET.SYSTEMBRIGHTNESS");
                    mIntent.putExtra("ztl_brightness", brightness);
                    mContext.sendBroadcast(mIntent);

                } catch (Exception localException) {
                    localException.printStackTrace();
                }
            } else {
                LOGD("brightness index 0~255 , please check it");
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    //获取屏幕方向	1
    @Override
    public int getDisplayOrientation() {
        //	String state = getSystemProperty("persist.sys.ztlOrientation","0");
        String state = getSystemProperty("persist.sys.ztlOrientation", "0");
        return Integer.valueOf(state).intValue();
    }

    //获取当前GPIO的值
    public int Getcurrentgpio(int port) {
        execRootCmdSilent("cat /sys/class/gpio/gpio" + port + "/value");
        String value = null;
        try {
            value = readStrFromFile("/sys/class/gpio/gpio" + port + "/value");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Integer.parseInt(value);
    }


    //打开导航兰	1
    @Override
    public void setOpenSystemBar() {
        Intent systemBarIntent = new Intent("ZTL.ACTION.OPEN.SYSTEMBAR");
        mContext.sendBroadcast(systemBarIntent);
    }

    //隐藏导航兰	1
    @Override
    public void setCloseSystemBar() {
        Intent systemBarIntent = new Intent("ZTL.ACTION.CLOSE.SYSTEMBAR");
        mContext.sendBroadcast(systemBarIntent);
    }

    //获取状态栏状态	1
    @Override
    public int getSystemBarState() {
        int ret = -1;
        //String state = getSystemProperty(SYSTEM_BAR_STATE,"1");
        String state = SystemProperties.get(SYSTEM_BAR_STATE);
        if (state.equals(SYSTEM_BAR_SHOW)) {
            ret = 1;
        } else if (state.equals(SYSTEM_BAR_HIDE)) {
            return 0;
        }
        return ret;
    }

    //获取屏幕分辨率列表
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
    }

}
