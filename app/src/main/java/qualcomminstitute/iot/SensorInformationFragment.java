package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class SensorInformationFragment extends Fragment {
    // Intent request codes
    private static final int REQUEST_AIR_CONNECT_DEVICE = 1;
    private static final int REQUEST_HEART_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BT = 0;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private Bluetooth bluetooth = null;

    private Button viewAirRegister, viewAirDeassociation, viewHeartRegister, viewHeartDeassociation;
    private TextView viewAirAddress, viewAirStatus, viewHeartAddress, viewHeartStatus;
    private String strToken, strAddress;

    private Handler handler;
    private ProgressDialog progressDialog;

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
        } else if (bluetooth == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetooth != null) {
            bluetooth.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (bluetooth != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetooth.getState() == Bluetooth.STATE_NONE) {
                // Start the Bluetooth chat services
                bluetooth.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_information, container, false);

        // Token 얻어오기
        SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, "");

        // Handler 생성
        handler = new Handler();

        // Progress Dialog 초기화
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sensor Registration...");

        viewAirAddress = view.findViewById(R.id.txtAirSensorAddress);
        viewAirStatus = view.findViewById(R.id.txtAirSensorStatus);
        viewHeartAddress = view.findViewById(R.id.txtHeartSensorAddress);
        viewHeartStatus = view.findViewById(R.id.txtHeartSensorStatus);
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
                setSensorDeassociation("air");
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
                setSensorDeassociation("heart");
            }
        });

        getSensorList();

        return view;
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        // Initialize the Bluetooth to perform bluetooth connections
        bluetooth = new Bluetooth(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
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

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetooth.getState() != Bluetooth.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.bluetooth_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the Bluetooth to write
            byte[] send = message.getBytes();
            bluetooth.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * The Handler that gets information back from the Bluetooth
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Bluetooth.STATE_CONNECTED:
                            break;
                        case Bluetooth.STATE_CONNECTING:
                            break;
                        case Bluetooth.STATE_LISTEN:
                        case Bluetooth.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // TODO : 블루투스 송신메세지
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("Android", readMessage);
                    // TODO : 블루투스 수신메세지
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_AIR_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, "air");
                }
                break;
            case REQUEST_HEART_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, "heart");
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
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
    private void connectDevice(Intent data, String strType) {
        // Get the device MAC address
        strAddress = data.getExtras().getString(BluetoothConnectActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strAddress);
        // Attempt to connect to the device
        bluetooth.connect(device, true);

        // ProgressDialog 생성
        progressDialog.show();

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

                            switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                case NetworkInterface.MESSAGE_SUCCESS :
                                    Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CHANGED_PASSWORD);
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
                                            SharedPreferences token = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            SharedPreferences.Editor tokenEditor = token.edit();
                                            tokenEditor.clear();
                                            tokenEditor.apply();
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
            rootObject.put(NetworkInterface.REQUEST_ADDRESS, strAddress);
            rootObject.put(NetworkInterface.REQUEST_TYPE, strType);

            new RequestMessage(NetworkInterface.REST_SENSOR_REGISTRATION, "POST", rootObject, registerHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
        }
    }

    private void registerSensor(String strType) {
        Intent serverIntent;
        switch(strType) {
            case "air" :
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(getActivity(), BluetoothConnectActivity.class);
                startActivityForResult(serverIntent, REQUEST_AIR_CONNECT_DEVICE);
                break;
            case "heart" :
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(getActivity(), BluetoothConnectActivity.class);
                startActivityForResult(serverIntent, REQUEST_AIR_CONNECT_DEVICE);
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
                                    Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CHANGED_PASSWORD);
                                    handler.post(new Thread(){
                                        @Override
                                        public void run() {
                                            try {
                                                if (returnObject.get(NetworkInterface.MESSAGE_AIR_ADDRESS) != null) {
                                                    viewAirAddress.setText(returnObject.getString(NetworkInterface.MESSAGE_AIR_ADDRESS));
                                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_online));
                                                }
                                                else {
                                                    viewAirAddress.setText(null);
                                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_offline));
                                                }
                                                if (returnObject.get(NetworkInterface.MESSAGE_HEART_ADDRESS) != null) {
                                                    viewHeartAddress.setText(returnObject.getString(NetworkInterface.MESSAGE_HEART_ADDRESS));
                                                    viewHeartStatus.setText(getResources().getString(R.string.bluetooth_online));
                                                }
                                                else {
                                                    viewHeartAddress.setText(null);
                                                    viewHeartStatus.setText(getResources().getString(R.string.bluetooth_offline));
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
                                            SharedPreferences token = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            SharedPreferences.Editor tokenEditor = token.edit();
                                            tokenEditor.clear();
                                            tokenEditor.apply();
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

            new RequestMessage(NetworkInterface.REST_SENSOR_LIST, "POST", rootObject, listHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(listHandler, getActivity(), TOAST_EXCEPTION);
        }
    }

    private void setSensorDeassociation(final String strType) {
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
                                    Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CHANGED_PASSWORD);
                                    handler.post(new Thread(){
                                        @Override
                                        public void run() {
                                            switch(strType) {
                                                case "air" :
                                                    viewAirAddress.setText(null);
                                                    viewAirStatus.setText(getResources().getString(R.string.bluetooth_offline));
                                                    break;
                                                case "heart" :
                                                    viewHeartAddress.setText(null);
                                                    viewHeartStatus.setText(getResources().getString(R.string.bluetooth_offline));
                                                    break;
                                            }
                                        }
                                    });
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
                                            SharedPreferences token = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                            SharedPreferences.Editor tokenEditor = token.edit();
                                            tokenEditor.clear();
                                            tokenEditor.apply();
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

            new RequestMessage(NetworkInterface.REST_SENSOR_DEREGISTRATION, "PUT", rootObject, listHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(listHandler, getActivity(), TOAST_EXCEPTION);
        }
    }
}