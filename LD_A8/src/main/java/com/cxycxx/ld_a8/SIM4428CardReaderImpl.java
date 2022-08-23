package com.cxycxx.ld_a8;

import com.landicorp.android.eptapi.card.Sim4428Driver;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.utils.BytesBuffer;
import com.landicorp.android.eptapi.utils.IntegerBuffer;

/**
 * 本模块支持 SIM4428 卡的数据读写等操作
 */

public class SIM4428CardReaderImpl {
    //  自定义错误码:接口调用失败或成功
    private static final int FAIL = 0xff;
    private static final int SUCCESS = 0x00;
    private Sim4428Driver sim4428Driver;

    public SIM4428CardReaderImpl(String deviceName) {
//        sim4428Driver = new Sim4428Driver(deviceName);
        sim4428Driver = new Sim4428Driver();
    }

    public int powerUp(int voltage) {
        try {
            BytesBuffer atr = new BytesBuffer();
            return sim4428Driver.powerUp(voltage, atr);

        } catch (RequestException e) {
            return FAIL;
        }
    }
    public boolean exists() {
        try {
            return sim4428Driver.exists();
        } catch (RequestException e) {
            return false;
        }
    }
    public boolean verify(byte[] password, IntegerBuffer errorCount) {
        int ret = FAIL;
        try {
            //  校验  4428  卡密码
            ret = sim4428Driver.verify(password, errorCount);
            if (ret != SUCCESS) {
                return false;
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean changeKey(byte[] password) {
        int ret = FAIL;
        try {
            //  修改存储卡的操作密码，须在校验密码正确后才可进行
            ret = sim4428Driver.changeKey(password);
            if (ret != SUCCESS) {
                return false;
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void read(int address, int len, BytesBuffer data) {
        int ret = FAIL;
        try {
            ret = sim4428Driver.read(address, len, data);
            if (ret != SUCCESS) {
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
    public byte[] read(byte[] pwd, int address, int len) {
        byte[] result = null;
        int ret = FAIL;
        try {
            if (pwd != null && pwd.length > 0) {
                IntegerBuffer pwdTimes = new IntegerBuffer();
                ret = sim4428Driver.verify(pwd, pwdTimes);
                if (ret != SUCCESS) {
                    return null;
                }
            }
            BytesBuffer data = new BytesBuffer();
            ret = sim4428Driver.read(address, len, data);
            if (ret != SUCCESS) {
                return null;
            }
            return data.getData();
        } catch (RequestException e) {
            e.printStackTrace();
        }
        return result;
    }
    public void write(int mode, int address, byte[] data) {
        int ret = FAIL;
        try {
            ret = sim4428Driver.write(mode, address, data);
            if (ret != SUCCESS) {
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
