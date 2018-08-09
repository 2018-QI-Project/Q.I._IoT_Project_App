package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class RealTimeFragment extends Fragment {
    private TextView viewRealTimeCO, viewRealTimeSO2, viewRealTimeNO2, viewRealTimeO3, viewRealTimePM25;
    private TextView viewRealTimeAirDate;
    private TextView viewRealTimeHeartRate, viewRealTimeRRInterval;
    private String strToken, strAddress;

    private Handler handler;

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
                                final JSONObject dataObject = returnObject.getJSONObject(NetworkInterface.MESSAGE_AIR_DATA);
                                handler.post(new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            viewRealTimeHeartRate.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_HEART_RATE)));
                                            viewRealTimeRRInterval.setText(String.format(Locale.US, "%.2f", dataObject.getDouble(NetworkInterface.MESSAGE_RR_INTERVAL)));
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

    public RealTimeFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        handler = new Handler();

        // Token 얻어오기
        SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
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

        try {
            // POST 데이터 전송을 위한 자료구조
            JSONObject rootObject = new JSONObject();
            rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
            rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
            rootObject.put(NetworkInterface.REQUEST_USER_TYPE, NetworkInterface.REQUEST_USER);

            new RequestMessage(NetworkInterface.REST_AIR_QUALITY_REAL_TIME, "POST", rootObject, airHandler).start();

            rootObject = new JSONObject();
            rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
            rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
            rootObject.put(NetworkInterface.REQUEST_USER_TYPE, NetworkInterface.REQUEST_USER);

            new RequestMessage(NetworkInterface.REST_HEART_REAL_TIME, "POST", rootObject, heartHandler).start();
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "JSON ERROR!");
            Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
        }

        return view;
    }
}
