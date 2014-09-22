package com.obcerver.iamhere.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

/**
 * Cary Util Library
 * @author Cary Zeyue Chen
 */
public class CUtil {

    /**
     * Parse dp(float) to px(int)
     * @author Cary Zeyue Chen
     */
    public final static float parseDpToPx(Resources r, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    /**
     * Parse dp(int) to px(float)
     * @author Cary Zeyue Chen
     */
    public final static float parseDpToPx(Resources r, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    /**
     * Parse dp(int) to px(int)
     * @author Cary Zeyue Chen
     */
    public final static int parseDpToPxInt(Resources r, int dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }


    /**
     * Simple Toast with short time
     */
    public final static void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * Simple Toast with long time
     */
    public final static void toastLong(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Simple Toast with content from resource 
     */
    public final static void toast(Context context, int resId) {
        toast(context, context.getString(resId));
    }
}
