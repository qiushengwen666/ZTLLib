package ZtlApi;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import ZtlApi.ZtlManager;

//20200916 去掉了数字openPort。新的app必须使用字符类型的gpio 实现51 71兼容

class Gpio {
    private static final String TAG = "GPIO";
    private int mPort;
    private boolean isGpioPortPrepared = false;
    
    private File mGpioExport = null;
    private File mGpioUnExport = null;
    private File mGpioPort = null;
    private File mGpioPortDirection = null;
    private File mGpioPortValue = null;

    private String gpio_export = "/sys/class/gpio/export";
    private String gpio_unexport = "/sys/class/gpio/unexport";
    private String gpio_port = "/sys/class/gpio/gpio";

    String gpio_name;

    public Gpio() {
    }

    //直接输入GPIO7_A5 之类 省得每次都按计算器
    public boolean open(String strPort) {
        if (strPort == null || strPort.isEmpty() )
            return false;

        gpio_name = strPort;

        //71 51兼容
        int nValue = ZtlManager.GetInstance().gpioStringToInt(strPort);

        this.mPort = nValue;
        this.mGpioExport = new File(this.gpio_export);
        this.mGpioUnExport = new File(this.gpio_unexport);
        this.isGpioPortPrepared = prepare_gpio_port(this.mPort);
        return isGpioPortPrepared;
    }

    //获取当前GPIO的Direction
    public String getDirection() {
        if (isGpioPortPrepared == false){
            return "unknown";
        }
        String strDir = null;

        if (this.mGpioPortDirection == null){
            return "unknown";
        }
        if (this.mGpioPortDirection.exists()) {
            strDir = readGpioNode(this.mGpioPortDirection);
        } else {
            return "unknown";
        }

        return strDir;
    }

    String lastError = "";

    //设置GPIO的Direction
    public void setDirection(String dir) {
        if (isGpioPortPrepared == false){
            return;
        }
        //如果系统占用该IO口，不执行setDirection
        if (this.mGpioPortDirection == null)
            return;

        if (getDirection().equals(dir) == false){
            writeGpioNode(this.mGpioPortDirection, dir);
        }
    }

    //获取当前direction的value
    public int getValue() {
        if (isGpioPortPrepared == false){
            return -1;
        }
        int value = -1;
        if (this.mGpioPortDirection != null){
            String string = readGpioNode(this.mGpioPortValue);
            if (string == null) {
                return -1;
            }
            if (string.equals("0"))
                value = 0;
            else if (string.equals("1")) {
                value = 1;
            } else {
                try {
                    value = Integer.valueOf(string);
                } catch (NumberFormatException m) {
                    return -1;
                }
            }
        }
        return value;
    }

    //获取指定direction的value
    public int getValue(String direction)//in out
    {
        if (isGpioPortPrepared == false){
            return -1;
        }
        String strCurDirection = null;
        //该IO口不存在（被系统占用）返回-1
        if(this.mGpioPort.exists() == false){
            return -1;
        }
        //当前direction目录不存在
        if (this.mGpioPortDirection.exists() == false) {
            return -1;
        }
        //获取到当前direction是in 还是out
        strCurDirection = readGpioNode(this.mGpioPortDirection);
        if (strCurDirection.equals(direction)) {
            return getValue();
        }

        return -1;
    }

    //设置当前direction的value
    public void setValue(int value) {
        if (isGpioPortPrepared == false){
            return;
        }
        if (getValue() != value){
            writeGpioNode(this.mGpioPortValue, Integer.toString(value));
        }
    }

    //设置指定direction的value //direction = "in" or "out"
    public void setValue(String direction, int value) {
        if (isGpioPortPrepared == false){
            return;
        }
        if (getDirection().equals(direction) == false){
            writeGpioNode(this.mGpioPortDirection, direction);
        }
        writeGpioNode(this.mGpioPortValue, Integer.toString(value));
    }

    private void writeGpioNode(File file, String value) {
        if (file.exists() == false)
            return;

        if (file.exists()) {

//      System.out.println("write " + flag + " to " + file);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(value.getBytes(), 0, value.getBytes().length);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                String error = e.toString();
                if (error.contains("Permission denied")) {
                    ZtlManager.GetInstance().execRootCmdSilent("chmod 777 " + file.getAbsolutePath());
                    Log.e(TAG, "正在申请权限");
                    writeGpioNode(file, value);
                } else {
                    Log.e(TAG, "writeGpioNode " + gpio_name + " 错误");
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean prepare_gpio_port(int port) {
        if (this.mGpioExport.exists()) {

            String path = this.gpio_port + port;
            boolean bbb = ZtlManager.GetInstance().isExist(path);
            if (bbb == false)
                writeGpioNode(this.mGpioExport, Integer.toString(port));

            String path_direction = path + "/direction";
            String path_value = path + "/value";

            this.mGpioPort = new File(path);
            if (this.mGpioPort.exists() == false) {
                if( gpio_name != null){
                    Log.e(TAG, "系统没有导出。"+ gpio_name+ "请看文档或查询定昌技术支持");
                }

                lastError = "系统没有导出这个io口。请看文档或查询定昌技术支持" + mPort;
                return false;
            }

            this.mGpioPortDirection = new File(path_direction);
            this.mGpioPortValue = new File(path_value);
        }
        return (this.mGpioPort.exists()) && (this.mGpioPortDirection.exists()) && (this.mGpioPortValue.exists());
    }

    public String getLastError() {
        String ls = lastError;
        lastError = "";
        return ls;
    }

    private boolean gpio_request() {
        return this.isGpioPortPrepared;
    }

    void gpio_free() {
        writeGpioNode(this.mGpioUnExport, Integer.toString(this.mPort));
    }

    private String readGpioNode(File file) {
        BufferedReader reader = null;
        String string = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            string = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }
}