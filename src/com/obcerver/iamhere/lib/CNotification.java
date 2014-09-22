package com.obcerver.iamhere.lib;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.content.ComponentName;
import android.os.Vibrator;

import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.R;

/**
 * Cary Notification Library
 * @author Cary Zeyue Chen
 * @since 2013-06-07
 */
public class CNotification {

    private Context              context;
    private Intent               ntfIntent;
    private String               ntfTicker, ntfContent;
    private int                  ntfDefault, ntfId;
    private final static int     DEFAULT_ID = 99999999;
    
    public CNotification(Context context, String ticker, String content, Intent intent) {
        this.context = context;
        this.ntfTicker = ticker;
        this.ntfContent = content;
        this.ntfIntent = intent;
        this.ntfDefault = getDefault();
        this.ntfId = DEFAULT_ID;
    }

    /**
     * Show
     */
    public void show() {
        
    	PendingIntent pi = PendingIntent.getActivity(context, 0, ntfIntent, 0);
    	NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    	Notification ntf = new Notification();
        
    	ntf.icon = R.drawable.ic_launcher;
    	ntf.when = System.currentTimeMillis();
    	ntf.defaults = ntfDefault; 
    	ntf.tickerText = ntfTicker;
    	ntf.setLatestEventInfo(context, context.getString(R.string.app_name), ntfContent, pi);

    	nm.notify(ntfId, ntf);
    }

    /**
     * Get Default Notification Ways (Lights and Sound)
     */
    private int getDefault() {
        return Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
    }
}

