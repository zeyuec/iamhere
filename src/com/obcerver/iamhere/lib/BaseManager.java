package com.obcerver.iamhere.lib;

import android.content.Context;

/**
 * Base Manager
 * @author Cary Zeyue Chen
 */
public class BaseManager {
	
    private Context context = null;

    public BaseManager(Context c) {
        this.context = c;
    }

    public Context getContext() {
        return this.context;
    }
}
