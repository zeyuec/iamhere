package com.obcerver.iamhere.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.obcerver.iamhere.HomeActivity;
import com.obcerver.iamhere.lib.BaseManager;
import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.lib.CNotification;
import com.obcerver.iamhere.lib.CUtil;
import com.obcerver.iamhere.model.UserModel;
import com.obcerver.iamhere.model.User;

/**
 * Floor Detetion Manager
 * It listens on Wifi Scan Result, analyse the result to detecti which floor the user stays.
 *   Only implement the pattern of 3rd floor of Cornell Tech: just count the number of "RedRover" and "eduroam" SSIDs,
 *   If the result has more than 5 SSIDs for each network, we could say the user is in the 3rd floor.
 * @author Cary Zeyue Chen
 */
public class FloorDetectionManager extends BaseManager {
    
    Context                      context;    
    WifiManager                  wifiManager;
    WifiScanResultReceiver       wifiScanResultReceiver;
    UserModel                    userModel;
    
    public FloorDetectionManager(Context c) {
        super(c);
        initVal();
    }

    private void initVal() {
        // init wifi manager and usermodel
        this.wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        this.userModel = new UserModel(getContext());

        // check wifi status
        if (wifiManager.isWifiEnabled() == false) {
            CUtil.toast(getContext(), "wifi is disabled..making it enabled");
            wifiManager.setWifiEnabled(true);
        }
        
        // wifi scan result receiver
        this.wifiScanResultReceiver = new WifiScanResultReceiver();
        getContext().registerReceiver(wifiScanResultReceiver,
                                      new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        
    }
    
    private class WifiScanResultReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent) {
            String debugStr = "";
            
            List<ScanResult> wifiList;
            wifiList = wifiManager.getScanResults();
            int countEdu = 0, countRed = 0;
            for(int i = 0; i < wifiList.size(); i++) {
                if (wifiList.get(i).SSID.equals("eduroam")) {
                    countEdu++;
                } else if (wifiList.get(i).SSID.equals("RedRover")) {
                    countRed++;
                }
                debugStr += wifiList.get(i).SSID + " " + String.valueOf(wifiList.get(i).level) + "\n";
            }
            
            // detect floor
            User u = userModel.getCurrentUser();
            if (countEdu > 5 && countRed > 5) {
                u.setFloor(3);
                // for debug
                // CNotification ntf;
                // Intent ntfIntent = new Intent(getContext(), HomeActivity.class);
                // String ntfString = "Edu Count: " + String.valueOf(countEdu) + " " + "Red Count: " + String.valueOf(countRed);
                // ntf = new CNotification(getContext(),"Wifi Result", ntfString, ntfIntent);
                // ntf.show();
            } else {
                u.setFloor(User.UNDETECTED_FLOOR);
            }
            // CLog.v("Detection Ret: " + String.valueOf(userModel.getCurrentUser().getFloor()));
            userModel.saveUser(u);
        }
    }
    
    public void tryGetFloor() {
        wifiManager.startScan();
    }

    public void finish() {
        if (wifiScanResultReceiver != null) {
            getContext().unregisterReceiver(wifiScanResultReceiver);
        }
    }
}
    

