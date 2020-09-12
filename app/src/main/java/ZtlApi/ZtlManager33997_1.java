package ZtlApi;

import android.content.Intent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import android.os.SystemProperties;

public class ZtlManager33997_1 extends ZtlManager{

	ZtlManager33997_1(){
		DEBUG_ZTL = SystemProperties.get("persist.sys.ztl.debug","false").equals("true");
	}

	//获取U盘路径	1
	@Override
	public String getUsbStoragePath(){
		String usbPath = null;
		String usbBasePath = "/storage/";
		String ApkPath = null;
		int m = 0;
	
		File file = new File(usbBasePath);
		try{
			if(file.exists() && file.isDirectory()){ //open usb_storage
				File[] files = file.listFiles();
		//		if( files.length > 0){
				for(int i= 0; i < files.length ; i++){
					usbPath = files[i].getAbsolutePath();
					LOGD("shx : get file path " + usbPath);
					if(usbPath.contains("udisk")){ //open USB_DISK
						LOGD("shx : open " + usbPath);
						File usbFile = new File(usbPath); //shx 3399 7.1OS maybe /storage/udisk0
						if(usbFile.exists() && usbFile.isDirectory()){
							usbPath = usbFile.getAbsolutePath();	//udisk0
							LOGD("shx : usbPath " + usbPath);
							break;
						}
					}//end open USB_DISK
				}
			
			}//end open usb_storage
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return usbPath;
	}

	//获取屏幕方向	1
	@Override
	public int getDisplayOrientation(){
		String state = getSystemProperty("persist.sys.ztlOrientation","0");
		return Integer.valueOf(state).intValue();
	}


	//获取USB调试状态	1
	@Override
	public int getUsbDebugState(){
		String state = getSystemProperty("persist.usb.mode","1");	//1 : disconnect to pc  0: connect to pc
		if(state.equals("0") || state.equals("2")){
			state = "1";
		}else{
			state = "0";
		}
		return Integer.valueOf(state).intValue();
	}

	//获取OTG口连接状态 勾的时候是2 不勾的时候是1
	@Override
	public boolean getUSBtoPC(){
		try {
			String state = loadFileAsString("/sys/kernel/debug/usb@fe800000/rk_usb_force_mode");
			if (state.equals("2")){
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	//设置OTG口连接状态
	@Override
	public void setUSBtoPC(boolean toPC){
		if (toPC){
			setSystemProperty("persist.usb.mode","2");
			writeMethod("/sys/kernel/debug/usb@fe800000/rk_usb_force_mode","2");
		}else
			setSystemProperty("persist.usb.mode","1");
			writeMethod("/sys/kernel/debug/usb@fe800000/rk_usb_force_mode","1");
	}
	
	//获取状态栏状态	1
	@Override
	public int getSystemBarState(){
		String state = getSystemProperty("persist.sys.barState","1");
		return Integer.valueOf(state).intValue();
	}

	//使能左右分屏功能
	@Override
	public void setSplitScreenLeftRightEnable(boolean isEnable){
		Log.e("ztllib", "unsupport fucntion now for this board.todo later.");
/*		if(isEnable){
			setSystemProperty("persist.sys.leftRightEnable","true");
		}else{
			setSystemProperty("persist.sys.leftRightEnable","false");
		}
*/
	}	
	
	//使能上下分屏功能
	@Override
	public void setSplitScreenUpDownEnable(boolean isEnable){
		Log.e("ztllib", "unsupport fucntion now for this board.todo later.");
/*		if(isEnable){
			setSystemProperty("persist.sys.upDownEnable","true");
		}else{
			setSystemProperty("persist.sys.upDownEnable","false");
		}
*/
	}

	//获取支持的分辨率列表
	@Override
	public String[] getScreenModes(){
		String displayModes;
		displayModes = getSystemProperty("persist.sys.displaymdoes","");
		String modes[] = displayModes.split(",");
		
		return modes;
	}

	//设置分辨率
	@Override
	public void setScreenMode(String mode){
    	Intent setModeIntent = new Intent("android.ztl.action.SET_SCREEN_MODE");
		setModeIntent.putExtra("mode", mode);
		mContext.sendBroadcast(setModeIntent);        
	}

	//设置GPU性能模式
	@Override
	public void setGPUMode(String mode){
		String fmt = String.format("echo "+mode+" >/sys/bus/platform/devices/ff9a0000.gpu/devfreq/ff9a0000.gpu/governor");
		execRootCmdSilent(fmt);
	}



}
