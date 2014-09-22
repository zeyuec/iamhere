package com.obcerver.iamhere.manager;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.obcerver.iamhere.model.CGeofence;
import com.obcerver.iamhere.manager.GeofenceTransitionIntentService;
import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.lib.CUtil;
import com.obcerver.iamhere.lib.BaseManager;

/**
 * Geofence Manager
 * It manages the Geofence Monitoring Service, start or top the Geofence.
 * When the user enter/leave Cornell Tech, it sends a GeofenceTransitionIntentService.
 * @author Cary Zeyue Chen
 */
public class GeofenceManager extends BaseManager implements
                                                     OnAddGeofencesResultListener,
                                                     ConnectionCallbacks,
                                                     OnConnectionFailedListener,
                                                     OnRemoveGeofencesResultListener              
{

    private final static String ctName = "Cornell Tech";
    private final static String ctAddr = "111, 8th Ave, #302 NYC";
    private final static double ctLatitude = 40.74093D;
    private final static double ctLongitude = -74.002158D;
    private final static double ctRadius = 100.0D;
    
    private LocationClient      locationClient;
    private LocationRequest     locationRequest;
    private PendingIntent       geofenceRequestIntent;
        
    public GeofenceManager(Context c) {
        super(c);
    }

    public void startGeofence() {
        locationClient = new LocationClient(getContext(), this, this);
        locationClient.connect();
    }

    public void stopGeofence() {
        locationClient.removeGeofences(geofenceRequestIntent, this);
    }

    // Geofence callbacks
    
    @Override public void onConnected(Bundle connectionHint) {
        CLog.v("connected");
        
        // add geofence to locationclient
        ArrayList<CGeofence> storeList = getGeofenceList();
        if (null != storeList && storeList.size() > 0) {
            ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();
            for (CGeofence store : storeList) {
                float radius = (float) store.getRadius();
                Geofence geofence = new Geofence.Builder()
                    .setRequestId(store.getId())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion(store.getLatitude(), store.getLongitude(), radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();
                geofenceList.add(geofence);
                CLog.v("add " + geofence.getRequestId());
            }

            // create intent to start a intent service, which can run in background
            Intent transitionIntentService = new Intent(getContext(), GeofenceTransitionIntentService.class);
            geofenceRequestIntent = PendingIntent.getService(getContext(), 0,
                                                             transitionIntentService,
                                                             PendingIntent.FLAG_UPDATE_CURRENT);
            locationClient.addGeofences(geofenceList, geofenceRequestIntent, this);
            CLog.v("add geofence");
        }
    }

    private ArrayList<CGeofence> getGeofenceList() {
        ArrayList<CGeofence> gfList = new ArrayList<CGeofence>();
        CGeofence gf = new CGeofence();
        gf.setId(ctName);
        gf.setAddress(ctAddr);
        gf.setLatitude(ctLatitude);
        gf.setLongitude(ctLongitude);
        gf.setRadius(ctRadius);
        gfList.add(gf);
        return gfList;
    }
    
    @Override public void onDisconnected() {
        // @todo
    }
    
    @Override public void onConnectionFailed(ConnectionResult result) {
        // @todo
    }

    @Override public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (statusCode != 0) {
            CUtil.toast(getContext(), "failed");
        } else {
            CUtil.toast(getContext(), "Add Geofence Success!");
            // @todo send message to activity
        }
    }

    @Override public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {
        if (statusCode == LocationStatusCodes.SUCCESS) {
            // handle success
        } else {
            // handle failure
        }
        locationClient.disconnect();
    }
    
    @Override public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        
    }
}
                           



