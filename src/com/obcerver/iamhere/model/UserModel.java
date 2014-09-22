package com.obcerver.iamhere.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.obcerver.iamhere.model.User;
import com.obcerver.iamhere.lib.BaseModel;
import com.obcerver.iamhere.lib.Constants;
import com.obcerver.iamhere.lib.CLog;

/**
 * User Model
 * This user model storages the user's data to the SharedPrefrence
 * @todo It can be optimaized by offering a memory cache
 * @author Cary Zeyue Chen
 */
public class UserModel extends BaseModel
{
    private SharedPreferences userStorage;
    
    public UserModel(Context c) {
        super(c);
        this.userStorage = getContext().getSharedPreferences(Constants.Storage.USER_PREF, 0);
    }

    public User getCurrentUser() {
        // @todo can use memory cache to faster this step
        return getUserFromStorage();
    }
    
    public User getUserFromStorage() {
        boolean loginStatus = userStorage.getBoolean(Constants.Storage.USER_LOGIN_STATUS, false);
        String id = userStorage.getString(Constants.Storage.USER_ID, null);
        String name = userStorage.getString(Constants.Storage.USER_NAME, null);
        int floor = userStorage.getInt(Constants.Storage.USER_FLOOR, User.UNDETECTED_FLOOR);
        User user = new User(id, name, loginStatus, floor);
        return user;
    }

    public boolean saveUser(User u) {
        if (u == null) {
            return false;
        } else {
            Editor editor = userStorage.edit();
            editor.putBoolean(Constants.Storage.USER_LOGIN_STATUS, u.getLoginStatus());
            editor.putString(Constants.Storage.USER_ID, u.getId());
            editor.putString(Constants.Storage.USER_NAME, u.getName());
            editor.putInt(Constants.Storage.USER_FLOOR, u.getFloor());
            boolean ret = editor.commit();
            return ret;
        }
    }

    public void setCurrentUserLeave() {
        User u = getCurrentUser();
        u.setId(null);
        u.setFloor(User.UNDETECTED_FLOOR);
        saveUser(u);
    }

    public void deleteCurrentUser() {
        Editor editor = userStorage.edit();
        editor.clear();
        editor.commit();
    }
}
