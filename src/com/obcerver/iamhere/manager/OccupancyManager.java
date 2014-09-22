package com.obcerver.iamhere.manager;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.obcerver.iamhere.lib.BaseManager;
import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.lib.CHttpConnection;
import com.obcerver.iamhere.lib.Constants;
import com.obcerver.iamhere.model.UserModel;
import com.obcerver.iamhere.model.User;

/**
 * Occupancy Network Request Manager
 * It offers APIs to communicate with the server and handle the return data.
 *   0. list who are online (not in use)
 *   1. first arrive occupancy 
 *   2. update 
 *   3. depart
 * @author Cary Zeyue Chen
 */
public class OccupancyManager extends BaseManager {
    
    private UserModel userModel;
    
    public OccupancyManager(Context c) {
        super(c);
        initVal();
    }
    
    private void initVal() {
        this.userModel = new UserModel(getContext());
    }

    // list handler
    public class OccupancyListHandler extends Handler {
        @Override public void handleMessage(Message msg) {
            // CLog.v(msg.what);
            switch (msg.what) {
            case CHttpConnection.MESSAGE_ON_SUCCESS:
                String retString = (String) msg.obj;
                CLog.v(retString);
                break;
            }
        }        
    }
    // occupancy handler
    public class OccupancyHandler extends Handler {
        @Override public void handleMessage(Message msg) {
            // CLog.v(msg.what);
            switch (msg.what) {
            case CHttpConnection.MESSAGE_ON_SUCCESS:
                String retString = (String) msg.obj;
                CLog.v(retString);
                handleOccupancyReturnData(retString);
                break;
            }
        }        
    }
    
    private void handleOccupancyReturnData(String retString) {
        try {
            JSONObject retJson = new JSONObject(retString);
            int id = retJson.getInt("id");
            String arrive = retJson.getString("arrive");
            String depart = retJson.getString("depart");
            String name = retJson.getString("name");
            String floor = retJson.getString("floor");
            String update = retJson.getString("update");
            CLog.v("return id: " + id);

            // add user to current user
            User currentUser = userModel.getCurrentUser();
            currentUser.setId(String.valueOf(id));
            userModel.saveUser(currentUser);

            CLog.v("new user id saved");

        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    // occupancy update handler
    public class OccupancyUpdateHandler extends Handler {
        @Override public void handleMessage(Message msg) {
            if (msg.what == CHttpConnection.MESSAGE_ON_SUCCESS) {
                CLog.v("update suc!");
                String retString = (String) msg.obj;
                CLog.v("server return: " + retString);
            } else if (msg.what == CHttpConnection.MESSAGE_ON_ERROR
                       || msg.what == CHttpConnection.MESSAGE_ON_EXCEPTION) {
                CLog.v("update fail!");
                userModel.setCurrentUserLeave();
            }
        }
    }
    
    // occupancy depart handler
    public class OccupancyDepartHandler extends Handler {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
            case CHttpConnection.MESSAGE_ON_SUCCESS:
                String retString = (String) msg.obj;
                CLog.v(retString);
                userModel.setCurrentUserLeave();
                break;
            }
        }        
    }

    
    // list
    public void getOccupancyList() {
        String connUrl = Constants.Server.API_OCCUPANCY;
        CHttpConnection connList = new CHttpConnection(connUrl, null);
        connList.get(new OccupancyListHandler());
    }

    // occupancy
    public void postOccupancy(String name) {
        postOccupancy(name, null);
    }

    public void postOccupancy(String name, String floor) {
        String connUrl = Constants.Server.API_OCCUPANCY;
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();;
        params.add(new BasicNameValuePair("name", name));
        if (floor != null) {
            params.add(new BasicNameValuePair("floor", floor));
        }
        CHttpConnection connOccu = new CHttpConnection(connUrl, params);
        connOccu.post(new OccupancyHandler());
    }
    
    // update
    public void postOccupancyUpdate(String id) {
        postOccupancyUpdate(id, null);
    }

    public void postOccupancyUpdate(String id, String floor) {
        if (id == null) {
            CLog.v("error on update, id is null");
            return;
        }
        
        String connUrl = Constants.Server.API_OCCUPANCY_UPDATE;
        connUrl = connUrl.replaceAll(":id", id);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();;
        
        if (floor != null) {
            params.add(new BasicNameValuePair("floor", floor));
        }
        
        CHttpConnection connOccu = new CHttpConnection(connUrl, params);
        connOccu.post(new OccupancyUpdateHandler());
    }

    // depart
    public void postOccupancyDepart(String id) {
        if (id == null) {
            CLog.v("error on depart, id is null");
            return;
        }
        
        CLog.v("try to depart id: " + id);
        String connUrl = Constants.Server.API_OCCUPANCY_DEPART;
        connUrl = connUrl.replaceAll(":id", id);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();;
        CHttpConnection connOccu = new CHttpConnection(connUrl, params);
        connOccu.post(new OccupancyDepartHandler());
    }
    
}
