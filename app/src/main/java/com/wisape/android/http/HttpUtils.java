package com.wisape.android.http;


import java.util.Iterator;
import java.util.Map;

/**
 * 拼接http请求参数
 * Created by hm on 2015/6/16.
 */
public class HttpUtils {

    /** url and para separator **/
    public static final String URL_AND_PARA_SEPARATOR = "?";
    /** parameters separator **/
    public static final String PARAMETERS_SEPARATOR = "&";
    /** equal sign **/
    public static final String EQUAL_SIGN = "=";



    public static String getUrlWithParas(String url,
                                         Map<String, String> parasMap) {
        StringBuilder urlWithParas = new StringBuilder(isEmpty(url) ? "" : url);
        String paras = joinParas(parasMap);
        if (!isEmpty(paras)) {
            urlWithParas.append(URL_AND_PARA_SEPARATOR).append(paras);
        }
        return urlWithParas.toString();
    }

    private static String joinParas(Map<String, String> parasMap) {
        if (parasMap == null || parasMap.size() == 0) {
            return null;
        }

        StringBuilder paras = new StringBuilder();
        Iterator<Map.Entry<String, String>> ite = parasMap.entrySet().iterator();

        while (ite.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) ite.next();
            paras.append(entry.getKey()).append(EQUAL_SIGN).append(entry.getValue());
            if (ite.hasNext()) {
                paras.append(PARAMETERS_SEPARATOR);
            }
        }
        return paras.toString();
    }

    private static boolean isEmpty(String str) {
        return (str == null || str.trim().length() == 0 || "".equals(str));
    }



}
