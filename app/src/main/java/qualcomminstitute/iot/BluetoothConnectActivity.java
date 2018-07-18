package qualcomminstitute.iot;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class BluetoothConnectActivity extends AppCompatActivity {
    private BluetoothAdapter bluetooth;

    public BluetoothConnectActivity() {
        bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    private boolean checkBluetooth() {
        if(bluetooth.isEnabled()) {
            return true;
        }
        else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
        return bluetooth.isEnabled();
    }
}