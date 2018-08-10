package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;

    private String strToken;
    private ProgressDialog progressDialog;

    private MyPolarBleReceiver mPolarBleUpdateReceiver;
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL :
                    Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                    progressDialog.dismiss();
                    break;
                case NetworkInterface.REQUEST_SUCCESS :
                    try {
                        // 응답 메세지 JSON 파싱
                        JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));
                        SharedPreferences data = getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                        SharedPreferences.Editor dataEditor = data.edit();
                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                dataEditor.remove(PreferenceName.preferenceToken);
                                for(int i = 0; i < NetworkInterface.CSV_DATA.length; ++i) {
                                    dataEditor.remove(NetworkInterface.CSV_DATA[i]);
                                }
                                dataEditor.remove(PreferenceName.preferenceRealHeart);
                                dataEditor.remove(PreferenceName.preferenceRealRR);
                                dataEditor.apply();
                                MainActivity.this.finish();
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_CLIENT_FAILED);
                                        break;
                                    case "invalid tokenApp":
                                        Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_TOKEN_FAILED);
                                        dataEditor.remove(PreferenceName.preferenceToken);
                                        for(int i = 0; i < NetworkInterface.CSV_DATA.length; ++i) {
                                            dataEditor.remove(NetworkInterface.CSV_DATA[i]);
                                        }
                                        dataEditor.remove(PreferenceName.preferenceRealHeart);
                                        dataEditor.remove(PreferenceName.preferenceRealRR);
                                        dataEditor.apply();
                                        MainActivity.this.finish();
                                        break;
                                    default:
                                        Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_DEFAULT_FAILED);
                                        break;
                                }
                                break;
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                    }
                    finally {
                        progressDialog.dismiss();
                    }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Progress Dialog 초기화
        progressDialog = new ProgressDialog(MainActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sign Out...");

        // Token 얻어오기
        SharedPreferences preferences = MainActivity.this.getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, null);

        drawerLayout = findViewById(R.id.layDrawerLayout);
        navigationView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.barMainToolBar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(FragmentName.REAL_DATA.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            displayView(FragmentName.REAL_DATA.ordinal());
        }

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.menuRealTimeData:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.REAL_DATA.getName());
                            displayView(FragmentName.REAL_DATA.ordinal());
                        }
                        break;
                    case R.id.menuSensorInformation:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.SENSOR_INFORMATION.getName());
                            displayView(FragmentName.SENSOR_INFORMATION.ordinal());
                        }
                        break;
                    case R.id.menuMap:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.MAP.getName());
                            displayView(FragmentName.MAP.ordinal());
                        }
                        break;
                    case R.id.menuPreviousData:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.PREVIOUS_DATA.getName());
                            displayView(FragmentName.PREVIOUS_DATA.ordinal());
                        }
                        break;
                    case R.id.menuChangePassword:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.CHANGE_PASSWORD.getName());
                            displayView(FragmentName.CHANGE_PASSWORD.ordinal());
                        }
                        break;
                    case R.id.menuIDCancel :
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.ID_CANCEL.getName());
                            displayView(FragmentName.ID_CANCEL.ordinal());
                        }
                        break;
                    case R.id.menuSignOut :
                        Utility.showYesNoDialog(MainActivity.this, "Sign Out", "Do you want to Sign Out?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // ProgressDialog 생성
                                        progressDialog.show();

                                        try {
                                            // POST 데이터 전송을 위한 자료구조
                                            JSONObject rootObject = new JSONObject();
                                            rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
                                            rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);

                                            new RequestMessage(NetworkInterface.REST_SIGN_OUT, "POST", rootObject, handler).start();
                                        } catch (JSONException e) {
                                            Log.e(this.getClass().getName(), "JSON ERROR!");
                                            Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                                        }
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        @SuppressLint("HandlerLeak")
        Handler listHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case NetworkInterface.REQUEST_FAIL :
                        Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                        break;
                    case NetworkInterface.REQUEST_SUCCESS :
                        try {
                            // 응답 메세지 JSON 파싱
                            final JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                            switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                case NetworkInterface.MESSAGE_SUCCESS :
                                    try {
                                        if (!returnObject.isNull(NetworkInterface.MESSAGE_AIR_ADDRESS)) {
                                            SharedPreferences data = MainActivity.this.getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            data.edit().putString(PreferenceName.preferenceBluetoothAir, returnObject.getString(NetworkInterface.MESSAGE_AIR_ADDRESS)).apply();
                                            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(data.getString(PreferenceName.preferenceBluetoothAir, null));
                                            new Bluetooth(MainActivity.this, Utility.getBluetoothHandler(MainActivity.this, handler)).connect(device, false);
                                        }
                                        if (!returnObject.isNull(NetworkInterface.MESSAGE_HEART_ADDRESS)) {
                                            SharedPreferences data = MainActivity.this.getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            data.edit().putString(PreferenceName.preferenceBluetoothHeart, returnObject.getString(NetworkInterface.MESSAGE_HEART_ADDRESS)).apply();
                                            String heartAddress = data.getString(PreferenceName.preferenceBluetoothHeart, null);
                                            if(heartAddress != null) {
                                                activatePolar();
                                            }
                                        }
                                    }
                                    catch(JSONException e) {
                                        Log.e(this.getClass().getName(), "JSON ERROR!");
                                        Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                                    }
                                    break;
                                case NetworkInterface.MESSAGE_FAIL :
                                    switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                        case "invalid client type":
                                            Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_CLIENT_FAILED);
                                            break;
                                        case "invalid tokenApp":
                                            Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_TOKEN_FAILED);
                                            SharedPreferences data = MainActivity.this.getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            SharedPreferences.Editor dataEditor = data.edit();
                                            dataEditor.clear();
                                            dataEditor.apply();
                                            MainActivity.this.finish();
                                            break;
                                        case "There are not sensors registered now":
                                            Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_SENSOR_NOTHING);
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                    break;
                            }
                        }
                        catch(JSONException e) {
                            Log.e(this.getClass().getName(), "JSON ERROR!");
                            Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                        }
                }
            }
        };
        try {
            // POST 데이터 전송을 위한 자료구조
            JSONObject rootObject = new JSONObject();
            rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
            rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);

            new RequestMessage(NetworkInterface.REST_SENSOR_LIST, "POST", rootObject, listHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(listHandler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Utility.showYesNoDialog(MainActivity.this, "Sign Out", "Do you want to Sign Out?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // ProgressDialog 생성
                            progressDialog.show();

                            try {
                                // POST 데이터 전송을 위한 자료구조
                                JSONObject rootObject = new JSONObject();
                                rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
                                rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);

                                new RequestMessage(NetworkInterface.REST_SIGN_OUT, "POST", rootObject, handler).start();
                            } catch (JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                Utility.displayToastMessage(handler, MainActivity.this, NetworkInterface.TOAST_EXCEPTION);
                            }
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
        }
    }

    private void displayView(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new RealTimeFragment();
                break;
            case 1:
                fragment = new SensorInformationFragment();
                break;
            case 2:
                fragment = new MapFragment();
                break;
            case 3:
                fragment = new PreviousDataFragment();
                break;
            case 4:
                fragment = new ChangePasswordFragment();
                break;
            case 5:
                fragment = new IDCancelFragment();
                break;
            default:
                break;
        }
        if (fragment != null) {
            handler.post(new CommitFragmentRunnable(fragment));
        }
    }

    private class CommitFragmentRunnable implements Runnable {
        private Fragment fragment;

        CommitFragmentRunnable(Fragment fragment) {
            this.fragment = fragment;
        }

        @ Override
        public void run() {
            getFragmentManager().beginTransaction().replace(R.id.layFrameLayout, fragment).commit();
        }
    }

    protected void activatePolar() {
        Log.w(this.getClass().getName(), "activatePolar()");
        mPolarBleUpdateReceiver = new MyPolarBleReceiver(strToken, MainActivity.this);
        this.registerReceiver(mPolarBleUpdateReceiver, makePolarGattUpdateIntentFilter());
    }

    protected void deactivatePolar() {
        if(mPolarBleUpdateReceiver != null) {
            this.unregisterReceiver(mPolarBleUpdateReceiver);
            mPolarBleUpdateReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deactivatePolar();
        SharedPreferences preferences = this.getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor dataEditor = preferences.edit();
        dataEditor.remove(PreferenceName.preferenceToken);
        for(int i = 0; i < NetworkInterface.CSV_DATA.length; ++i) {
            dataEditor.remove(NetworkInterface.CSV_DATA[i]);
        }
        dataEditor.remove(PreferenceName.preferenceRealHeart);
        dataEditor.remove(PreferenceName.preferenceRealRR);
        dataEditor.remove(PreferenceName.preferenceBluetoothAir);
        dataEditor.remove(PreferenceName.preferenceBluetoothHeart);
        dataEditor.apply();
    }

    private static IntentFilter makePolarGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyPolarBleReceiver.ACTION_GATT_CONNECTED);
        intentFilter.addAction(MyPolarBleReceiver.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(MyPolarBleReceiver.ACTION_HR_DATA_AVAILABLE);
        return intentFilter;
    }
}