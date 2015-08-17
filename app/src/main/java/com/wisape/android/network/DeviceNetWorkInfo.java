/*
 * Copyright (c) 2012, Incito Corporation, All Rights Reserved
 */
package com.wisape.android.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * get device information tools
 * 获取设备各种信息的工具类
 * @author William
 * @version 1.0
 */
public class DeviceNetWorkInfo {
	
	private static final String TAG = DeviceNetWorkInfo.class.getCanonicalName();
	
	/**
	 * get Local IP Address only return by IPv4
	 * @return
	 */
	public static String getLocalIpAddressV4() {
		try {
			String ipv4;
			ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface ni : nilist) {
				ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress() && 
							InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())
							) 
					{
						return ipv4;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}
	
	/**
	 *  获取本地Mac地址
	 * @param context
	 * @return
	 */
	public static String getLocalMacAddressBySystemService(Context context) {
		String macAddress = null;
		try {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
			WifiInfo info = wifi.getConnectionInfo();
			macAddress = info.getMacAddress();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return macAddress;
    }
	
	public static String getLocalMacAddressBySystemService2FullString(Context contex){
		String macAddress = "";
		try {
			String temp = getLocalMacAddressBySystemService(contex);
			String[] o = temp.split(":");
			for (String string : o) {
				macAddress = macAddress + string;
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return macAddress;
	}

	/**
	 * Get Local MacAddress from IP Address whatever in Wireless environment.
	 * 根据IP获取本地MacAddress， 必须使用网络连接
	 * @description 
	 * @author   tianran
	 * @createDate Feb 11, 2015
	 * @param context
	 * @return
	 */
	public static String getLocalMacAddressFromIp(Context context) {
		String mac_s = "";
		try {
			byte[] mac;
			String ipAddress = getLocalIpAddressV4();
			if (null != ipAddress) {
				NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress
						.getByName(ipAddress));
				mac = ne.getHardwareAddress();
				mac_s = byte2hex(mac);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return mac_s;
	}
	/**
	 * byte to hex
	 * @description 
	 * @author   tianran
	 * @createDate Feb 11, 2015
	 * @param b
	 * @return
	 */
	private static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1)
				hs = hs.append("0").append(stmp);
			else {
				hs = hs.append(stmp);
			}
		}
		return String.valueOf(hs);
	}
}
