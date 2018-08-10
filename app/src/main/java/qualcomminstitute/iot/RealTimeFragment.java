package qualcomminstitute.iot;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RealTimeFragment extends Fragment {
    private TextView viewRealTimeCO, viewRealTimeSO2, viewRealTimeNO2, viewRealTimeO3, viewRealTimePM25;
    private TextView viewRealTimeAirDate;
    private TextView viewRealTimeHeartRate, viewRealTimeRRInterval;
    private String strToken, strAddress;

    private Handler handler;
    private Thread realTimeThread;
    private SharedPreferences preferences;
    private boolean flag;

    /*
    @SuppressLint("HandlerLeak")
    private final Handler airHandler = new Handler() {
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
                                final JSONObject dataObject = returnObject.getJSONObject(NetworkInterface.MESSAGE_AIR_DATA);
                                handler.post(new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            viewRealTimeCO.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_CO)));
                                            viewRealTimeSO2.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_SO2)));
                                            viewRealTimeNO2.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_NO2)));
                                            viewRealTimeO3.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_O3)));
                                            viewRealTimePM25.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_PM25)));
                                            viewRealTimeAirDate.setText(Utility.convertUnixTime(dataObject.getLong(NetworkInterface.MESSAGE_DATE)));
                                        }
                                        catch(JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                        break;
                                    case "not valid token":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                        SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor dataEditor = data.edit();
                                        dataEditor.remove(PreferenceName.preferenceToken);
                                        dataEditor.apply();
                                        getActivity().finish();
                                        break;
                                    case "not registered sensor":
                                    case "You have not air sensor":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_UNUSED_SENSOR);
                                        break;
                                    case "nothing data":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_NO_DATA);
                                        break;
                                    default:
                                        Log.d("TEST", returnObject.getString(NetworkInterface.MESSAGE_VALUE));
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

    @SuppressLint("HandlerLeak")
    private final Handler heartHandler = new Handler() {
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
                                final JSONObject dataObject = returnObject.getJSONObject(NetworkInterface.MESSAGE_HEART_DATA);
                                handler.post(new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            viewRealTimeHeartRate.setText(String.format(Locale.US, "%d", dataObject.getInt(NetworkInterface.MESSAGE_HEART_RATE)));
                                            viewRealTimeRRInterval.setText(String.format(Locale.US, "%d", dataObject.getInt(NetworkInterface.MESSAGE_RR_INTERVAL)));
                                        }
                                        catch(JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                        break;
                                    case "not valid token":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                        SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor dataEditor = data.edit();
                                        dataEditor.remove(PreferenceName.preferenceToken);
                                        dataEditor.apply();
                                        getActivity().finish();
                                        break;
                                    case "nothing data":
                                    case "You have not heart sensor":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_NO_DATA);
                                        break;
                                    case "not registered sensor":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_UNUSED_SENSOR);
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
    */

    public RealTimeFragment(){}

    @Override
    public void onResume() {
        super.onResume();
        flag = true;
        realTimeThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        flag = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        flag = true;
        handler = new Handler();

        // Token 얻어오기
        preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, null);
        strAddress = preferences.getString(PreferenceName.preferenceBluetoothAir, null);

        viewRealTimeCO = view.findViewById(R.id.txtRealTimeCO);
        viewRealTimeSO2 = view.findViewById(R.id.txtRealTimeSO2);
        viewRealTimeNO2 = view.findViewById(R.id.txtRealTimeNO2);
        viewRealTimeO3 = view.findViewById(R.id.txtRealTimeO3);
        viewRealTimePM25 = view.findViewById(R.id.txtReaTimePM25);
        viewRealTimeAirDate = view.findViewById(R.id.txtRealTimeAirDate);
        viewRealTimeHeartRate = view.findViewById(R.id.txtRealTimeHeartRate);
        viewRealTimeRRInterval = view.findViewById(R.id.txtRealTimeRRInterval);

        realTimeThread = new Thread() {
            @Override
            public void run() {
                while(flag){
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    handler.post(new Thread(){
                        @Override
                        public void run() {
                            viewRealTimeCO.setText(preferences.getString(NetworkInterface.CSV_DATA[0], null));
                            viewRealTimeSO2.setText(preferences.getString(NetworkInterface.CSV_DATA[1], null));
                            viewRealTimeNO2.setText(preferences.getString(NetworkInterface.CSV_DATA[2], null));
                            viewRealTimeO3.setText(preferences.getString(NetworkInterface.CSV_DATA[3], null));
                            viewRealTimePM25.setText(preferences.getString(NetworkInterface.CSV_DATA[4], null));
                            viewRealTimeHeartRate.setText(preferences.getString(PreferenceName.preferenceRealHeart, null));
                            viewRealTimeRRInterval.setText(preferences.getString(PreferenceName.preferenceRealRR, null));
                            viewRealTimeAirDate.setText(Utility.convertUnixTime((System.currentTimeMillis() / 1000)));
                            Log.d("POSTHANDLER", "1234");
                        }
                    });
                }
            }
        };

        return view;
    }
}