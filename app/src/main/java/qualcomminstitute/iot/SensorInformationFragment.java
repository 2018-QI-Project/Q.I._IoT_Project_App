package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class SensorInformationFragment extends Fragment {
    // Intent request codes
    private static final int REQUEST_HEART_CONNECT_DEVICE = 1;
    private static final int REQUEST_AIR_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BT = 0;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Member object for the chat services
     */
    private Bluetooth airBluetooth = null;

    private Button viewAirRegister, viewAirDeassociation, viewHeartRegister, viewHeartDeassociation;
    private TextView viewAirAddress, viewAirStatus, viewHeartAddress, viewHeartStatus;
    private String strToken, strAddress;
    private MyPolarBleReceiver mPolarBleUpdateReceiver;

    private SharedPreferences preferences;
    private Handler handler;
    private ProgressDialog progressDialog;

    private Thread checkConnect;
    private boolean flag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (airBluetooth == null) {
            setupAirBluetooth();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (airBluetooth != null) {
            airBluetooth.stop();
        }
        flag = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (airBluetooth != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (airBluetooth.getState() == Bluetooth.STATE_NONE) {
                // Start the Bluetooth chat services
                airBluetooth.start();
            }
        }
        flag = true;
        checkConnect.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_information, container, false);

        preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
        flag = true;
        // Handler 생성
        handler = new Handler();

        // Bluetooth 연결 검사
        checkConnect = new Thread() {
            @Override
            public void run() {
                while(flag) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (airBluetooth != null) {
                        if (airBluetooth.getState() != Bluetooth.STATE_CONNECTED) {
                            handler.post(new Thread() {
                                @Override
                                public void run() {
                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_offline));
                                }
                            });
                        } else {
                            handler.post(new Thread() {
                                @Override
                                public void run() {
                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_online));
                                }
                            });
                        }
                    }
                    if (preferences.getString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect").equals("Disconnect")) {
                        handler.post(new Thread() {
                            @Override
                            public void run() {
                                SharedPreferences preference = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
                                viewHeartStatus.setText(preference.getString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect"));
                            }
                        });
                    } else {
                        handler.post(new Thread() {
                            @Override
                            public void run() {
                                SharedPreferences preference = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
                                viewHeartStatus.setText(preference.getString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect"));
                            }
                        });
                    }
                }
            }
        };

        // Token 얻어오기
        SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, null);

        // Progress Dialog 초기화
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sensor Registration...");

        viewAirAddress = view.findViewById(R.id.txtAirSensorAddress);
        viewAirStatus = view.findViewById(R.id.txtAirSensorStatus);
        viewHeartAddress = view.findViewById(R.id.txtHeartSensorAddress);
        viewHeartStatus = view.findViewById(R.id.txtHeartSensorStatus);
        viewHeartStatus.setText(getResources().getString(R.string.bluetooth_offline));
        viewAirRegister = view.findViewById(R.id.btnAirSensorRegister);
        viewAirRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerSensor("air");
            }
        });
        viewAirDeassociation = view.findViewById(R.id.btnAirSensorDeassociation);
        viewAirDeassociation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!viewAirAddress.getText().equals("")) {
                    setSensorDeassociation("air", viewAirAddress.getText().toString());
                }
            }
        });
        viewHeartRegister = view.findViewById(R.id.btnHeartSensorRegister);
        viewHeartRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerSensor("heart");
            }
        });
        viewHeartDeassociation = view.findViewById(R.id.btnHeartSensorDeassociation);
        viewHeartDeassociation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!viewHeartAddress.getText().equals("")) {
                    setSensorDeassociation("heart", viewHeartAddress.getText().toString());
                    deactivatePolar();
                }
            }
        });

        getSensorList();
        return view;
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupAirBluetooth() {
        // Initialize the Bluetooth to perform bluetooth connections
        airBluetooth = new Bluetooth(getActivity(), Utility.getBluetoothHandler(getActivity(), new Handler()));
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_AIR_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_HEART_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    final String heartAddress = data.getExtras().getString(BluetoothConnectActivity.EXTRA_DEVICE_ADDRESS);
                    @SuppressLint("HandlerLeak")
                    Handler heartHandler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case NetworkInterface.REQUEST_FAIL :
                                    Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                                    break;
                                case NetworkInterface.REQUEST_SUCCESS :
                                    try {
                                        // 응답 메세지 JSON 파싱
                                        JSONObject returnObject = new JSONObject(msg.getData().getString(NetworkInterface.RESPONSE_DATA));

                                        SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                        SharedPreferences.Editor dataEditor = data.edit();

                                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                            case NetworkInterface.MESSAGE_SUCCESS :
                                                dataEditor.putString(PreferenceName.preferenceBluetoothHeart, heartAddress);
                                                dataEditor.apply();

                                                getSensorList();
                                                break;
                                            case NetworkInterface.MESSAGE_FAIL :
                                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                                    case "invalid client type":
                                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                                        break;
                                                    case "already registered sensor":
                                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_USED_SENSOR);
                                                        break;
                                                    case "not valid token":
                                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                                        dataEditor.remove(PreferenceName.preferenceToken);
                                                        for(int i = 0; i < NetworkInterface.CSV_DATA.length; ++i) {
                                                            dataEditor.remove(NetworkInterface.CSV_DATA[i]);
                                                        }
                                                        dataEditor.remove(PreferenceName.preferenceRealHeart);
                                                        dataEditor.remove(PreferenceName.preferenceRealRR);
                                                        dataEditor.apply();
                                                        getActivity().finish();
                                                        break;
                                                    case "you already have sensor":
                                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_SENSOR_EXIST);
                                                        break;
                                                    default:
                                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_DEFAULT_FAILED);
                                                        break;
                                                }
                                                break;
                                        }
                                    }
                                    catch(JSONException e) {
                                        e.printStackTrace();
                                        Log.e(this.getClass().getName(), "JSON ERROR!");
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                                    }
                                    finally {
                                        progressDialog.dismiss();
                                    }
                            }
                        }
                    };
                    try {
                        // POST 데이터 전송을 위한 자료구조
                        JSONObject rootObject = new JSONObject();
                        rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                        rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
                        rootObject.put(NetworkInterface.REQUEST_ADDRESS, heartAddress);
                        rootObject.put(NetworkInterface.REQUEST_TYPE, NetworkInterface.REQUEST_HEART_SENSOR);

                        new RequestMessage(NetworkInterface.REST_SENSOR_REGISTRATION, "POST", rootObject, heartHandler).start();
                    } catch (JSONException e) {
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    if(airBluetooth == null) {
                        setupAirBluetooth();
                    }
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getActivity(), R.string.bluetooth_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link BluetoothConnectActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        strAddress = data.getExtras().getString(BluetoothConnectActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strAddress);

        airBluetooth.connect(device, false);

        @SuppressLint("HandlerLeak")
        Handler registerHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case NetworkInterface.REQUEST_FAIL :
                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                        break;
                    case NetworkInterface.REQUEST_SUCCESS :
                        try {
                            // 응답 메세지 JSON 파싱
                            JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                            SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                            SharedPreferences.Editor dataEditor = data.edit();

                            switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                case NetworkInterface.MESSAGE_SUCCESS :
                                    dataEditor.putString(PreferenceName.preferenceBluetoothAir, strAddress);
                                    dataEditor.apply();
                                    break;
                                case NetworkInterface.MESSAGE_FAIL :
                                    switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                        case "invalid client type":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                            break;
                                        case "already registered sensor":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_USED_SENSOR);
                                            break;
                                        case "not valid token":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                            dataEditor.remove(PreferenceName.preferenceToken);
                                            for(int i = 0; i < NetworkInterface.CSV_DATA.length; ++i) {
                                                dataEditor.remove(NetworkInterface.CSV_DATA[i]);
                                            }
                                            dataEditor.remove(PreferenceName.preferenceRealHeart);
                                            dataEditor.remove(PreferenceName.preferenceRealRR);
                                            dataEditor.apply();
                                            getActivity().finish();
                                            break;
                                        case "you already have sensor":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_SENSOR_EXIST);
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                    break;
                            }
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                            Log.e(this.getClass().getName(), "JSON ERROR!");
                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                        }
                }
            }
        };

        try {
            // POST 데이터 전송을 위한 자료구조
            JSONObject rootObject = new JSONObject();
            rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
            rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
            rootObject.put(NetworkInterface.REQUEST_ADDRESS, strAddress);
            rootObject.put(NetworkInterface.REQUEST_TYPE, NetworkInterface.REQUEST_AIR);

            new RequestMessage(NetworkInterface.REST_SENSOR_REGISTRATION, "POST", rootObject, registerHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
        }
        getSensorList();
    }

    private void registerSensor(String strType) {
        Intent serverIntent;
        switch(strType) {
            case "air":
                setupAirBluetooth();
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(getActivity(), BluetoothConnectActivity.class);
                startActivityForResult(serverIntent, REQUEST_AIR_CONNECT_DEVICE);
                break;
            case "heart":
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(getActivity(), BluetoothConnectActivity.class);
                startActivityForResult(serverIntent, REQUEST_HEART_CONNECT_DEVICE);
                break;
        }
    }

    private void getSensorList() {
        @SuppressLint("HandlerLeak")
        Handler listHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case NetworkInterface.REQUEST_FAIL :
                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                        break;
                    case NetworkInterface.REQUEST_SUCCESS :
                        try {
                            // 응답 메세지 JSON 파싱
                            final JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                            switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                case NetworkInterface.MESSAGE_SUCCESS :
                                    handler.post(new Thread(){
                                        @Override
                                        public void run() {
                                            try {
                                                if (!returnObject.isNull(NetworkInterface.MESSAGE_AIR_ADDRESS)) {
                                                    SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                                    data.edit().putString(PreferenceName.preferenceBluetoothAir, returnObject.getString(NetworkInterface.MESSAGE_AIR_ADDRESS)).apply();
                                                    viewAirAddress.setText(returnObject.getString(NetworkInterface.MESSAGE_AIR_ADDRESS));
                                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_online));
                                                }
                                                else {
                                                    viewAirAddress.setText("");
                                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_offline));
                                                }
                                                if (!returnObject.isNull(NetworkInterface.MESSAGE_HEART_ADDRESS)) {
                                                    SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                                    data.edit().putString(PreferenceName.preferenceBluetoothHeart, returnObject.getString(NetworkInterface.MESSAGE_HEART_ADDRESS)).apply();
                                                    activatePolar();
                                                    if (data.getString(PreferenceName.preferenceBluetoothHeart, null) != null) {
                                                        viewHeartAddress.setText(data.getString(PreferenceName.preferenceBluetoothHeart, null));
                                                    }
                                                    if (data.getString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect").equals("Connect")) {
                                                        viewHeartStatus.setText(data.getString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect"));
                                                    }
                                                }
                                            }
                                            catch(JSONException e){
                                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                                Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                                            }
                                        }
                                    });
                                    break;
                                case NetworkInterface.MESSAGE_FAIL :
                                    switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                        case "invalid client type":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                            break;
                                        case "invalid tokenApp":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                            SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            SharedPreferences.Editor dataEditor = data.edit();
                                            dataEditor.clear();
                                            dataEditor.apply();
                                            getActivity().finish();
                                            break;
                                        case "There are not sensors registered now":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_SENSOR_NOTHING);
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                    break;
                            }
                        }
                        catch(JSONException e) {
                            Log.e(this.getClass().getName(), "JSON ERROR!");
                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
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
            Utility.displayToastMessage(listHandler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
        }
    }

    private void setSensorDeassociation(final String strType, String strAddress) {
        @SuppressLint("HandlerLeak")
        Handler listHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case NetworkInterface.REQUEST_FAIL :
                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                        break;
                    case NetworkInterface.REQUEST_SUCCESS :
                        try {
                            // 응답 메세지 JSON 파싱
                            final JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));
                            final SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                            final SharedPreferences.Editor dataEditor = data.edit();

                            switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                case NetworkInterface.MESSAGE_SUCCESS :
                                    switch(strType) {
                                        case "air" :
                                            handler.post(new Thread() {
                                                @Override
                                                public void run() {
                                                    viewAirAddress.setText("");
                                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_offline));
                                                    airBluetooth = null;
                                                    dataEditor.remove(PreferenceName.preferenceBluetoothAir);
                                                    dataEditor.apply();
                                                }
                                            });
                                            break;
                                        case "heart":
                                            handler.post(new Thread() {
                                                @Override
                                                public void run() {
                                                    viewHeartAddress.setText("");
                                                    dataEditor.putString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect").apply();
                                                    viewHeartStatus.setText(data.getString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect"));
                                                    dataEditor.remove(PreferenceName.preferenceBluetoothAir);
                                                    dataEditor.apply();
                                                }
                                            });
                                            break;
                                    }
                                    break;
                                case NetworkInterface.MESSAGE_FAIL :
                                    switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                        case "invalid client type":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                            break;
                                        case "unregistered sensor":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_UNUSED_SENSOR);
                                            break;
                                        case "not valid token":
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                            dataEditor.remove(PreferenceName.preferenceToken);
                                            dataEditor.apply();
                                            getActivity().finish();
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                    break;
                            }
                        }
                        catch(JSONException e) {
                            e.printStackTrace();
                            Log.e(this.getClass().getName(), "JSON ERROR!");
                            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                        }
                }
            }
        };
        try {
            // POST 데이터 전송을 위한 자료구조
            JSONObject rootObject = new JSONObject();
            rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
            rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
            rootObject.put(NetworkInterface.REQUEST_ADDRESS, strAddress);

            new RequestMessage(NetworkInterface.REST_SENSOR_DEREGISTRATION, "PUT", rootObject, listHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(listHandler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
        }
    }

    protected void activatePolar() {
        Log.w(this.getClass().getName(), "activatePolar()");
        if(mPolarBleUpdateReceiver == null) {
            mPolarBleUpdateReceiver = new MyPolarBleReceiver(strToken, getActivity());
            getActivity().registerReceiver(mPolarBleUpdateReceiver, makePolarGattUpdateIntentFilter());
        }
    }

    protected void deactivatePolar() {
        if(mPolarBleUpdateReceiver != null) {
            getActivity().unregisterReceiver(mPolarBleUpdateReceiver);
            mPolarBleUpdateReceiver = null;
        }
    }

    private static IntentFilter makePolarGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyPolarBleReceiver.ACTION_GATT_CONNECTED);
        intentFilter.addAction(MyPolarBleReceiver.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(MyPolarBleReceiver.ACTION_HR_DATA_AVAILABLE);
        return intentFilter;
    }
}