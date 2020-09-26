package ZtlApi;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ZtlManagerA33_A64 extends ZtlManager{
	
	private boolean DEBUG_ZTL = false;
	private String TAG = "ZtlManagerA33_A64";

	public ZtlManagerA33_A64(){
		init_gpiomap();
		DEBUG_ZTL = SystemProperties.get("persist.sys.ztl.debug","false").equals("true");
	}
	
	void LOGD(String msg){
		if(DEBUG_ZTL){
			Log.d(TAG, msg);
		}		
	}
	
	//获取U盘路径	1
	@Override
	public String getUsbStoragePath(){
		String usbPath = null;
	
		try{
			usbPath = getSystemProperty("persist.sys.usbDisk","unKnown");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return usbPath;
	}
	
	//A64获取芯片唯一ID
	@Override
	public String getDeviceID(){
		BufferedReader bre = null;
		String lineInfo;
        String cpuSerial;
		
		File cpuInfo = new File("/sys/class/android_usb/android0/iSerial");
		if(!cpuInfo.exists()){
			return null;
		}
		
		 try {
			bre = new BufferedReader(new FileReader(cpuInfo));
			cpuSerial = bre.readLine(); // 一次读入一行数据  
			return cpuSerial;
			/*while((lineInfo = bre.readLine())!= null){
				if(!lineInfo.contains("androidboot.serialno")){
					continue;
				}
				LOGD(lineInfo.length() + lineInfo);
				
				cpuSerial = lineInfo.substring(lineInfo.indexOf(":")+2);
				LOGD(cpuSerial);
				return cpuSerial;
			}*/
		  }catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		return null;
	}

	//增大音量，音量+1	1
	@Override
	public int setRaiseSystemVolume(){
		try {
		 AudioManager am=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		 am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
		 } catch (Exception e) {
		   	e.printStackTrace();
		      return -1;
		 }
		  
		  return 0;
	}
	
	//减小音量，音量-1	1
	@Override
	public int setLowerSystemVolume(){
		try {
			AudioManager am=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
		} catch (Exception e) {
		   	e.printStackTrace();
		      return -1;
		 }
		  
		  return 0;
	}

    //设置开机自启动APP包名和Activity		1
    @Override
    public int setBootPackageActivity(String pkgName,String pkgActivity){
    	if(pkgName != null && pkgActivity != null){
    		//setSystemProperty("persist.sys.bootPkgName",pkgName);
    		//setSystemProperty("persist.sys.bootPkgActivity",pkgActivity);
			execRootCmdSilent("setprop persist.sys.bootPkgName "+pkgName);
			execRootCmdSilent("setprop persist.sys.bootPkgActivity "+pkgActivity);
    	}else{
    		Log.e(TAG,"pkgName ("+pkgName+") or pkgActivity ("+pkgActivity+") err");
    		return -1;
    	}
    	return 0;
    }

     //设置APP加密密钥
    public int setAppKey(String key){
    	if(key != null){
    		Intent systemBarIntent = new Intent("com.ztl.key");
			String str = key;
			systemBarIntent.putExtra("enable", str); 
			mContext.sendBroadcast(systemBarIntent);   
    	}else{
    		Log.e(TAG,"设置APP加密密钥值错误");
    		return -1;
    	}
    	return 0;
    }

	//获取屏幕方向	1
	@Override
	public int getDisplayOrientation(){
		return 0;
	}

	//设置屏幕方向,设置完后重启系统
	@Override
	public void setDisplayOrientation(int rotation){
		Intent systemBarIntent = new Intent("com.ztl.rotation");
		int nowrotation = rotation;
		systemBarIntent.putExtra("enable", nowrotation);
		mContext.sendBroadcast(systemBarIntent);
	}

	//打开导航兰	1
	@Override
	public void setOpenSystemBar(){
		Intent systemBarIntent = new Intent("com.ztl.systembar");
		String str = "1";
		systemBarIntent.putExtra("enable", str);
		mContext.sendBroadcast(systemBarIntent);	
	}
	
	//隐藏导航兰	1
	@Override
	public void setCloseSystemBar(){
		Intent systemBarIntent = new Intent("com.ztl.systembar");
		String str = "0";
		systemBarIntent.putExtra("enable", str);
		mContext.sendBroadcast(systemBarIntent);
	}
	
	//设置分辨率		1
	@Override
	public void setScreenMode(String mode){
		Intent systemBarIntent = new Intent("com.ztl.vga");
		String str = mode;
		systemBarIntent.putExtra("enable", str);
		mContext.sendBroadcast(systemBarIntent);
	}
	
	//获取USB调试状态	1
	@Override
	public int getUsbDebugState(){
		return 0;
	}
	
	//获取状态栏状态	1
	@Override
	public int getSystemBarState(){
		return 0;
	}

	Map<String, Integer> gpios = new HashMap<>();

	void init_gpiomap(){
		gpios.put("PE1",129);
		gpios.put("PE2",130);
		gpios.put("PE3",131);
		gpios.put("PE4",132);
		gpios.put("PE7",135);
	}

	@Override
	public int gpioStringToInt(String strGpioName) {
        if (strGpioName.contains("PE") == false){
            Log.e(TAG,"传入参数错误,请传入GPIO7_A5之类的，实际以规格书为准");
            return -1;
        }

		Object v = gpios.get( strGpioName );
		if (v == null){
			Log.e("gpio","name"+strGpioName+"缺乏映射，请联系管理员添加");
		}
		return gpios.get( strGpioName );
	}

}
