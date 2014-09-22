package com.obcerver.iamhere;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.obcerver.iamhere.lib.CLog;
import com.obcerver.iamhere.lib.CUtil;
import com.obcerver.iamhere.model.User;
import com.obcerver.iamhere.model.UserModel;

/**
 * Main Activity
 * The first Activity of the application, it required the user to input their name
 * @author Cary Zeyue Chen
 */
public class MainActivity extends Activity
{

    private UserModel                userModel;
    private Button                   buttonLogin;
    private EditText                 editTextUserName;
    
    @Override public void onCreate(Bundle savedInstanceState) {

        // add view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // init
        initVal();
        initView();

        // check if login
        if (userModel.getCurrentUser().getLoginStatus()) {
            // goto home activity
            CLog.v("already login");
            gotoHomeActivity();
        } else {
            // stay here, input name
            CLog.v("not login");
        }
    }

    private void initVal() {
        this.userModel = new UserModel(this);
    }

    private void initView() {
        this.buttonLogin = (Button) findViewById(R.id.main_button_login);
        this.editTextUserName = (EditText) findViewById(R.id.main_edittext_username);
        buttonLogin.setOnClickListener(new LoginClickListener());
    }

    private class LoginClickListener implements View.OnClickListener {
        @Override public void onClick(View v) {
            if (editTextUserName.getText().length() == 0) {
                CUtil.toast(getApplicationContext(), R.string.main_button_login_click_username_empty);
            } else {
                User u = new User(editTextUserName.getText().toString());
                userModel.saveUser(u);
                gotoHomeActivity();
            }
        }
    }


    private void gotoHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);            
    }
}
