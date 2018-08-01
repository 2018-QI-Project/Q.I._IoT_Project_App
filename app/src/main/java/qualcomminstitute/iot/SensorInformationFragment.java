package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SensorInformationFragment extends Fragment {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothCommunication bluetoothCommunication;
    private String strConnectDeviceName;
    private StringBuffer strOutBuffer;

    private Button viewAirRegister;
    private Button viewAirDeassociation;
    private Button viewHeartRegister;
    private Button viewHeartDeassociation;

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommunication.STATE_CONNECTED:
                            break;
                        case BluetoothCommunication.STATE_CONNECTING:
                            break;
                        case BluetoothCommunication.STATE_LISTEN:
                        case BluetoothCommunication.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // TODO : Bluetooth 메세지 전송부분
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("Android", readMessage);
                    // TODO : Bluetooth 메세지 수신부분
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    strConnectDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), "Connected to" + strConnectDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Initialize the BluetoothCommunication to perform bluetooth connections
                    bluetoothCommunication = new BluetoothCommunication(getActivity(), handler);

                    // Initialize the buffer for outgoing messages
                    strOutBuffer = new StringBuffer("");
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getActivity(), R.string.bluetooth_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    public SensorInformationFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothCommunication == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            bluetoothCommunication = new BluetoothCommunication(getActivity(), handler);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_information, container, false);

        viewAirRegister = view.findViewById(R.id.btnAirSensorRegister);
        viewAirRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog bluetooth_dialog = new Dialog(getActivity());
                bluetooth_dialog.setContentView(R.layout.dialog_bluetooth);
                bluetooth_dialog.show();
            }
        });
        viewAirDeassociation = view.findViewById(R.id.btnAirSensorDeassociation);
        viewAirDeassociation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ensureDiscoverable();
            }
        });
        viewHeartRegister = view.findViewById(R.id.btnHeartSensorRegister);
        viewHeartRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog bluetooth_dialog = new Dialog(getActivity());
                bluetooth_dialog.setContentView(R.layout.dialog_bluetooth);
                bluetooth_dialog.show();
            }
        });
        viewHeartDeassociation = view.findViewById(R.id.btnHeartSensorDeassociation);
        viewHeartDeassociation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ensureDiscoverable();
            }
        });

        return view;
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothCommunication.getState() != BluetoothCommunication.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.bluetooth_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothCommunication to write
            byte[] send = message.getBytes();
            bluetoothCommunication.write(send);

            // Reset out string buffer to zero and clear the edit text field
            strOutBuffer.setLength(0);
        }
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link BluetoothDialog#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(BluetoothDialog.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bluetoothCommunication.connect(device, secure);
    }
}