package repoter.hamza.alif.oncalllock;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int READ_PHONE_STATE_REQUEST_CODE = 1;
    private static final int DRAW_OVER_APPS_REQUEST_CODE = 2;

    private static final String TAG = "MainActivity";
    public static final String PREF_NAME = "OnCallPref";
    public static final String PASSWORD_KEY = "Password";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Button btnChangePassword, btnSave, btnExit;
    private EditText etPassword, etOldPassword;
    private LinearLayout linearSetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        preferences = getSharedPreferences(PREF_NAME, 0);
        editor = preferences.edit();

        checkReadPhoneStatePermission();

        String password = preferences.getString(PASSWORD_KEY, null);
        if (password != null) {
            showNewPasswordViews(false);
        } else {
            showNewPasswordViews(true);
        }
    }

    private void showNewPasswordViews(boolean show) {
        if (show) {
            linearSetPassword.setVisibility(View.VISIBLE);
            btnChangePassword.setVisibility(View.GONE);
            etOldPassword.setVisibility(View.GONE);
        } else {
            btnChangePassword.setVisibility(View.VISIBLE);
            etOldPassword.setVisibility(View.VISIBLE);
            linearSetPassword.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        btnChangePassword = (Button) findViewById(R.id.btn_change_password);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnExit = (Button) findViewById(R.id.btn_exit);
        etPassword = (EditText) findViewById(R.id.et_password);
        etOldPassword = (EditText) findViewById(R.id.et_old_password);
        linearSetPassword = (LinearLayout) findViewById(R.id.linear_set_password);

        btnChangePassword.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    private void checkReadPhoneStatePermission() {
        Log.d(TAG, "In Check Read Phone Permission Method");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "Request Read Phone State Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_CODE);
        } else {
            checkAdminPolicy();
            checkDrawOverAppsPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "In Request Permission Result Method");
        if (requestCode == READ_PHONE_STATE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "User Accepted Read Phone State Permission");
                checkAdminPolicy();
                checkDrawOverAppsPermission();
            } else {
                Log.d(TAG, "User Denied Read Phone State Permission");
            }
        }
    }

    private void checkDrawOverAppsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                new AlertDialog.Builder(this)
                        .setTitle("Note!!")
                        .setCancelable(false)
                        .setMessage("You should allow Draw Over Apps permission !!")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                i.setData(Uri.parse("package:" + getPackageName()));
                                startActivityForResult(i, DRAW_OVER_APPS_REQUEST_CODE);
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DRAW_OVER_APPS_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                checkDrawOverAppsPermission();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_change_password) {
            String oldPassword = preferences.getString(PASSWORD_KEY, "");
            String password = etOldPassword.getText().toString();

            if (oldPassword.equals(password)) {
                etOldPassword.setText("");
                savePassword();
            } else {
                etOldPassword.setError("Password doesn't match");
            }

        } else if (view.getId() == R.id.btn_save) {
            savePassword();
        } else {
            finish();
        }
    }

    private void savePassword() {
        String password = etPassword.getText().toString();
        if (password.trim().length() > 2) {
            editor.putString(PASSWORD_KEY, password);
            editor.apply();
            etPassword.setText("");
            showNewPasswordViews(false);
            Toast.makeText(this, "Password Saved", Toast.LENGTH_SHORT).show();
        } else {
            etPassword.setError("Password Length must be > 2");
        }
    }

    private void checkAdminPolicy() {
        ComponentName componentName = new ComponentName(this, AdminPolicyReceiver.class);
        DevicePolicyManager policyManager
                = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!policyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "On Call App Will Lock Phone in Incoming Calls");
            startActivityForResult(intent, 1);
        }
    }

}
