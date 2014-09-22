package com.obcerver.iamhere.manager;

import java.util.List;

import android.os.Bundle;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import com.obcerver.iamhere.HomeActivity;
import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.lib.CNotification;
import com.obcerver.iamhere.manager.UpdateService;
import com.obcerver.iamhere.manager.OccupancyManager;
import com.obcerver.iamhere.model.UserModel;

/**
 * Geofence Transition Intent Service
 * This geofence transition intent service runs when the user enter/leave CT.
 * @author Cary Zeyue Chen
 */
public class GeofenceTransitionIntentService extends IntentService {

    private UserModel userModel;
    
    public static final String SERVICE_NAME = "GeofenceTransitionIntentService";

    public GeofenceTransitionIntentService() {
        super(SERVICE_NAME);
    }

    @Override protected void onHandleIntent(Intent intent) {
        CLog.v("trigger geofence handleintent");
        if (LocationClient.hasError(intent)) {
            CLog.v("error");
            //todo error process
            
        } else {
            List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
            
            // @todo just process cornell tech
            // for (Geofence geofence : triggerList) {
            //     CLog.v(geofence.getRequestId());
            // }
            
            int transitionType = LocationClient.getGeofenceTransition(intent);
            CNotification ntf;
            Intent ntfIntent = new Intent(getApplicationContext(), HomeActivity.class);
            Intent updateService = new Intent(this, UpdateService.class);

            // create loc broadcast
            Intent locIntent = new Intent();
            locIntent.setAction(HomeActivity.EDIT_UI);
            Bundle data = new Bundle();
            data.putString("type", HomeActivity.EDIT_UI_LOC);

            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                CLog.v("enter ct");
                // show ntf
                ntf = new CNotification(this, "Enter Cornell Tech", "You're now at Cornell Tech.", ntfIntent);
                ntf.show();
                
                // send broadcast
                data.putBoolean("data", true);
                
                // start update
                startService(updateService);
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                CLog.v("leave ct");
                
                // show ntf
                ntf = new CNotification(this, "Leave Cornell Tech", "You're out of Cornell Tech", ntfIntent);
                ntf.show();

                data.putBoolean("data", false);
                // stop update
                stopService(updateService);
            }
            locIntent.putExtras(data);
            sendBroadcast(locIntent);
        }
    }
    
}
