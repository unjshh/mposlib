package com.icbc.smartpos.deviceservice.aidl;

import com.icbc.smartpos.deviceservice.aidl.TusnData;

/**
 * 终端设备信息对象
 * @author: baoxl
 */
interface IDeviceInfo {
	/**
	 * 获取终端序列号
	 * @return 终端设备序列号
	 */
	String getSerialNo();
	
	/**
	 * 获取终端IMSI号
	 * @return 终端IMSI号
	 */
	String getIMSI();
	
	/**
	 * 获取终端IMEI号
	 * @return 终端IMEI号
	 */
	String getIMEI();
	
	/**
	 * 获取SIM卡ICCID
	 * @return 终端SIM卡ICCID
	 */
	String getICCID();
	
	/**
	 * 获取厂商名称
	 * @return 终端厂商名称
	 */
	String getManufacture();
	
	/**
	 * 获取终端设备型号
	 * @return 终端设备型号
	 */
	String getModel();
	
	/**
	 * 获取终端Android操作系统版本
	 * @return 终端Android系统版本
	 */
	String getAndroidOSVersion();
	
	/**
	 * 获取终端Android内核版本
	 * @return 终端Android内核版本
	 */
	String getAndroidKernelVersion();
	
	/**
	 * 获取终端ROM版本
	 * @return 终端系统ROM版本
	 */
	String getROMVersion();
	
	/**
	 * 获取终端固件版本
	 * @return 终端固件版本
	 */
	String getFirmwareVersion();
	
	/**
	 * 获取终端硬件版本
	 * @return 终端硬件版本 -后标贴的硬件版本号，如H:L4200151361，测试机器可能没有录入
	 */
	String getHardwareVersion();
	
	/**
	 * 更新终端系统时间
	 * @param date - 日期，格式yyyyMMdd
	 * @param time - 时间，格式HHmmss
	 * @return 更新成功返回true，失败返回false
	 **/
	boolean updateSystemTime(String date, String time);
	
	/**
	 * 设置系统功能（设置全局有效）
	 * @param bundle - 设置参数
     * <ul>
     * <li>HOMEKEY(boolean) – 是否允许使用HOME键，true可用，false禁用（注：在机器重启后会取消屏蔽）</li>
     * <li>STATUSBARKEY(boolean) – 是否允许使用下拉菜单，true可用，false禁用</li>
     * </ul>
	 * @return 设置成功返回true，失败返回false
	 **/
	boolean setSystemFunction(in Bundle bundle);
	
    /**
	 * 获取银联终端唯一终端号TUSN
	 * @param mode 模式, 预留参数， 需为0
	 * @param input 指 对TUSN计算 MAC 时，参与计算的随机数(随机因子)，允许范围:4~10字节
	 * @return 成功返回TUSN数据，失败返回null。
	 **/
	TusnData getTUSN(int mode, in byte[] input);
}
