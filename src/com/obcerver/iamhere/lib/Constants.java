package com.obcerver.iamhere.lib;

/**
 * Constants
 * @author Cary Chen
 * @since 09/13/2014
 */
public final class Constants {
    public final class App {
        public static final boolean DEBUG = true;
    }

    public final class Storage {
        public static final String USER_PREF = "userPref";
        public static final String USER_ID = "userId";
        public static final String USER_NAME = "userName";
        public static final String USER_LOGIN_STATUS = "userLoginStatus";
        public static final String USER_FLOOR = "userFloor";
    }

    public final class Server {
        public static final String API_DOMAIN = "http://iamhere.smalldata.io";
        public static final String API_OCCUPANCY = API_DOMAIN + "/occupancy";
        public static final String API_OCCUPANCY_UPDATE = API_DOMAIN + "/occupancy/:id/update";
        public static final String API_OCCUPANCY_DEPART = API_DOMAIN + "/occupancy/:id/depart";
        
    }
}
