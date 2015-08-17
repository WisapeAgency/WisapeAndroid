package com.wisape.android.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
/**
 *
 * @description 
 * @author   William
 * @createDate Feb 11, 2015
 * @version  1.0
 */
public class NetworkState {
	
    public static final int NETWORK_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
    public static final int NETWORK_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
    
    /**
     * Decide is NetWork available
     * 是否连接网络
     * @param ctx
     * @return
     */
    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo net = connectivityManager.getActiveNetworkInfo();
        if (net != null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 通过 host address 判断网络服务
     * @param ctx
     * @param hostAddress
     * @return
     */
    public static boolean isServiceReachable(Context ctx, int hostAddress) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.requestRouteToHost(connectivityManager
                .getActiveNetworkInfo().getType(), hostAddress);
    }
    
    /**
     * get Network type.
     * @param ctx
     * @return
     */
    public static int getNetworkType(Context ctx){
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return NETWORK_TYPE_MOBILE;
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isAvailable()) {
            if(netinfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_TYPE_WIFI;
            } else {
                return NETWORK_TYPE_MOBILE;
            }
        }
        return NETWORK_TYPE_MOBILE;
    }
    
    /**
     * 是否为Wap 网络
     * @return
     */
    public static boolean isWapNetwork() {
        return !TextUtils.isEmpty(getProxyHost());
    }
    
    /**
     * 获取代理Host
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String getProxyHost() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return System.getProperty("http.proxyHost");
        } else {
            return android.net.Proxy.getDefaultHost();
        }
    }
    
    /**
     * 获取代理端口
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getProxyPort() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Integer.valueOf(System.getProperty("http.proxyPort"));
        } else {
            return Integer.valueOf(android.net.Proxy.getDefaultHost());
        }
    }

	private NetworkState() {
	}
}
