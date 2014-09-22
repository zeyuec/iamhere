package com.obcerver.iamhere.lib;

import android.content.Context;

/**
 * Base Model
 * @author Cary Zeyue Chen
 */
public class BaseModel {
	
    private Context context = null;

    public BaseModel(Context c) {
        this.context = c;
    }

    public Context getContext() {
        return this.context;
    }
}
