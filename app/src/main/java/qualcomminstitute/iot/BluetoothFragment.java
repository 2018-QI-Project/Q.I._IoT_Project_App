package qualcomminstitute.iot;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class BluetoothFragment extends Fragment {
    private final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothAdapter bluetooth;

    public BluetoothFragment() {
        bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    private boolean checkBluetooth() {
        if(bluetooth.isEnabled()) {
            return true;
        }
        else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
        return bluetooth.isEnabled();
    }
}