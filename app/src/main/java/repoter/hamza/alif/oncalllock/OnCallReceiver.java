package repoter.hamza.alif.oncalllock;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class OnCallReceiver extends BroadcastReceiver implements View.OnClickListener {

    private static final String TAG = "OnCallReceiver";
    private static String prevState = TelephonyManager.EXTRA_STATE_IDLE;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, " On Call Receiver Method");

        String currentState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(currentState)
                && TelephonyManager.EXTRA_STATE_IDLE.equals(prevState)) {
            prevState = TelephonyManager.EXTRA_STATE_RINGING;
            showPasswordScreen(context);
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(currentState)) {
            prevState = TelephonyManager.EXTRA_STATE_IDLE;
            if(isCallLocked){
                hidePasswordWindow();
            }
        }
    }

    private static String userPassword;
    private void showPasswordScreen(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MainActivity.PREF_NAME, 0);
        userPassword = preferences.getString(MainActivity.PASSWORD_KEY, null);
        if (userPassword != null) {
            Toast.makeText(context, "Show Password Screen", Toast.LENGTH_SHORT).show();
            lockDevice(context);
            showPasswordWindow(context);
        }
    }


    private static View passwordView;
    private static WindowManager windowManager;
    private static TextView tvPassword;
    private static TextView tvWrongPassword;
    public static boolean isCallLocked;

    private void showPasswordWindow(final Context context) {

        isCallLocked = true;

        passwordView = LayoutInflater.from(context).inflate(R.layout.view_password,null);

        tvPassword = passwordView.findViewById(R.id.tv_password);
        tvWrongPassword = passwordView.findViewById(R.id.tv_wrong_password);

        passwordView.findViewById(R.id.btn_number_0).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_1).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_2).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_3).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_4).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_5).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_6).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_7).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_8).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_number_9).setOnClickListener(this);
        passwordView.findViewById(R.id.btn_clear_all).setOnClickListener(this);
        passwordView.findViewById(R.id.iv_backspace).setOnClickListener(this);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT
                        , WindowManager.LayoutParams.MATCH_PARENT
                        , Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR  | WindowManager.LayoutParams.TYPE_PHONE
                        , WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        , PixelFormat.TRANSLUCENT);

        params.dimAmount = 0.5f;
        windowManager.addView(passwordView, params);

    }

    private static void hidePasswordWindow(){
        isCallLocked = false;
        windowManager.removeViewImmediate(passwordView);
    }

    private void lockDevice(Context context) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (policyManager.isAdminActive(new ComponentName(context, AdminPolicyReceiver.class))) {
            policyManager.lockNow();
        }
    }

    @Override
    public void onClick(View view) {
        tvWrongPassword.setVisibility(View.GONE);
        switch (view.getId()){
            case R.id.btn_number_0:
                tvPassword.append("0");
                break;
            case R.id.btn_number_1:
                tvPassword.append("1");
                break;
            case R.id.btn_number_2:
                tvPassword.append("2");
                break;
            case R.id.btn_number_3:
                tvPassword.append("3");
                break;
            case R.id.btn_number_4:
                tvPassword.append("4");
                break;
            case R.id.btn_number_5:
                tvPassword.append("5");
                break;
            case R.id.btn_number_6:
                tvPassword.append("6");
                break;
            case R.id.btn_number_7:
                tvPassword.append("7");
                break;
            case R.id.btn_number_8:
                tvPassword.append("8");
                break;
            case R.id.btn_number_9:
                tvPassword.append("9");
                break;
            case R.id.btn_clear_all:
                tvPassword.setText("");
                break;
            case R.id.iv_backspace:
                String password = tvPassword.getText().toString();
                if(password.length() > 0) {
                    password = password.substring(0, password.length() - 1);
                    tvPassword.setText(password);
                }
                break;
        }
        checkPassword();
    }

    private void checkPassword() {
        String enteredPassword = tvPassword.getText().toString();
        if(enteredPassword.length() == userPassword.length()){
            if(enteredPassword.equals(userPassword)){
                hidePasswordWindow();
            }else{
                tvWrongPassword.setVisibility(View.VISIBLE);
                tvPassword.setText("");
            }
        }
    }
}
