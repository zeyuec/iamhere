package com.obcerver.iamhere.lib;

/**
 * Cary Log Library
 * @author Cary Zeyue Chen
 * @since 09/13/2014
 */
public class CLog
{
    private static String DEFAULT_TAG = "iamhere";

    /**
     * Default v function
     */
	public static final void v(String tag, String content) {
		if (Constants.App.DEBUG) {
			android.util.Log.v(tag, content);
		} 
	}

    /**
     * Use thread stack trace to get the class and method name
     */
    public static final String getTag() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int currentState = 4;
        String fullClassName = trace[currentState].getClassName();
        String fullMethodName = trace[currentState].getMethodName();
        
        String tag = "[" +
            fullClassName.substring(fullClassName.lastIndexOf('.')+1) +
            "." +
            fullMethodName +
            "]";
        return tag;
    }

    // Simple V function for some situations

    public static final void v(String content) {
		if (Constants.App.DEBUG) {
			android.util.Log.v(getTag(), content);
		} 
    }

    public static final void v(int content) {
		if (Constants.App.DEBUG) {
            android.util.Log.v(getTag(), String.valueOf(content));
        } 
    }
    
	public static final void d(String tag, String content) {
		if (Constants.App.DEBUG) {
			android.util.Log.v(tag, content);
		} 
	}
    
    public static final void d(String content) {
		if (Constants.App.DEBUG) {
			android.util.Log.v(getTag(), content);
		} 
    }

    public static final void d(int content) {
		if (Constants.App.DEBUG) {
            android.util.Log.v(getTag(), String.valueOf(content));
        } 
    }    
}
