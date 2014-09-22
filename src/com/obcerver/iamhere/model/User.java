package com.obcerver.iamhere.model;

/**
 * Custom User Object
 * @author Cary Zeyue Chen
 */
public class User {
    public final static int UNDETECTED_FLOOR = -1;
    
    private String id, name;
    private int floor;
    private boolean loginStatus;

    public User() {
        this.id = null;
        this.name = null;
        this.loginStatus = false;
        this.floor = UNDETECTED_FLOOR;
    }

    public User(String pName) {
        this.id = null;
        this.name = pName;
        this.loginStatus = true;
        this.floor = UNDETECTED_FLOOR;
    }

    public User(String pId, String pName, boolean pLoginStatus, int pFloor) {
        this.id = pId;
        this.name = pName;
        this.loginStatus = pLoginStatus;
        this.floor = pFloor;
    }

    // id
    public void setId(String pId) {
        this.id = pId;
    }
    public String getId() {
        return this.id;
    }

    // name
    public void setName(String pName) {
        this.name = pName;
    }

    public String getName() {
        return this.name;
    }

    // login status
    public void setLoginStatus(boolean pLoginStatus) {
        this.loginStatus = pLoginStatus;
    }

    public boolean getLoginStatus() {
        return this.loginStatus;
    }

    // floor
    public void setFloor(int pFloor) {
        this.floor = pFloor;
    }
    
    public int getFloor() {
        return this.floor;
    }
}
