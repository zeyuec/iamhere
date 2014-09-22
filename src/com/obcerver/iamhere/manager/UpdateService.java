package com.obcerver.iamhere.manager;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.manager.OccupancyManager;
import com.obcerver.iamhere.model.UserModel;
import com.obcerver.iamhere.model.User;
import com.obcerver.iamhere.manager.FloorDetectionManager;
import com.obcerver.iamhere.HomeActivity;

/**
 * Update Service 
 * 1. This service starts when the user enter Cornell Tech and ends when the user leaves.
 *    Start and stop by GeofenceTransitionIntentService.
 * 2. The service uses AlarmManager to send interval broadcast
 *    and it also registers a receiver to catch the broadcast to update the user's status to the server
 * @author Cary Zeyue Chen
 */
public class UpdateService extends Service
{
    private final static String     BROADCAST_REPEAT_ACTION = "repeat_action";
    private final static int        EXEC_INTERVAL_SECOND = 10*60;
    private RepeatTaskReceiver      repeatTaskReceiver;
    private OccupancyManager        occupancyManager;
    private FloorDetectionManager   floorDetectionManager;
    private UserModel               userModel;
    
    // Service callbacks
    @Override public void onCreate() {
        super.onCreate();
        initVal();
        initMain();
        start();
    }

    @Override public IBinder onBind(Intent intent) {
    	return null;
    }

    private void initVal() {
        // register repeat call
        this.repeatTaskReceiver = new RepeatTaskReceiver();
        registerReceiver(this.repeatTaskReceiver,
                         new IntentFilter(BROADCAST_REPEAT_ACTION)
                         );
        // manager
        this.occupancyManager = new OccupancyManager(this);
        this.userModel = new UserModel(this);
        this.floorDetectionManager = new FloorDetectionManager(this);
    }
    
    private void initMain() {
        
    }

    public void start() {
        CLog.v("start");
		Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, 3);
        PendingIntent pendingIntent
            = PendingIntent.getBroadcast(this, 0,
                                         new Intent(BROADCAST_REPEAT_ACTION),
                                         PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP,
                        now.getTimeInMillis(),
                        EXEC_INTERVAL_SECOND*1000,
                        pendingIntent);
    }

    private void sendBroadcastNextTime() {
        // send broadcast
        Calendar nextTime = Calendar.getInstance();
        nextTime.add(Calendar.SECOND, EXEC_INTERVAL_SECOND);
        int hour = nextTime.get(Calendar.HOUR_OF_DAY);
        int minute = nextTime.get(Calendar.MINUTE);
        int second = nextTime.get(Calendar.SECOND);
        String nextTimeStr = hour+":"+minute+":"+second;
        CLog.v(nextTimeStr);

        Intent nextTimeIntent = new Intent();
        nextTimeIntent.setAction(HomeActivity.EDIT_UI);
        Bundle data = new Bundle();
        data.putString("type", HomeActivity.EDIT_UI_NEXT_UPDATE);
        data.putString("data", nextTimeStr);
        nextTimeIntent.putExtras(data);
        sendBroadcast(nextTimeIntent);
    }

    private void sendBroadcastFloor(int floor) {
        Intent floorIntent = new Intent();
        floorIntent.setAction(HomeActivity.EDIT_UI);
        Bundle data = new Bundle();
        data.putString("type", HomeActivity.EDIT_UI_FLOOR);
        data.putInt("data", floor);
        floorIntent.putExtras(data);
        sendBroadcast(floorIntent);
    }

    private class RepeatTaskReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context c, Intent i) {

            // send broadcast to change next time
            sendBroadcastNextTime();
            
            CLog.v("update");
            if (userModel.getCurrentUser().getId() == null) {
                CLog.v("no local id, try occupancy");
                occupancyManager.postOccupancy(userModel.getCurrentUser().getName());
            } else {
                floorDetectionManager.tryGetFloor();
                
                CLog.v("id " + userModel.getCurrentUser().getId());
                int floor = userModel.getCurrentUser().getFloor();
                String strFloor = null;
                if (floor == User.UNDETECTED_FLOOR) {
                    strFloor = "unknown";
                } else {
                    strFloor = String.valueOf(floor);
                }
                CLog.v("floor " + strFloor);
                occupancyManager.postOccupancyUpdate(userModel.getCurrentUser().getId(), strFloor);
                
                // send broadcast to change floor
                sendBroadcastFloor(floor);
            }
        }
    }

    @Override public void onDestroy() {
        try {
            CLog.v("try depart user id: " + userModel.getCurrentUser().getId());
            occupancyManager.postOccupancyDepart(userModel.getCurrentUser().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (floorDetectionManager != null) {
            floorDetectionManager.finish();
        }
    	super.onDestroy();
        if (repeatTaskReceiver != null) {
            unregisterReceiver(repeatTaskReceiver);
        }
        CLog.v("destroy");
    }    
    
}
