package com.cxycxx.p990;

import com.landicorp.android.eptapi.card.Sim4442Driver;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.utils.BytesBuffer;
import com.landicorp.android.eptapi.utils.IntegerBuffer;

/**
 * 本模块支持 SIM4442 卡的数据读写等操作
 */

public class SIM4442CardReaderImpl {
    // 自定义错误码:接口调用失败或成功
    public static final int FAIL = 0xff;
    public static final int SUCCESS = 0x00;
    private Sim4442Driver sim4442Driver;

    public SIM4442CardReaderImpl(String deviceName) {
        //sim4442Driver = new Sim4442Driver(deviceName);
        sim4442Driver = new Sim4442Driver();
    }

    public int powerUp(int voltage) {
        int ret = FAIL;
        try {
            BytesBuffer atr = new BytesBuffer();
            return sim4442Driver.powerUp(voltage, atr);
        } catch (RequestException e) {
            e.printStackTrace();
            return FAIL;
        }
    }

    public boolean exists() {
        try {
            return sim4442Driver.exists();
        } catch (RequestException e) {
            return false;
        }
    }

    public boolean verify(byte[] password, IntegerBuffer errorCount) {
        int ret = FAIL;
        try {
            // 校验 4442 卡密码
            ret = sim4442Driver.verify(password, errorCount);
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
            // 修改存储卡的操作密码，须在校验密码正确后才可进行
            ret = sim4442Driver.changeKey(password);
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
            ret = sim4442Driver.read(address, len, data);
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
                ret = sim4442Driver.verify(pwd, pwdTimes);
                if (ret != SUCCESS) {
                    return null;
                }
            }
            BytesBuffer data = new BytesBuffer();
            ret = sim4442Driver.read(address, len, data);
            if (ret != SUCCESS) {
                return null;
            }
            return data.getData();
        } catch (RequestException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void write(int address, byte[] data) {
        int ret = FAIL;
        try {
            ret = sim4442Driver.write(address, data);
            if (ret != SUCCESS) {
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    public int readErrorCount(IntegerBuffer errorCount) {
        int ret = FAIL;
        try {
            ret = sim4442Driver.readErrorCount(errorCount);
            if (ret != SUCCESS) {
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
        return ret;
    }
}