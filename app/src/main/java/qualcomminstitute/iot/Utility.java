package qualcomminstitute.iot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;

public class Utility {
    public static void initView(TextView...views) {
        for(TextView view : views) {
            view.setText("");
            view.setError(null);
        }
    }

    public static void displayToastMessage(Handler handler, final Context context, final String Message) {
        handler.post(new Thread(){
            @Override
            public void run() {
                Toast.makeText(context, Message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean validateInputForm(Context context, TextView...views) {
        for(TextView view : views) {
            TextInputLayout layout = (TextInputLayout) view.getParent().getParent();
            if(layout.getHint() != null) {
                if(layout.getHint().toString().equals(context.getResources().getString(R.string.email))) {
                    String email = view.getText().toString();
                    if (!email.matches(InputFormCondition.EMAIL_CONDITION)) {
                        view.setError(InputFormCondition.ERROR_EMAIL);
                        view.requestFocus();
                        return false;
                    } else {
                        view.setError(null);
                    }
                }
                else if(layout.getHint().toString().equals(context.getResources().getString(R.string.password)) ||
                        layout.getHint().toString().equals(context.getResources().getString(R.string.current_password)) ||
                        layout.getHint().toString().equals(context.getResources().getString(R.string.new_password))) {
                    String password = view.getText().toString();
                    if (!password.matches(InputFormCondition.PASSWORD_CONDITION)) {
                        view.setError(InputFormCondition.ERROR_PASSWORD);
                        view.requestFocus();
                        return false;
                    } else {
                        view.setError(null);
                    }
                }
                else if(layout.getHint().toString().equals(context.getResources().getString(R.string.full_name))) {
                    String name = view.getText().toString();
                    if(!name.matches(InputFormCondition.FULL_NAME_CONDITION)) {
                        view.setError(InputFormCondition.ERROR_FULL_NAME);
                        view.requestFocus();
                        return false;
                    } else {
                        view.setError(null);
                    }
                }
                else if(layout.getHint().toString().equals(context.getResources().getString(R.string.age))) {
                    String age = view.getText().toString();
                    if(!age.matches(InputFormCondition.AGE_CONDITION)) {
                        view.setError(InputFormCondition.ERROR_AGE);
                        view.requestFocus();
                        return false;
                    } else {
                        view.setError(null);
                    }
                }
            }
            else {
                return false;
            }
        }
        return true;
    }

    public static boolean validatePassword(TextView viewPassword, TextView viewRepeat) {
        String password = viewPassword.getText().toString();
        String repeatPassword = viewRepeat.getText().toString();

        if(!password.equals(repeatPassword)) {
            viewRepeat.setError(InputFormCondition.ERROR_REPEAT_PASSWORD);
            viewRepeat.requestFocus();
            return false;
        } else {
            viewRepeat.setError(null);
        }
        return true;
    }

    public static void showYesNoDialog(Context context, String strTitle, String strContext, DialogInterface.OnClickListener listenerYes, DialogInterface.OnClickListener listenerNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setTitle(strTitle);
        builder.setMessage(strContext);
        builder.setPositiveButton("Yes", listenerYes);
        builder.setNegativeButton("No", listenerNo);
        builder.show();
    }

    public static String convertUnixTime(long timestamp) {
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return dateFormat.format(date);
    }

    public static String[] parseCSVFile(String fileData) {
        return fileData.split("\n");
    }

    public static String[] parseCSVString(String strData) {
        return strData.split(",");
    }

    public static Handler getBluetoothHandler(final Activity activity, final Handler handler) {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        final int LOCATION_PERMISSION = 0;

        @SuppressLint("HandlerLeak")
        final Handler bluetoothHandler = new Handler() {
            SharedPreferences data = activity.getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
            SharedPreferences.Editor dataEditor = data.edit();
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
                        @SuppressLint("HandlerLeak")
                        Handler airHandler = new Handler() {
                            @Override
                            public void handleMessage(Message message) {
                                switch (message.what) {
                                    case NetworkInterface.REQUEST_FAIL :
                                        break;
                                    case NetworkInterface.REQUEST_SUCCESS :
                                        try {
                                            // 응답 메세지 JSON 파싱
                                            JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                                            switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                                                case NetworkInterface.MESSAGE_SUCCESS :
                                                    break;
                                                case NetworkInterface.MESSAGE_FAIL :
                                                    switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                                        case "invalid client type":
                                                            Utility.displayToastMessage(handler, activity, NetworkInterface.TOAST_CLIENT_FAILED);
                                                            break;
                                                        case "already registered sensor":
                                                            Utility.displayToastMessage(handler, activity, NetworkInterface.TOAST_USED_SENSOR);
                                                            break;
                                                        case "not valid token":
                                                            Utility.displayToastMessage(handler, activity, NetworkInterface.TOAST_TOKEN_FAILED);
                                                            dataEditor.remove(PreferenceName.preferenceToken);
                                                            dataEditor.apply();
                                                            activity.finish();
                                                            break;
                                                        case "you already have sensor":
                                                            Utility.displayToastMessage(handler, activity, NetworkInterface.TOAST_SENSOR_EXIST);
                                                            break;
                                                        default:
                                                            Utility.displayToastMessage(handler, activity, NetworkInterface.TOAST_DEFAULT_FAILED);
                                                            break;
                                                    }
                                                    break;
                                            }
                                        }
                                        catch(JSONException e) {
                                            e.printStackTrace();
                                            Log.e(this.getClass().getName(), "JSON ERROR!");
                                            Utility.displayToastMessage(handler, activity, NetworkInterface.TOAST_EXCEPTION);
                                        }
                                }
                            }
                        };

                        try {
                            String[] sensorData = Utility.parseCSVFile(readMessage);

                            for(String csvString : sensorData) {
                                String[] sensor = Utility.parseCSVString(csvString);

                                // POST 데이터 전송을 위한 자료구조
                                JSONObject rootObject = new JSONObject();
                                rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                                rootObject.put(NetworkInterface.REQUEST_TOKEN, data.getString(PreferenceName.preferenceToken, null));
                                rootObject.put(NetworkInterface.REQUEST_ADDRESS, data.getString(PreferenceName.preferenceBluetoothAir, null));

                                for(int i = 0; i < NetworkInterface.CSV_DATA.length; ++i) {
                                    rootObject.put(NetworkInterface.CSV_DATA[i], sensor[i]);
                                    dataEditor.putString(NetworkInterface.CSV_DATA[i], sensor[i]);
                                }
                                dataEditor.apply();

                                LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // 사용자 권한 요청
                                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
                                } else {
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,locationListener);
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                    // 수동으로 위치 구하기
                                    String locationProvider = LocationManager.NETWORK_PROVIDER;
                                    Location currentLocation = locationManager.getLastKnownLocation(locationProvider);
                                    if (currentLocation != null) {
                                        rootObject.put(NetworkInterface.REQUEST_LAT, currentLocation.getLatitude());
                                        rootObject.put(NetworkInterface.REQUEST_LON, currentLocation.getLongitude());
                                    }
                                }

                                new RequestMessage(NetworkInterface.REST_AIR_QUALITY_INSERT, "POST", rootObject, airHandler).start();
                            }
                        } catch (JSONException e) {
                            Log.e(this.getClass().getName(), "JSON ERROR!");
                            Utility.displayToastMessage(handler, activity, TOAST_EXCEPTION);
                        }
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        if (null != activity) {
                            Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Constants.MESSAGE_TOAST:
                        if (null != activity) {
                            Toast.makeText(activity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };

        return bluetoothHandler;
    }
}