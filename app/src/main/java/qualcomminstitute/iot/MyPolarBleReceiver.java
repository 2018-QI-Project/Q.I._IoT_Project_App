package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.StringTokenizer;

public class MyPolarBleReceiver extends BroadcastReceiver {
    public final static String ACTION_GATT_CONNECTED =
            "edu.ucsd.healthware.fw.device.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "edu.ucsd.healthware.fw.device.ble.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_HR_DATA_AVAILABLE =
            "edu.ucsd.healthware.fw.device.ble.ACTION_HR_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "edu.ucsd.healthware.fw.device.ble.EXTRA_DATA";
    private SharedPreferences preferences;
    private String strToken;
    private Context context;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL:
                    break;
                case NetworkInterface.REQUEST_SUCCESS:
                    try {
                        // 응답 메세지 JSON 파싱
                        JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                        switch (returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS:
                                break;
                            case NetworkInterface.MESSAGE_FAIL:
                                preferences.edit().remove(PreferenceName.preferenceRealHeart).apply();
                                preferences.edit().remove(PreferenceName.preferenceRealRR).apply();
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        break;
                                    case "already registered sensor":
                                        break;
                                    case "not valid token":
                                        break;
                                    case "you already have sensor":
                                        break;
                                    default:
                                        break;
                                }
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                    }
                    break;
            }
        }
    };

    public MyPolarBleReceiver(String strToken, Context context) {
        this.strToken = strToken;
        this.context = context;
        preferences = context.getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        if(preferences.getString(PreferenceName.preferenceBluetoothHeart, null) != null) {
            if (ACTION_GATT_CONNECTED.equals(action)) {
                Log.w(this.getClass().getName(), "####ACTION_GATT_CONNECTED");
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                preferences.edit().putString(PreferenceName.preferenceBluetoothHeartConnect, "Disconnect").apply();
            } else if (ACTION_HR_DATA_AVAILABLE.equals(action)) {
                //broadcastUpdate(ACTION_HR_DATA_AVAILABLE, heartRate+";"+pnnPercentage+";"+pnnCount+";"+rrThreshold+";"+bioHarnessSessionData.totalNN+";"+bioHarnessSessionData.lastRRvalue+";"+bioHarnessSessionData.sessionId);
                String data = intent.getStringExtra(EXTRA_DATA);
                StringTokenizer tokens = new StringTokenizer(data, ";");
                int heartRate = Integer.parseInt(tokens.nextToken());
                int pnnPercentage = Integer.parseInt(tokens.nextToken());
                int pnnCount = Integer.parseInt(tokens.nextToken());
                int rrThreshold = Integer.parseInt(tokens.nextToken());
                int totalNN = Integer.parseInt(tokens.nextToken());
                int lastRRvalue = Integer.parseInt(tokens.nextToken());
                String sessionId = tokens.nextToken();

                try {
                    // POST 데이터 전송을 위한 자료구조
                    JSONObject rootObject = new JSONObject();
                    rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                    rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
                    rootObject.put(NetworkInterface.REQUEST_ADDRESS, preferences.getString(PreferenceName.preferenceBluetoothHeart, null));
                    rootObject.put(NetworkInterface.REQUEST_TIMESTAMP, System.currentTimeMillis() / 1000);
                    rootObject.put(NetworkInterface.REQUEST_HEART_RATE, heartRate);
                    rootObject.put(NetworkInterface.REQUEST_RR_INTERVAL, lastRRvalue);

                    preferences.edit().putString(PreferenceName.preferenceBluetoothHeartConnect, "Connect").apply();
                    preferences.edit().putInt(PreferenceName.preferenceRealHeart, heartRate).apply();
                    preferences.edit().putInt(PreferenceName.preferenceRealRR, lastRRvalue).apply();

                    new RequestMessage(NetworkInterface.REST_HEART_INSERT, "POST", rootObject, handler).start();
                } catch (JSONException e) {
                    Log.e(this.getClass().getName(), "JSON ERROR!");
                }
                Log.w(this.getClass().getName(), "####Received heartRate: " +heartRate+" pnnPercentage: "+pnnPercentage+" pnnCount: "+pnnCount+" rrThreshold: "+rrThreshold+" totalNN: "+totalNN+" lastRRvalue: "+lastRRvalue+" sessionId: "+sessionId);
            }
        }
    }
}