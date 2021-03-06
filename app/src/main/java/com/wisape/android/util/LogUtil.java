package com.wisape.android.util;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.format.PatternFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志
 * @author hm
 *
 */
public final class LogUtil {
	
    private static  Logger logger;
    static {
        logger = LoggerFactory.getLogger();
    }

    private static StackTraceElement getCaller(){
        return Thread.currentThread().getStackTrace()[4];
    }

    /**
     *创建标签类名，方法名，行数
     */
    private static String genaratorTag(StackTraceElement caller){
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = getCurrentTime() + "-->" + String.format(tag,callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return tag + ":";
    }
	    
    /** 打印日志
     * @param info  日志消息
     */
    public static void d(String info){
        setFileAppender();
        logger.debug(genaratorTag(getCaller()) + info);
    }

    /**
     * 打印异常日志
     * @param cause
     */
    public static void e(String info,Throwable cause){
        setFileAppender();
        logger.error(genaratorTag(getCaller()) + info + ":" + getErrorInfo(cause));
    }

    private static void setFileAppender(){
        //选择记录类型
        final FileAppender mfa = (FileAppender) logger.getAppender(1);
        mfa.setAppend(true);//是否追加
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
        String date = sdf.format(new Date());
        String fileName = String.format("Wisape-%s.log", date);
        mfa.setFileName(fileName);//生成的日志名称
        PatternFormatter formatter = new PatternFormatter();
        formatter.setPattern("%r %c{1} [%P] %m %T");//%d{3}
        mfa.setFormatter(formatter);
    }
	    
    private static String getErrorInfo(Throwable arg1) {
        if(arg1 != null){
            Writer writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            arg1.printStackTrace(pw);
            pw.close();
            String error = writer.toString();
            return error;
        }
        return "没有传递过来异常原因";
    }

	/**
	 * 获取系统当前时间
	 * @return
	 */
	public static String getCurrentTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		return df.format(new Date());
	}
}
