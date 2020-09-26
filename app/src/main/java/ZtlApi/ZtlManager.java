package ZtlApi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import java.io.DataOutputStream;

import android.content.ComponentName;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import android.os.SystemClock;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.view.WindowManager;
import android.util.Log;
import android.text.TextUtils;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.os.storage.StorageManager;

import java.lang.reflect.Array;

import android.media.AudioManager;

import java.lang.reflect.InvocationTargetException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import android.app.AlarmManager;
import android.provider.Settings.SettingNotFoundException;

import java.io.FileReader;
import java.util.Enumeration;

import android.net.Uri;
import android.app.PendingIntent;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

//这个类是3288_5.1  todo 记得修改API版本号
//20200926 添加获取IPv4的IP地址
//20200924 添加是否背光反向接口，传入true为反向，false不反向。
//         添加获取背光是否方向接口返回值boolean，true为反向，false为不反向.
//20200921 修改设置DPI接口，原来的接口不可用，现在直接输入120-360之间的DPI即可。
//20200919 添加禁用/启动网络ADB 添加恢复出厂设置接口(Helper实现)
//20200918 添加设置亮度\增大亮度\减少亮度接口(Helper实现)
//         修复增加音量问题,增加\减少音量设置的是铃声,设置音量接口设置的是媒体音量.现在都改成设置媒体音量.
//20200916 废弃int类型的GPIO接口，以后只能使用字符串类型的GPIO接口。
//20200915 添加定时开关机接口由ZtlHelper实现，添加休眠唤醒接口ZtlHelper实现。添加设置桌面接口。
//         修改部分接口名字。
//20200911 添加获取运行内存接口。
//20200910 添加守护/取消守护进程接口，添加守护/取消守护服务接口(守护保持不被杀死，取消守护也不是杀死这个服务)。
//         新增设置系统时间接口，关机接口，重启接口。因为这些需要系统权限与系统签名，有的用户可能没有条件。交给ZtlHelper转发。
//20200901 修改定时开关机接口，恢复成以前的版本(把关于发送广播给ZtlHelper的都删了)。
//20200829 去除i2c-1 加密验证。
//         弃用设置和获取触摸方向。添加两个新的设置和触摸方向接口。
//20200824 添加停止自动重连wifi，指定wifi重连，需要输入wifi的ssid与密码。(配合Helper)
//20200820 添加获取联网方式接口，-1=未知 0=以太网 1=wifi 2=2G 3=3G 4=4G 5=5G
//20200813 定时开关机函数添加发送广播方式，Helper负责执行定时开关机。
//20200731 添加静默安装并重启、静默安装启动APP等函数。
//20200730 添加同步网络时间函数，发送广播，Helper负责执行同步网络时间。
//20200724 添加安装重启接口，发送安装广播，收到广播后进行安装与重启。
//20200722 添加时间-立刻同步网络时间、自定义同步周期，都是发送广播版本
//         需要配合智通利助手使用或者接收广播的方式。
//20200702 合并A33\A64 API，测试通过
//20200630 添加版本号，每次修改内容需更改
//20200622 合并RK系列的jar 测试各个板型的公版固件测试通过
//20200617 整理函数，按系统 显示 文件 网络 媒体等排列

public class ZtlManager {
    /**
     * @return todo 标识颜色：添加内容需要更改版本号
     */
    public String getAPIVersion() {
        return "2020/09/24";
    }

    protected Context mContext;
    boolean DEBUG_ZTL = false;
    static String TAG = "ZtlManager";
    String BlFile = "/proc/bl_root/bl_entry";
    String HdmiEnableFile = "/sys/class/display/HDMI/enable";
    String BlOn = "1";
    String BlOff = "0";

    String POWER_ON_TIME = "persist.sys.powerontime";
    String IS_OPEN_ALARM = "persist.sys.isopenalarm";

    String ALARM_ON = "1";
    String ALARM_OFF = "0";
    String LCD_DENSITY_PROP = "persist.sys.ztl_density";
    String CAMERA_ORIENTATION_PROP = "persist.sys.cameraOrientation";
    String TP_ORIENTATION_PROP = "persist.sys.tp.orientation";

    static ZtlManager Instance;
    CpuInfo cpuInfo;

    private native static int setScreenResolution(String path);

    static {
        //System.loadLibrary("screenset");
    }

    public static ZtlManager GetInstance() {

        if (Instance == null) {
            //根据设备类型和系统版本生成不同的对象
            String devType = getDeviceVersion();
            if (devType.contains("3288") && getAndroidVersion().contains("5.1")) {
                Instance = new ZtlManager();
            } else if (devType.contains("3399")) {
                Instance = new ZtlManager33997_1();
            } else if (devType.contains("3288") && getAndroidVersion().contains("7.1")) {
                Instance = new ZtlManager32887_1();
            } else if (devType.contains("3328")) {
                Instance = new ZtlManager33287_1();
            } else if (devType.contains("3368")) {
                Instance = new ZtlManager3368();
            } else if (devType.contains("3126") || devType.contains("3128")) {
                Instance = new ZtlManager3128();
            } else if (devType.contains("A64") || devType.contains("A33")) {
                Instance = new ZtlManagerA33_A64();
            }
            if (Instance == null) {
                Instance = new ZtlManager();
            }
        }

        return Instance;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    ZtlManager() {
        DEBUG_ZTL = getSystemProperty("persist.sys.ztl.debug", "false").equals("true");
    }

    void LOGD(String msg) {
        if (DEBUG_ZTL) {
            Log.d(TAG, msg);
        }
    }

    //系统-获取设备型号	返回RK3288之类的
    public static String getDeviceVersion() {
        return android.os.Build.MODEL;
    }

    //系统-获取系统版本	返回5.1之类的
    @Deprecated
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    //系统-获取安卓版本号
    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    //系统-获取SDK版本    返回22 23之类的
    public static int getSDKVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    //系统-获取设备唯一ID	1
    public String getDeviceID() {
        BufferedReader bre = null;
        String lineInfo;
        String cpuSerial;

        File cpuInfo = new File("/proc/cpuinfo");
        if (!cpuInfo.exists()) {
            LOGD("/proc/cpuinfo not found!");
            return null;
        }

        try {
            bre = new BufferedReader(new FileReader(cpuInfo));
            while ((lineInfo = bre.readLine()) != null) {
                if (!lineInfo.contains("Serial")) {
                    continue;
                }
                LOGD(lineInfo.length() + lineInfo);

                cpuSerial = lineInfo.substring(lineInfo.indexOf(":") + 2);
                LOGD(cpuSerial);
                return cpuSerial;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //系统-存储-获取内部存储大小，单位：字节 	(储存器容量)
    public long getTotalInternalMemorySize() {
        //获取内部存储根目录
        File path = Environment.getDataDirectory();
        //系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        //每个区块占字节数
        long blockSize = stat.getBlockSize();
        //区块总数
        long totalBlocks = stat.getBlockCount();

        long a = totalBlocks * blockSize;
        //经过处理的内存大小
        long b = a / 1024 / 1024 / 1024;    //单位：GB

        return a;//单位：字节
    }

    //系统-获取运行内存,单位：字节
    public long getFreeMemorySize() {
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(outInfo);
        long avaliMem = outInfo.availMem;
        return avaliMem;
    }

    //系统-存储-获取内部SD卡路径	1
    public String getInternalSDCardPath() {
        String path = null;
        path = System.getenv("EXTERNAL_STORAGE");
        return path;
    }

    //系统-存储-获取外部SD卡路径	1
    public String getExternalSDCardPath() {
        String path = null;
        path = getAppRootOfSdCardRemovable();
        return path;
    }

    //系统-储存-获取外部SD卡路径
    private String getAppRootOfSdCardRemovable() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return null;
        }
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        /**
         * 这一句取的还是内置卡的目录。
         * /storage/emulated/0/Android/data/com.newayte.nvideo.phone/cache
         * 神奇的是，加上这一句，这个可移动卡就能访问了。
         * 猜测是相当于执行了某种初始化动作。
         */
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                if ((Boolean) isRemovable.invoke(storageVolumeElement)) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //系统-存储-获取内部U盘路径	1
    public String getUsbStoragePath() {
        String usbPath = null;
        String usbBasePath = "/mnt/usb_storage/";

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

    /*
     *	注意：调用休眠和休眠唤醒接口，需要声明使用权限，并且需要系统签名
     *	android:sharedUserId="android.uid.system"
     *	<permission android:name="android.permission.DEVICE_POWER"></permission>
     */

    //系统-休眠 需要系统签名，所以交给Helper
    @Deprecated
    public void goToSleep() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("goToSleep", new Class[]{long.class}).
                    invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    //系统-休眠
    public void sleep() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",
                "com.ztl.helper.ZTLHelperService");

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "sleep");
        mContext.startService(intent);

    }

    //系统-休眠唤醒  需要系统签名，所以交给Helper
    @Deprecated
    public void wakeUp() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("wakeUp", new Class[]{long.class})
                    .invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    //唤醒-休眠唤醒
    public void awake() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "awake");
        mContext.startService(intent);
    }

    //系统-关机	陆工说太暴力了，需要转发给ZtlHelper
    @Deprecated
    public void shutDownSystem() {
        String cmd = "reboot -p";
        execRootCmdSilent(cmd);
    }

    //系统-关机     ZtlHelper实现
    public void shutdown() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "shutdown");
        mContext.startService(intent);
    }

    //系统-重启	转发给ZtlHelper
    @Deprecated
    public void rebootSystem() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Intent intent = new Intent("reboot");
        mContext.sendBroadcast(intent);

        String cmd = "reboot";
        execRootCmdSilent(cmd);
    }

    //系统-重启     ZtlHelper实现  参数传入延迟时间，如果要马上执行传入0即可。
    public void reboot(int delay) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "reboot");
        intent.putExtra("delay", delay);
        mContext.startService(intent);
    }

    //系统-截取当前屏幕
    public void startScreenShot(String path, String fileName) {
        if (isExist(path)) {
            String filePath = path + "/" + fileName;
            String screenShotCmd = "screencap -p " + filePath;
            execRootCmdSilent(screenShotCmd);
        } else {
            Log.e(TAG, "file path " + path + " not exist");
        }
    }

    //系统-设置开机自启动APP包名和Activity
    public int setBootPackageActivity(String pkgName, String pkgActivity) {
        if (pkgName != null && pkgActivity != null) {
            setSystemProperty("persist.sys.bootPkgName", pkgName);
            setSystemProperty("persist.sys.bootPkgActivity", pkgActivity);
        } else {
            Log.e(TAG, "pkgName (" + pkgName + ") or pkgActivity (" + pkgActivity + ") err");
            return -1;
        }
        return 0;
    }

    //系统-APP-获取设置的开机自启动APP包名
    public String getBootPackageName() {
        String PkgName = "";
        PkgName = getSystemProperty("persist.sys.bootPkgName", "unKnown");
        return PkgName;
    }

    //系统-APP-获取设置的开机自启动APP ACTIVITY名
    public String getBootPackageActivity() {
        String pkgActivity = "";
        pkgActivity = getSystemProperty("persist.sys.bootPkgActivity", "unKnown");
        return pkgActivity;
    }

    //系统-APP-启动另一个APP
    public void startActivity(String pkgName, String pkgActivity) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        if (pkgName != null && pkgActivity != null) {
            try {
                ComponentName componetName = new ComponentName(pkgName, pkgActivity);
                Intent gameIntent = new Intent();
                gameIntent.setComponent(componetName);
                mContext.startActivity(gameIntent);
            } catch (Exception e) {
                LOGD("start app (" + pkgName + "," + pkgActivity + ") fail!");
            }
        } else {
            Log.e(TAG, "pkg is null please check it");
        }
    }

    //系统-APP-静默安装APK
    public void installAppSilent(String filePath) {
        if (isExist(filePath) == false) {
            Log.e(TAG, "file [" + filePath + "] not isExist");
            return;
        }
        if (filePath.contains(".apk") == false) {
            Log.e(TAG, "file [" + filePath + "] 后缀不合法");
            return;
        }
//        if (fileType.equals("apk") == false) {
//            Log.e(TAG, "file [" + fileType + "] type invalid");
//            return;
//        }
        execRootCmdSilent("pm install -r " + filePath);

    }

    //系统-APP-卸载APP
    public void uninstallAppSilent(String packageName) {
        try {
            execRootCmdSilent("pm uninstall " + packageName);
        } catch (Exception e) {
            Log.e(TAG, "uninstall package " + packageName + " faild");
        }
    }

    //系统-APP-静默安装APK并且重启    ZtlHelper实现
    public void installAppSilentAndRebootSystem(String filePath, int reboot_in_sec) {
        reboot(reboot_in_sec);
        installAppSilent(filePath);
    }

    //系统-APP-APP完成安装后启动APP   ZtlHelper实现
    public void installAppAndStartUp(String filePath, String pkgName) {

        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }

        ComponentName componetName1 = new ComponentName("com.ztl.helper", "com.ztl.helper.ZTLHelperService");
        Intent intent1 = new Intent();
        intent1.setComponent(componetName1);
        intent1.putExtra("cmd", "start_up_app");//value填的需要和ztlhelper统一
        intent1.putExtra("package", pkgName);
        mContext.startService(intent1);

        Intent intent = new Intent();
        ComponentName componetName = new ComponentName("com.ztl.helper", "com.ztl.helper.ZTLHelperService");
        intent.setComponent(componetName);
        intent.putExtra("cmd", "install");//value填的需要和ztlhelper统一
        intent.putExtra("filepath", filePath);
        intent.putExtra("package", pkgName);
        mContext.startService(intent);

    }

    //ZtlHelper安装APK        ZtlHelper实现
    public void installApp(String filePath, String pkgName) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "install");//value填的需要和ztlhelper统一
        intent.putExtra("filepath", filePath);
        intent.putExtra("package", pkgName);
        mContext.startService(intent);

    }

    //系统-守护某个进程,保持置定     appservice实现
    public void keepActivity(String pkgName) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName("com.ztl.appservice",
                "com.ztl.appservice.BasicService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "keepActivity");//value填的需要和ztlhelper统一
        intent.putExtra("package", pkgName);
        mContext.startService(intent);
    }

    //系统-取消守护某个进程       appservice实现
    public void unKeepActivity() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.appservice",
                "com.ztl.appservice.BasicService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "unkeepActivity");//value填的需要和ztlhelper统一
        mContext.startService(intent);
    }

    //系统-守护某个服务，保持不被清除  appservice实现
    public void keepService(String package_name, String service) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.appservice",
                "com.ztl.appservice.BasicService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "keepService");//value填的需要和ztlhelper统一
        intent.putExtra("package", package_name);
        intent.putExtra("service", service);
        mContext.startService(intent);
    }

    //系统-取消守护某个服务,不是杀死这个服务  appservice实现
    public void unkeepService() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.appservice",
                "com.ztl.appservice.BasicService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "unkeepService");//value填的需要和ztlhelper统一
        mContext.startService(intent);
    }

    //系统-设置系统桌面 todo 如果要恢复成默认桌面，参数传入null
    public void setLauncher(String pkgage, String Activity) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "setLauncher");//value填的需要和ztlhelper统一(ztlhelper执行动作)
        intent.putExtra("package", pkgage);
        intent.putExtra("activity", Activity);
        mContext.startService(intent);
    }

    //系统-判断包名对应的APP是否存在
    public boolean isAppExist(String pkgName) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return false;
        }
        ApplicationInfo info;
        try {
            info = mContext.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            info = null;
        }

        return info != null;
    }

    /*
     *	注意：恢复出厂设置需要系统签名，另外需要声明权限
     *	android:sharedUserId="android.uid.system">
     *	<uses-permission android:name="android.permission.MASTER_CLEAR"/>
     */
    //系统-恢复出厂设置	1
    @Deprecated
    public void recoverySystem() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Intent clearIntent = new Intent("android.intent.action.MASTER_CLEAR");
        clearIntent.putExtra("isReformate", true);
        mContext.sendBroadcast(clearIntent);
    }

    //恢复出厂设置 因为需要权限，交给ZtlHelper执行
    public void resetSystem() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }

        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "resetSystem");
        mContext.startService(intent);

    }

    //系统-打开设置界面	1
    public int startSettings() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        try {
            mContext.startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception e) {
            LOGD("start settings fail!");
            return -1;
        }
        return 0;
    }

    //系统-打开wifi设置界面	1
    public int startWifiSettings() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        try {
            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } catch (Exception e) {
            LOGD("start wifi settings fail!");
            return -1;
        }
        return 0;
    }

    //系统-显示导航栏与状态栏	1
    public void setOpenSystemBar() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Intent systemBarIntent = new Intent("com.ding.systembar.chang");
        String str = "0";
        systemBarIntent.putExtra("enable", str);
        mContext.sendBroadcast(systemBarIntent);
    }

    //系统-隐藏导航栏与状态栏	1
    public void setCloseSystemBar() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Intent systemBarIntent = new Intent("com.ding.systembar.chang");
        String str = "1";
        systemBarIntent.putExtra("enable", str);
        mContext.sendBroadcast(systemBarIntent);
    }

    //系统-是否已打开导航栏与状态栏	1

    /**
     * @deprecated
     */
    @Deprecated
    public int getSystemBarState() {
        String state = getSystemProperty("persist.sys.barState", "1");
        int value = Integer.parseInt(state);
        int ret = 0;
        if (value == 0) {
            ret = 1;
        }
        return ret;
    }

    //系统-是否已打开导航栏与状态栏
    public boolean isSystemBarOpen() {
        String state = getSystemProperty("persist.sys.barState", "1");
        int value = Integer.parseInt(state);
        return value != 0;
    }

    //系统-打开USB调试	1
    public void setOpenUsbDebug() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Intent systemBarIntent = new Intent("com.ding.adbsetting");
        String str = "1";
        systemBarIntent.putExtra("enable", str);
        mContext.sendBroadcast(systemBarIntent);
    }

    //系统-关闭USB调试	1
    public void setCloseUsbDebug() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Intent systemBarIntent = new Intent("com.ding.adbsetting");
        String str = "0";
        systemBarIntent.putExtra("enable", str);
        mContext.sendBroadcast(systemBarIntent);
    }

    //系统-USB调试是否打开
    public boolean isUsbDebugOpen() {
        String state = getSystemProperty("persist.sys.adbState", "1");
        int instate = Integer.valueOf(state).intValue();
        if (instate == 1) {
            return true;
        }
        return false;
    }

    /**
     * @deprecated
     */
    @Deprecated
    //系统-获取USB调试状态
    public int getUsbDebugState() {
        String state = getSystemProperty("persist.sys.adbState", "1");
        return Integer.valueOf(state).intValue();
    }

    //系统-获取OTG口连接状态 //勾中的时候是2 不勾的时候是1
    public boolean getUSBtoPC() {
        try {
            String state = loadFileAsString("/sys/bus/platform/drivers/usb20_otg/force_usb_mode");
            if (state.contains("2")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //系统-设置OTG口连接状态
    public void setUSBtoPC(boolean toPC) {
        if (toPC) {
            setSystemProperty("persist.usb.mode", "2");
            writeMethod("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", "2");
        } else {
            setSystemProperty("persist.usb.mode", "1");
            writeMethod("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", "1");
        }
    }

    //系统-设置生成序列号
    public int setBuildSerial(String sn) {
        int ret = 0;
        if (sn != null) {
            setSystemProperty("persist.sys.ztlsn", sn);
        }

        return ret;
    }

    //系统-获取生成的序列号
    public String getBuildSerial() {
        String sn = "";
        sn = getSystemProperty("persist.sys.ztlsn", "unknown");

        return sn;
    }

    //系统-su执行命令行
    public int execRootCmdSilent(String cmd) {
        int result = -1;
        try {
            result = _execCmdAsSU("su", cmd);
        } catch (Exception e) {
            String error = e.toString();
            if (error.contains("Directory: null Environment: null") || error.contains("Permission")) {
                Log.e(TAG, "无SU执行权限,正在尝试testsu");
                try {
                    result = _execCmdAsSU("testsu", cmd);
                } catch (Exception ex) {
                    e.printStackTrace();
                    Log.e(TAG, "此函数连接失败，请联系厂家解决");
                }
            } else {
                Log.e(TAG, "testsu的权限不通过");
                e.printStackTrace();
                Log.e(TAG, "无SU执行权限,请联系厂家解决");
                return -1;
            }

        }
        return result;
    }

    int _execCmdAsSU(String strSU, String cmd) throws Exception {
        Process p;
        p = Runtime.getRuntime().exec(strSU);
        DataOutputStream dos = new DataOutputStream(p.getOutputStream());

        dos.writeBytes(cmd + "\n");
        dos.flush();
        dos.writeBytes("exit\n");
        dos.flush();
        int result = p.waitFor();
        dos.close();

        return result;
    }

    //时间-获取系统日期	1
    public String getSystemDate() {
        String date = "";
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthOfYear = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        date = year + "/" + monthOfYear + "/" + dayOfMonth;
        return date;
    }

    //时间-获取系统时间	1
    public String getSystemTime() {
        String time = "";
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        time = hour + ":" + minute + ":" + second;
        return time;
    }

    //时间-发送同步时间广播   ZtlHelper实现
    public void syncNetworkTime() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "sync_time_now");//value填的需要和ztlhelper统一
        mContext.startService(intent);

    }

    //时间-发送同步时间周期       ZtlHelper实现
    public void setSyncNetworkTimePeroid(int peroid_in_minute) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "sync_time_period");//value填的需要和ztlhelper统一
        intent.putExtra("peroid", peroid_in_minute);
        mContext.startService(intent);

    }

    /**
     * 设置系统日期和时间，需要系统签名
     */
    //时间-设置系统日期
/*	public void setSystemDate(int year,int month,int day){
		  LOGD("set system Date "+year+"/"+month+"/"+day);
		  Calendar c = Calendar.getInstance();
		  c.set(Calendar.YEAR, year);
		  c.set(Calendar.MONTH, month+1);
		  c.set(Calendar.DAY_OF_MONTH, day);
		  long when = c.getTimeInMillis();
		  if(when / 1000 < Integer.MAX_VALUE){
		      ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		  }
	}*/

    //时间-设置系统日期	1
    public void setSystemDate(int year, int month, int day) {
        LOGD("set system Date " + year + "/" + month + "/" + day);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
    }

    //时间-设置系统时间	1
    @RequiresPermission(Manifest.permission.SET_TIME)
    public void setSystemTime(int hour, int minute, int second, int millisecond) {

        LOGD("set system time " + hour + ":" + minute);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, millisecond);
        SystemClock.setCurrentTimeMillis(c.getTimeInMillis());

    }

    //时间-设置系统时间     ZtlHelper实现
    //注意：使用此接口月份需要-1 因为需要系统签名 给Helper实现
    public void setSystemTime(Calendar cal) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "setSystemTime");//value填的需要和ztlhelper统一
        intent.putExtra("time", cal.getTimeInMillis());
        mContext.startService(intent);

    }

    //时间-设置系统日期与时间
    @RequiresPermission(Manifest.permission.SET_TIME)
    @Deprecated
    public void setSystemDateAndTime(int year, int month, int day, int hour, int minute) {

        //Log.d("test","setDateTimeA33");
        Calendar c = Calendar.getInstance();

        c.set(YEAR, Integer.parseInt(String.valueOf(year)));
        c.set(MONTH, Integer.parseInt(String.valueOf(month)) - 1);
        c.set(DAY_OF_MONTH, Integer.parseInt(String.valueOf(day)));
        c.set(HOUR_OF_DAY, Integer.parseInt(String.valueOf(hour)));
        c.set(MINUTE, Integer.parseInt(String.valueOf(minute)));

        long when = c.getTimeInMillis();
        if (when / 1000L < 2147483647L) {
            SystemClock.setCurrentTimeMillis(when);
        }

    }

    //时间-判断系统的时区是否是自动获取的	1
    public boolean getTimeZoneAuto() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return false;
        }
        try {
            return android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME_ZONE) > 0;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    //时间-设置系统的时区是否自动获取
    public int setAutoTimeZone(int checked) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        try {
            android.provider.Settings.Global.putInt(mContext.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME_ZONE, checked);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    //时间-判断系统的时间是否自动获取的	1
    public boolean getDateTimeAuto() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return false;
        }
        try {
            return android.provider.Settings.Global.getInt(mContext.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME) > 0;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


    //时间-设置系统的时间是否需要自动获取
    public int setAutoDateTime(int checked) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        try {
            android.provider.Settings.Global.putInt(mContext.getContentResolver(),
                    android.provider.Settings.Global.AUTO_TIME, checked);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    /***3399 api****/
    //时间-设置定时开机时间   目前测试用的这个
    //存在一个问题，如果设置好定时开关机，再把系统时间往过去的时间调整，会导致执行不开关机。
    @Deprecated
    public void setSchedulePowerOn(int hour, int minute, boolean enableSchedulPowerOn) {

        long now = System.currentTimeMillis();
        long now_totalSeconds = now / 1000;

        int year;
        int month;
        int day;
        long set_totalSeconds = 0;

        Calendar c = Calendar.getInstance();

        if (enableSchedulPowerOn) {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            c.set(year, month, day, hour, minute, 0);

            set_totalSeconds = c.getTimeInMillis() / 1000;

            setSystemProperty(POWER_ON_TIME, set_totalSeconds + "");
            setSystemProperty(IS_OPEN_ALARM, ALARM_ON);
            setSystemProperty("persist.sys.iseverydayalarm", "1");
        } else {
            setSystemProperty(POWER_ON_TIME, "0");
            setSystemProperty(IS_OPEN_ALARM, ALARM_OFF);
        }

        Log.d(TAG, "shx get now " + now_totalSeconds + " ,set time " + set_totalSeconds + " sult " + (set_totalSeconds - now_totalSeconds));
    }

    //时间-设置定时关机时间  每天   目前测试用的这个
    @Deprecated
    public void setSchedulePowerOff(int hour, int minute, boolean enableSchedulPowerOff) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        if (enableSchedulPowerOff == false) {
            setSystemProperty("persist.sys.powerOffTime", "unknown");
            setSystemProperty("persist.sys.powerOffEnable", "false");
            return;
        }

        Calendar c = Calendar.getInstance();
        long curTime = c.getTimeInMillis();

        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        long targetTime = c.getTimeInMillis();
        if (targetTime < curTime) {
            c.add(Calendar.DAY_OF_MONTH, 1);
            targetTime = c.getTimeInMillis();
        }

        Intent intent = new Intent("com.android.settings.action.REQUEST_POWER_OFF");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        setSystemProperty("persist.sys.powerOffTime", hour + ":" + minute);
        setSystemProperty("persist.sys.powerOffEnable", "true");
        setSystemProperty("persist.sys.powerOffEveryday", "true");
        targetTime = targetTime / 1000;
        setSystemProperty("persist.sys.powerOffTimeMillis", targetTime + "");
        Log.i(TAG, "Next time power off " + hour + ":" + minute);

    }

    //时间-定时开机0，指定某一天 待测试 这里月份是否要减一 测试结果：month确实要-1才能用   目前测试用的这个
    @Deprecated
    public void setPowerOnAlarm(int year, int month, int day, int hour, int minute, boolean enableSchedulPowerOn) {

        if (enableSchedulPowerOn == false)
            return;

        Calendar cal = Calendar.getInstance();
        month -= 1;
        cal.set(year, month, day, hour, minute, 0);

        setSystemProperty(POWER_ON_TIME, cal.getTimeInMillis() / 1000 + "");
        setSystemProperty(IS_OPEN_ALARM, ALARM_ON);
        setSystemProperty("persist.sys.iseverydayalarm", "0");

    }

    //时间-定时关机，指定某一天 目前测试用的这个
    @Deprecated
    public void setPowerOffAlarm(int year, int month, int day, int hour, int minute, boolean enableSchedulPowerOff) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        Calendar c = Calendar.getInstance();
        long curTime = c.getTimeInMillis() / 1000;
        if (enableSchedulPowerOff == false) {
            setSystemProperty("persist.sys.powerOffTime", "unknown");
            setSystemProperty("persist.sys.powerOffEnable", "false");
        }
        month = month - 1;
        c.set(year, month, day, hour, minute, 0);
        long targetTime = c.getTimeInMillis() / 1000;

        Log.d(TAG, "set false tar " + targetTime + " cur" + curTime);
        if (targetTime < curTime) {
            Log.d(TAG, "set false tar " + targetTime + " cur" + curTime);
            setSystemProperty("persist.sys.powerOffEnable", "false");
            return;
        }

        Log.d(TAG, "set next time power off " + year + "/" + (month) + "/" + day + " " + hour + ":" + minute);
        Intent intent = new Intent("com.android.settings.action.REQUEST_POWER_OFF");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        setSystemProperty("persist.sys.powerOffTime", hour + ":" + minute);
        setSystemProperty("persist.sys.powerOffEnable", "true");
        setSystemProperty("persist.sys.powerOffEveryday", "false");
        setSystemProperty("persist.sys.powerOffTimeMillis", targetTime + "");
    }

    //时间-定时关机，转发给Helper
    public void timingOff(int hour, int minute, int weekdays) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "timingOff"); //value填的需要和ztlhelper统一
        intent.putExtra("hour", hour);  //这里填要传入的参数，第一个name需要和ztlhelper统一
        intent.putExtra("minute", minute);
        intent.putExtra("weekdays", weekdays);

        mContext.startService(intent);
    }

    //时间-定时开机，转发给Helper
    public void timingOn(int hour, int minute, int weekdays) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "timingOn"); //value填的需要和ztlhelper统一
        intent.putExtra("hour", hour);  //这里填要传入的参数，第一个name需要和ztlhelper统一
        intent.putExtra("minute", minute);
        intent.putExtra("weekdays", weekdays);

        mContext.startService(intent);
    }

    //时间-辅助函数
    public static long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
            Log.d("steve", " " + date.getYear() + " " + date.getMonth() + " " + date.getDay() + " " + date.getHours() + " " + date.getMinutes() + " " + date.getSeconds());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return date.getTime();
    }

    //显示-获取屏幕分辨率	1
    public String getDisplayMode() {
        String Mode = getSystemProperty("persist.sys.screenmode", "0");
        return Mode;
    }

    //显示-获取屏幕y轴像素	1
    public int getDisplayHeight() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        int height = metrics.heightPixels;
        return height;
    }

    //显示-获取屏幕x轴像素	1
    public int getDisplayWidth() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        return width;
    }

    //显示-获取显示密度(dpi)
    public int getDisplayDensity() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);

        int density = metrics.densityDpi;
        return density;
    }

    //显示-设置dpi
    public void setDisplayDensity(int dpis) {

        Log.d(TAG, "set lcd density value = " + dpis);

        //先从源码拿
        String lcdDensity = getSystemProperty("ro.sf.lcd_density", "160");
        //再从系统变量拿
        int ztl_density = Integer.parseInt(getSystemProperty(LCD_DENSITY_PROP, "0"));
        //如果系统变量有效 以系统变量为准
        if (ztl_density > 0) {
            lcdDensity = ztl_density + "";
        }

        switch (dpis) {
            case 0:
                lcdDensity = "120";
                break;
            case 1:
                lcdDensity = "160";
                break;
            case 2:
                lcdDensity = "240";
                break;
            case 3:
                lcdDensity = "320";
                break;
            default: {
                dpis = Math.abs(dpis);
                lcdDensity = String.valueOf(dpis);
            }
        }
        execRootCmdSilent("wm density " + dpis);
        setSystemProperty(LCD_DENSITY_PROP, lcdDensity);
        execRootCmdSilent("reboot");
/*
	   Intent intent2=new Intent(Intent.ACTION_REBOOT);
	   intent2.putExtra("nowait", 1);
	   intent2.putExtra("interval", 1);
	   intent2.putExtra("window", 0);
	   mContext.sendBroadcast(intent2);
*/
    }

    //是否设置背光反向
    public void reverseBrighness(boolean true_or_false) {
        if (true_or_false) {
            setSystemProperty("persist.ztl.reverseBri", "1");
        } else {
            setSystemProperty("persist.ztl.reverseBri", "0");
        }
    }

    //获取背光是否已反向
    public boolean isReverseBrighness() {
        String getProperty = getSystemProperty("persist.ztl.reverseBri", "1");
        if (getProperty.equals("1")) {
            return true;
        } else
            return false;
    }

    //显示-调整LCD背光
    public int setLcdBackLight(int status) {
        int ret = -1;
        if (isExist(this.BlFile)) {
            if (status == 1) {
                writeMethod(this.BlFile, this.BlOn);
                ret = 0;
            } else if (status == 0) {
                writeMethod(this.BlFile, this.BlOff);
                ret = 0;
            } else {
                ret = -1;
                Log.e(TAG, "status illegal");
            }
        } else {
            ret = -1;
            Log.e(TAG, "lcd bl node not found");
        }

        if (isExist(this.HdmiEnableFile)) {
            if (status == 1) {
                writeMethod(this.HdmiEnableFile, this.BlOn);
                ret = 0;
            } else if (status == 0) {
                writeMethod(this.HdmiEnableFile, this.BlOff);
                ret = 0;
            } else {
                ret = -1;
                Log.e(TAG, "status illegal");
            }
        } else {
            ret = -1;
            Log.e(TAG, "hdmi enable node not found");
        }

        return ret;
    }

    //显示-获取当前亮度	1
    public int getSystemBrightness() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    //显示-获取最大亮度值
    public int getSystemMaxBrightness() {
        return 255;
    }

    //显示-调大亮度 +1	1
    //需要权限：
    // android:sharedUserId ="android.uid.system"
    //<permission android:name="android.permission.WRITE_SETTINGS" />
    @Deprecated
    public int setRaiseSystemBrightness() {
        int curBrightnss = getSystemBrightness();
        return setSystemBrightness(curBrightnss + 1);
    }

    //显示-调低亮度 -1	1
    //需要权限：
    // android:sharedUserId ="android.uid.system"
    //<permission android:name="android.permission.WRITE_SETTINGS" />
    @Deprecated
    public int setLowerSystemBrightness() {
        int curBrightnss = getSystemBrightness();
        return setSystemBrightness(curBrightnss - 1);
    }

    //调大亮度 因为需要系统权限，交给ZtlHelper
    public void increaseBrightness() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "increaseBrightness"); //value填的需要和ztlhelper统一

        mContext.startService(intent);
    }

    //降低亮度 因为需要系统权限，交给ZtlHelper
    public void decreaseBrightness() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "decreaseBrightness"); //value填的需要和ztlhelper统一

        mContext.startService(intent);
    }

    //显示-设置亮度值(需支持pwm设置)	1
    //需要权限：
    //android:sharedUserId ="android.uid.system"
    //<permission android:name="android.permission.WRITE_SETTINGS" />
    @Deprecated
    public int setSystemBrightness(int brightness) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        try {
            if (brightness >= 0 && brightness <= 255) {
                try {
                    Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                    Uri uri = Settings.System.getUriFor("screen_brightness");
                    mContext.getContentResolver().notifyChange(uri, null);
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

    //设置亮度 因为需要系统权限，交给ZtlHelper
    public void setBrightness(int brightness) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "setBrightness"); //value填的需要和ztlhelper统一
        intent.putExtra("brightness", brightness);

        mContext.startService(intent);
    }

    //显示-设置屏幕方向 传入0 90 180 270
    public void setDisplayOrientation(int rotation) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        if (rotation == getDisplayOrientation()) {
            Log.e("当前方向", "与旋转方向一致，不执行");
            return;
        }
        int oritation = rotation / 90;
        try {
            Intent oritationIntent = new Intent("ACTION_ZTL_ROTATION");
            oritationIntent.putExtra("rotation", oritation);
            mContext.sendBroadcast(oritationIntent);
        } catch (Exception exc) {
            Log.e(TAG, "set rotation err!");
        }
    }

    //显示-获取屏幕方向	1
    public int getDisplayOrientation() {
        String state = getSystemProperty("persist.ztl.hwrotation", "0");
        return Integer.parseInt(state);
    }

    //显示-获取触摸方向
    @Deprecated
    public int getTpOrientation() {
        String value = getSystemProperty(TP_ORIENTATION_PROP, "0");
        int ret = Integer.valueOf(value).intValue();
        return ret;
    }

    //显示-设置触摸方向
    @Deprecated
    public void setTpOrientation(int orientation, boolean rebootnow) {
        if (orientation < 0 || orientation > 3) {
            return;
        }
        String str = (Integer.toString(orientation));
        try {
            setSystemProperty(TP_ORIENTATION_PROP, str);
        } catch (Exception exc) {
            return;
        }
        if (rebootnow) {
            execRootCmdSilent("reboot");
        }
    }

    //显示-设置触摸方向
    public void setTouchOrientation(int orientation, boolean rebootnow) {
        orientation /= 90;
        String str = (Integer.toString(orientation));
        try {
            setSystemProperty(TP_ORIENTATION_PROP, str);
        } catch (Exception exc) {
            return;
        }
        if (rebootnow) {
            execRootCmdSilent("reboot");
        }
    }

    //显示-获取触摸方向
    public int getTouchOrientation() {
        String value = getSystemProperty(TP_ORIENTATION_PROP, "0");
        int ret = Integer.valueOf(value).intValue();
        return ret * 90;
    }

    //显示-使能左右分屏功能
    public void setSplitScreenLeftRightEnable(boolean isEnable) {
        if (isEnable) {
            setSystemProperty("persist.sys.leftRightEnable", "true");
        } else {
            setSystemProperty("persist.sys.leftRightEnable", "false");
        }
    }

    //显示-使能上下分屏功能
    public void setSplitScreenUpDownEnable(boolean isEnable) {
        if (isEnable) {
            setSystemProperty("persist.sys.upDownEnable", "true");
        } else {
            setSystemProperty("persist.sys.upDownEnable", "false");
        }
    }

    //显示-获取支持的分辨率列表
    public String[] getScreenModes() {
        Log.e("ztllib", "unsupport fucntion now for this board.todo later.");
        return null;
    }

    //显示-设置分辨率 读.so文件
    public void setResolution(String mode) {
//        String path = "/sdcard/Download/" + mode + ".bin";
//        String cmd = "wp " + path;
//        Log.e("cmd", "" + cmd);
//        //execRootCmdSilent(cmd);
//        setSystemProperty("persist.sys.screenmode", mode);
//        execRootCmdSilent(path);
//        setScreenResolution(path);

    }

    //显示-设置分辨率		1
    public void setScreenMode(String mode) {
        int index = 5;
        String cmd = "lcdparamservice ";

        boolean splitScreenLeftRightEnable = false;
        boolean splitScreenUpDownEnable = false;

        splitScreenLeftRightEnable = getSystemProperty("persist.sys.leftRightEnable", "false").equals("true");
        splitScreenUpDownEnable = getSystemProperty("persist.sys.upDownEnable", "false").equals("true");

        if (mode != null) {
            if (mode.equals("800x600@60p")) {
                index = 1;
            } else if (mode.equals("1024x768@60p")) {
                index = 2;
            } else if (mode.equals("1280x1024@60p")) {
                index = 3;
            } else if (mode.equals("1280x720@60p")) {
                index = 4;
            } else if (mode.equals("1366x768@60p")) {
                index = 14;
            } else if (mode.equals("1440x900@60p")) {
                index = 15;
            } else if (mode.equals("1600x900@60p")) {
                index = 16;
            } else if (mode.equals("1920x1080@60p")) {
                index = 5;
            } else if (mode.equals("1600x600@60p")) {//左右
                if (splitScreenLeftRightEnable) {
                    index = 6;
                } else {
                    index = 1;
                }
            } else if (mode.equals("2048x768@60p")) {
                if (splitScreenLeftRightEnable) {
                    index = 7;
                } else {
                    index = 2;
                }
            } else if (mode.equals("2560x720@60p")) {
                if (splitScreenLeftRightEnable) {
                    index = 8;
                } else {
                    index = 4;
                }
            } else if (mode.equals("2732x768@60p")) {
                if (splitScreenLeftRightEnable) {
                    index = 17;
                } else {
                    index = 14;
                }
            } else if (mode.equals("2880x900@60p")) {
                if (splitScreenLeftRightEnable) {
                    index = 18;
                } else {
                    index = 15;
                }
            } else if (mode.equals("3200x900@60p")) {
                if (splitScreenLeftRightEnable) {
                    index = 19;
                } else {
                    index = 16;
                }
            } else if (mode.equals("3840x1080@60p")) {//左右
                if (splitScreenLeftRightEnable) {
                    index = 9;
                } else {
                    index = 5;
                }
            } else if (mode.equals("1600x1800@60p")) {//上下
                if (splitScreenUpDownEnable) {
                    index = 11;
                } else {
                    index = 16;
                }
            } else if (mode.equals("1280x1440@60p")) {
                if (splitScreenUpDownEnable) {
                    index = 12;
                } else {
                    index = 4;
                }
            } else if (mode.equals("1920x2160@60p")) {
                if (splitScreenUpDownEnable) {
                    index = 13;
                } else {
                    index = 5;
                }
            } else {
                LOGD("set screen mode error , please check mode list");
                return;
            }

            cmd += index;
            LOGD("set screen mode " + cmd);
            execRootCmdSilent(cmd);
        } else {
            LOGD("mode is null , please check it");
        }
    }

    //显示-设置字体大小
    public void setFontSize(int index) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        int value = index;

        LOGD("set font size value = " + value);
        Intent i = new Intent("com.action.ztl.fontsize");
        i.putExtra("fontsize", value);
        mContext.sendBroadcast(i);
    }

    //网络-获取连接的网络类型辅助函数
    //需要权限：<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    static NetworkInfo getConnectedType(Context context) {
        if (context == null)
            return null;

        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo;
        }

        return null;
    }

    //网络-获取MAC地址 获取的是以太网口的。因为wifi不一定启用
    public String getMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //网络-获取以太网IP地址
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress().toString();
                        if (!ipAddress.contains("::"))
                            return inetAddress.getHostAddress().toString();
                    } else
                        continue;
                }
            }
        } catch (SocketException ex) {
            //if(CameraPublishActivity.DEBUG)  Log.e("getloaclIp exception", ex.toString());
        }
        return "";
    }

    //网络-获取当前连接的网络类型
    //-1 = 未知 0 = 以太网 1 = wifi 2 = 2g 3 = 3g 4 = 4g 5 = 5g
    public int getNetWorkType() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        NetworkInfo ni = getConnectedType(mContext);
        if (ni == null) {
            return -1;
        } else {
            if (ni.getType() == ConnectivityManager.TYPE_ETHERNET) {
                //Log.e(TAG, "上网方式是:以太网");
                return 0;

            } else if (ni.getType() == ConnectivityManager.TYPE_WIFI) {

                //Log.e(TAG, "上网方式是:wifi");
                return 1;
            } else if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {

                //Log.e(TAG, "上网方式是:移动网络");
                int nSubType = ni.getSubtype();
                TelephonyManager mTelephony = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);

                if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                        || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                        || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                        || nSubType == TelephonyManager.NETWORK_TYPE_GSM
                        && !mTelephony.isNetworkRoaming()) {
                    //netType = 3;// 2G
                    //Log.e(TAG, "上网方式是:2G");
                    return 2;
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                        || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        && !mTelephony.isNetworkRoaming()) {
                    //netType = 2;// 3G
                    //Log.e(TAG, "上网方式是:3G");
                    return 3;
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_LTE) {
                    //Log.e(TAG, "上网方式是:4G");
                    return 4;
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_NR) {
                    //5g
                    return 5;
                }
                //Log.e(TAG,"ip" + getPsdnIp("4g"));
            }
        }
        return -1;
    }

    //网络-指定wifi重连       ZtlHelper实现
    public void keepWifiConnect(String SSID, String password) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "keepWifiConnect"); //value填的需要和ztlhelper统一
        intent.putExtra("ssid", SSID);  //这里填要传入的参数，第一个name需要和ztlhelper统一
        intent.putExtra("psw", password);

        mContext.startService(intent);

    }

    //网络-停止连接指定wifi     ZtlHelper实现
    public void stopKeepWifiConnect() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "stopKeepWifiConnect"); //value填的需要和ztlhelper统一

        mContext.startService(intent);
    }


    // todo 对象序列化
    //网络-设置IP地址与动态静态
    public void setIpConfig(IpConfig ipConfig, int networkType) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "setIpConfig"); //value填的需要和ztlhelper统一
        intent.putExtra("ipConfig", ipConfig);
        intent.putExtra("networktype", networkType);

        mContext.startService(intent);
    }

    //网络-禁用或启动网络adb
    public void setNetAdb(boolean bEnable) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "set_net_adb");
        intent.putExtra("enable", bEnable);
        mContext.startService(intent);
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    //获取IPv4地址
    public static String getIPv4(String eth0, String wlan0, String ppp0) {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (eth0.equals(intf.getDisplayName()) == false || wlan0.equals(intf.getDisplayName()) == false || ppp0.equals(intf.getDisplayName()) == false) { //判断网口是否在使用，判断是否为我们获取的网口
                    continue;
                }

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取IP信息出错");
        }

        return null;
    }

    /*
     *	注意：获取SIM卡信息需要声明权限
     *	<uses-permission android:name="android.permission.READ_PHONE_STATE" />
     */
    //网络-获取SIM卡IMEI信息	1
    public String getSimImei() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return "";
        }
        String imei = null;
        TelephonyManager telManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }

        imei = telManager.getDeviceId();
        return imei;
    }

    //网络-获取SIM卡tel信息	0
    public String getSimTel() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return "";
        }
        String tel = null;
        TelephonyManager telManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        tel = telManager.getLine1Number();
        return tel;
    }

    //网络-获取SIM卡iccid信息		1
    public String getSimIccid() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return "";
        }
        String iccid = null;
        TelephonyManager telManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        iccid = telManager.getSimSerialNumber();
        return iccid;
    }

    //网络-获取SIM卡imsi信息	1
    public String getSimImsi() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return "";
        }
        String imsi = null;
        TelephonyManager telManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        imsi = telManager.getSubscriberId();
        return imsi;
    }

    //网络-获取运营商信息
    public String getSimOperator() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return "";
        }
        TelephonyManager telManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        String operatorNum = telManager.getSimOperator();
        String operator = "";
        if (operatorNum != null) {

            if (operatorNum.equals("46000") || operatorNum.equals("46002")
                    || operatorNum.equals("46007")) {

                // 中国移动
                operator = "CMCC";
            } else if (operatorNum.equals("46001")) {

                // 中国联通
                operator = "CUCC";
            } else if (operatorNum.equals("46003")) {

                // 中国电信
                operator = "CT";

            }

        }
        return operator;
    }

    //文件-写入文件方法
    final void writeMethod(String file, String conent) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(conent);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //文件-获取文件后缀名
    public String getFileType(String filePath) {
        String fileName;
        String prefix = "";
        if (isExist(filePath)) {
            File f = new File(filePath);
            fileName = f.getName();
            prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return prefix;
    }

    //文件-判断文件是否存在
    public boolean isExist(String path) {
        try {
            File file = new File(path);
            //判断文件夹是否存在,如果不存在则创建文件夹
            return file.exists();
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }

    //文件-加载文件
    String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];

        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        String readData = null;
        reader.close();
        return fileData.toString();
    }

    //GPIO计算方式
    public int gpioStringToInt(String port) {

        if (port.contains("GPIO") == false) {
            Log.e(TAG, "传入参数错误,请传入GPIO7_A5之类的，实际以规格书为准");
            return -1;
        }
        int A = port.charAt(4);
        int B = port.charAt(6);
        int C = port.charAt(7);
        int value = ((A - '0') & 0xff) * 32 + (B - 'A') * 8 + C - '0';
        return value;
    }

    //设置GPIO值
    public void setGpioValue(String port, int value) {
        Gpio gpio = new Gpio();
        gpioStringToInt(port);
        if (gpio.open(port) == false) {
            return;
        }
        gpio.setValue("out", value);
    }

    //获取GPIO值
    public int getGpioValue(String port) {
        Gpio gpio = new Gpio();
        gpioStringToInt(port);
        if (gpio.open(port) == false)
            return -1;
        gpio.setDirection("out");
        return gpio.getValue();
    }

    //20200916-废弃这个接口只能传入字符串类型的GPIO口了
//    @Deprecated
//    public void setGpioValue(int port, int value) {
//        Gpio gpio = new Gpio();
//        if (gpio.open(port) == false)
//            return;
//        gpio.setValue("out", value);
//    }

    //获取GPIO检测输入的值
    public int getGpioInValue(String port) {
        Gpio gpio = new Gpio();

        gpioStringToInt(port);

        if (gpio.open(port) == false)
            return -1;
        gpio.setDirection("in");
        return gpio.getValue();
    }

    //设置GPIO输入
    public void setGpioIn(String port) {
        Gpio gpio = new Gpio();
        gpioStringToInt(port);
        if (gpio.open(port) == false)
            return;
        gpio.setDirection("in");
    }

    //设置GPIO输出
    public void setGpioOut(String port) {
        Gpio gpio = new Gpio();
        gpioStringToInt(port);
        if (gpio.open(port) == false)
            return;
        gpio.setDirection("out");
    }

    //20200916-废弃这个接口，只能传入字符串类型的GPIO了。
//    @Deprecated
//    public int getGpioValue(int port, String direction) {
//        Gpio gpio = new Gpio();
//        if (gpio.open(port) == false)
//            return -1;
//        gpio.setDirection(direction);
//        return gpio.getValue(direction);
//    }

    //媒体-获取最大音量	1
    public int getSystemMaxVolume() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return maxVolume;
    }

    //媒体-获取当前音量	1
    public int getSystemCurrenVolume() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume;
    }

    //媒体-增大音量，音量+1	//设置媒体音量
    public int setRaiseSystemVolume() {
        int curVolume = getSystemCurrenVolume();
        curVolume++;
        if (curVolume > getSystemMaxVolume()) {
            curVolume = getSystemMaxVolume();
        }
        return setSystemVolumeIndex(curVolume);

    }

    //媒体-减小音量，音量-1	//设置媒体音量
    public int setLowerSystemVolume() {
        int curVolume = getSystemCurrenVolume();
        curVolume--;
        if (curVolume < 0) {
            curVolume = 0;
        }
        return setSystemVolumeIndex(curVolume);

    }

    //媒体-设置音量值		1
    public int setSystemVolumeIndex(int index) {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return -1;
        }
        try {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (index >= 0 && index <= maxVolume)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, index, AudioManager.FLAG_PLAY_SOUND);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    //媒体-设置相机方向(摄像头)
    public void setCameraOrientation(int orientation) {
        int value = orientation;

        if (value < 0 || value > 3) {
            Log.e(TAG, "set camera orientation value(" + value + ") err!,set close");
            value = 4;
        }
        String str = value + "";

        Log.d(TAG, "set camera orientation value = " + value);
        try {
            setSystemProperty(CAMERA_ORIENTATION_PROP, str);
        } catch (Exception exc) {
            Log.w(TAG, "Unable to set camera orientation");
        }
    }

    //媒体-获取相机方向
    public int getCameraOrientation() {
        String state = getSystemProperty("persist.sys.cameraOrientation", "0");
        return Integer.parseInt(state);
    }

    //媒体-获取相机是否镜像
    public boolean getCameraIsMirror() {
        String state = getSystemProperty("persist.ztl.ismirror", "0");
        if (state.contains("0")) {
            return false;
        } else
            return true;
    }

    //媒体-设置相机镜像
    public void setCameraIsMirror(boolean Misrror) {
        if (Misrror) {
            setSystemProperty("persist.ztl.ismirror", "1");
        } else
            setSystemProperty("persist.ztl.ismirror", "0");
    }

    //通过反射机制调用SystemProperties.get
    public String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);//方法名，参数类型
            String value = (String) getter.invoke(clazz.newInstance(), property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to read system properties");
        }
        return defaultValue;
    }

    //通过反射机制设置property
    public void setSystemProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongConstant")
    PendingIntent getPendingIntent(String paramString, int paramInt) {

        Intent localIntent = new Intent();
        localIntent.setAction(paramString);
        return PendingIntent.getBroadcast(mContext, paramInt, localIntent, 268435456);
    }

    //设置GPU性能模式
    public void setGPUMode(String mode) {

    }

    //打开CPU监控
    public void openMonitor() {
        if (mContext == null) {
            Log.e("上下文为空", "不执行");
            return;
        }
        ComponentName componetName = new ComponentName(
                "com.ztl.helper",  //这个参数是另外一个app的包名
                "com.ztl.helper.ZTLHelperService");   //这个是要启动的Service的全路径名

        Intent intent = new Intent();
        intent.setComponent(componetName);
        intent.putExtra("cmd", "open_monitor");//value填的需要和ztlhelper统一

        mContext.startService(intent);
    }

    //获取CPU可用频率
    public String[] getCPUFreq() {
        if (cpuInfo == null) {
            cpuInfo = new CpuInfo();
            cpuInfo.Init(null);
        }

        return CpuInfo.SubCore.getFreq();
    }

    //设置CPU频率
    public void setCPUFreq(String cpu_freq) {

        if (cpuInfo == null) {
            cpuInfo = new CpuInfo();
            cpuInfo.Init(null);
        }
        cpuInfo.setCPUFreq(cpu_freq);
    }
}
