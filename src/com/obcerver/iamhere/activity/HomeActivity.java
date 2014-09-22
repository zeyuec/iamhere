package com.obcerver.iamhere;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.location.Location;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;

import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.model.User;
import com.obcerver.iamhere.model.UserModel;
import com.obcerver.iamhere.manager.GeofenceManager;
import com.obcerver.iamhere.manager.OccupancyManager;
import com.obcerver.iamhere.manager.UpdateService;

/**
 * Home Activity
 * It offers the monitoring screen of the user's status
 * It also registers a receiver to let other service to change the UI.
 * @author Cary Zeyue Chen
 */
public class HomeActivity extends Activity 
{
    public final static String EDIT_UI = "EDIT_UI";
    public final static String EDIT_UI_LOC = "EDIT_UI_LOC";
    public final static String EDIT_UI_FLOOR = "EDIT_UI_FLOOR";
    public final static String EDIT_UI_NEXT_UPDATE = "EDIT_UI_UPDATE_NEXT";
    
    private Button             buttonUpdate, buttonLogout;
    private TextView           textViewName, textViewLocation, textViewFloor;
    private TextView           textViewUpdateStatus, textViewNextUpdate;
    private GeofenceManager    geofenceManager;
    private OccupancyManager   occupancyManager;
    private UserModel          userModel;
    private HomeUIReceiver     homeUIReceiver;
    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        initVal();
        initView();
        initMain();
    }

    @Override public void onBackPressed() {
        // @todo confirm log off, earse user info, depart from cornell
        CLog.v("press back");
    }

    private void initVal() {
        this.geofenceManager = new GeofenceManager(this);
        this.occupancyManager = new OccupancyManager(this);
        this.userModel = new UserModel(this);

        this.homeUIReceiver = new HomeUIReceiver();
        registerReceiver(this.homeUIReceiver,
                         new IntentFilter(EDIT_UI)
                         );
        CLog.v("register receiver");
    }

    private void initView() {
        this.textViewLocation = (TextView) findViewById(R.id.home_textview_location);
        this.textViewName = (TextView) findViewById(R.id.home_textview_name);
        this.textViewFloor = (TextView) findViewById(R.id.home_textview_floor);
        this.textViewUpdateStatus = (TextView) findViewById(R.id.home_textview_update_status);
        this.textViewNextUpdate = (TextView) findViewById(R.id.home_textview_next_update);
        
        this.buttonLogout = (Button) findViewById(R.id.home_button_logout);
        
        buttonLogout.setOnClickListener(new LogoutClickListerner());
    }

    private void initMain() {
        // start geofence manager
        geofenceManager.startGeofence();

        // set user name
        textViewName.setText("Hello, " + userModel.getCurrentUser().getName() + "!");
        
        // set location info
        textViewLocation.setText("Locating...");

        // set floor info
        textViewFloor.setText("Unknown");
    }

    private class LogoutClickListerner implements View.OnClickListener {
        @Override public void onClick(View v) {
            stopAllService();
            HomeActivity.this.finish();
        }
    }
    
    private void stopAllService() {
        geofenceManager.stopGeofence();
        Intent updateService = new Intent(HomeActivity.this, UpdateService.class);
        stopService(updateService);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (homeUIReceiver != null) {
            unregisterReceiver(homeUIReceiver);
        }
    }

    private class HomeUIReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            CLog.v(intent.getAction());
            Bundle data = intent.getExtras();
            String type = data.getString("type");
            if (type.equals(EDIT_UI_FLOOR)) {
                // floor
                int floor = data.getInt("data", User.UNDETECTED_FLOOR);
                if (floor == User.UNDETECTED_FLOOR) {
                    textViewFloor.setText("Unknown");
                } else {
                    textViewFloor.setText("No."+String.valueOf(floor)+ " Floor");
                }
            } else if (type.equals(EDIT_UI_LOC)) {
                // loc
                boolean inCT = data.getBoolean("data", false);
                if (inCT) {
                    textViewLocation.setText("At Cornell Tech");
                    textViewUpdateStatus.setText("Update Service: Working...");
                    textViewUpdateStatus.setTextColor(Color.BLACK);
                    textViewNextUpdate.setTextColor(Color.BLACK);
                } else {
                    textViewLocation.setText("Out of Cornell Tech");
                    textViewFloor.setText("Unknown");
                    textViewUpdateStatus.setText("Update Service: Stopped");
                    textViewUpdateStatus.setTextColor(Color.RED);
                    textViewNextUpdate.setText("Next Update: NULL");
                    textViewNextUpdate.setTextColor(Color.RED);
                }
            } else if (type.equals(EDIT_UI_NEXT_UPDATE)) {
                // next update time
                String nextTime = data.getString("data");
                if (nextTime == null) {
                    nextTime = "NULL";
                }
                textViewNextUpdate.setText("Next Update: " + nextTime);
            }
        }
    }
}
