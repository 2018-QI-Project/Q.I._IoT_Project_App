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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PreviousDataFragment extends Fragment {
    private Spinner viewFilter;
    private Button viewSearch;
    private DatePicker dpkDate;
    private int year, month, date;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
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
                                final JSONArray dataArray = returnObject.getJSONArray(NetworkInterface.MESSAGE_AIR_DATA);
                                handler.post(new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            List<BarEntry> goodData = new ArrayList<>();
                                            List<BarEntry> moderateData = new ArrayList<>();
                                            List<BarEntry> unhealthyForSensitiveGroupsData = new ArrayList<>();
                                            List<BarEntry> unhealthyData = new ArrayList<>();
                                            List<BarEntry> veryUnhealthyData = new ArrayList<>();
                                            List<BarEntry> hazardousData = new ArrayList<>();

                                            for(int i = 0; i < dataArray.length(); ++i)  {
                                                JSONObject dataObject = dataArray.getJSONObject(i);

                                                switch(viewFilter.getSelectedItem().toString()){
                                                    case "CO":
                                                        switch((dataObject.getInt(NetworkInterface.SENSOR_AQI_DATA[0]) - 1) / 50) {
                                                            case 0 :
                                                                goodData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[0]))));
                                                                break;
                                                            case 1 :
                                                                moderateData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[0]))));
                                                                break;
                                                            case 2 :
                                                                unhealthyForSensitiveGroupsData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[0]))));
                                                                break;
                                                            case 3 :
                                                                unhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[0]))));
                                                                break;
                                                            case 4 :
                                                            case 5 :
                                                                veryUnhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[0]))));
                                                                break;
                                                            case 6 :
                                                            case 7 :
                                                            case 8 :
                                                            case 9 :
                                                                hazardousData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[0]))));
                                                                break;
                                                        }
                                                        break;
                                                    case "NO2":
                                                        switch((dataObject.getInt(NetworkInterface.SENSOR_AQI_DATA[1]) - 1) / 50) {
                                                            case 0 :
                                                                goodData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[1]))));
                                                                break;
                                                            case 1 :
                                                                moderateData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[1]))));
                                                                break;
                                                            case 2 :
                                                                unhealthyForSensitiveGroupsData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[1]))));
                                                                break;
                                                            case 3 :
                                                                unhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[1]))));
                                                                break;
                                                            case 4 :
                                                            case 5 :
                                                                veryUnhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[1]))));
                                                                break;
                                                            case 6 :
                                                            case 7 :
                                                            case 8 :
                                                            case 9 :
                                                                hazardousData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[1]))));
                                                                break;
                                                        }
                                                        break;
                                                    case "SO2":
                                                        switch((dataObject.getInt(NetworkInterface.SENSOR_AQI_DATA[2]) - 1) / 50) {
                                                            case 0 :
                                                                goodData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[2]))));
                                                                break;
                                                            case 1 :
                                                                moderateData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[2]))));
                                                                break;
                                                            case 2 :
                                                                unhealthyForSensitiveGroupsData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[2]))));
                                                                break;
                                                            case 3 :
                                                                unhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[2]))));
                                                                break;
                                                            case 4 :
                                                            case 5 :
                                                                veryUnhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[2]))));
                                                                break;
                                                            case 6 :
                                                            case 7 :
                                                            case 8 :
                                                            case 9 :
                                                                hazardousData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[2]))));
                                                                break;
                                                        }
                                                        break;
                                                    case "O3":
                                                        switch((dataObject.getInt(NetworkInterface.SENSOR_AQI_DATA[3]) - 1) / 50) {
                                                            case 0 :
                                                                goodData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[3]))));
                                                                break;
                                                            case 1 :
                                                                moderateData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[3]))));
                                                                break;
                                                            case 2 :
                                                                unhealthyForSensitiveGroupsData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[3]))));
                                                                break;
                                                            case 3 :
                                                                unhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[3]))));
                                                                break;
                                                            case 4 :
                                                            case 5 :
                                                                veryUnhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[3]))));
                                                                break;
                                                            case 6 :
                                                            case 7 :
                                                            case 8 :
                                                            case 9 :
                                                                hazardousData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[3]))));
                                                                break;
                                                        }
                                                        break;
                                                    case "PM2.5":
                                                        switch((dataObject.getInt(NetworkInterface.SENSOR_AQI_DATA[4]) - 1) / 50) {
                                                            case 0 :
                                                                goodData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[4]))));
                                                                break;
                                                            case 1 :
                                                                moderateData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[4]))));
                                                                break;
                                                            case 2 :
                                                                unhealthyForSensitiveGroupsData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[4]))));
                                                                break;
                                                            case 3 :
                                                                unhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[4]))));
                                                                break;
                                                            case 4 :
                                                            case 5 :
                                                                veryUnhealthyData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[4]))));
                                                                break;
                                                            case 6 :
                                                            case 7 :
                                                            case 8 :
                                                            case 9 :
                                                                hazardousData.add(new BarEntry(Float.parseFloat(dataObject.getString(NetworkInterface.MESSAGE_HOUR)), Float.parseFloat(dataObject.getString(NetworkInterface.SENSOR_AQI_DATA[4]))));
                                                                break;
                                                        }
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }

                                            BarDataSet goodDataSet = new BarDataSet(goodData, "Good");
                                            goodDataSet.setColor(getResources().getColor(R.color.good));
                                            BarDataSet moderateDataSet = new BarDataSet(moderateData, "Moderate");
                                            moderateDataSet.setColor(getResources().getColor(R.color.moderate));
                                            BarDataSet unhealthyForSensitiveGroupsDataSet = new BarDataSet(unhealthyForSensitiveGroupsData, "Unhealthy for Sensitive Groups");
                                            unhealthyForSensitiveGroupsDataSet.setColor(getResources().getColor(R.color.unhealthy_for_sensitive_groups));
                                            BarDataSet unhealthyDataSet = new BarDataSet(unhealthyData, "Unhealthy");
                                            unhealthyDataSet.setColor(getResources().getColor(R.color.unhealthy));
                                            BarDataSet veryUnhealthyDataSet = new BarDataSet(veryUnhealthyData, "Very Unhealthy");
                                            veryUnhealthyDataSet.setColor(getResources().getColor(R.color.very_unhealthy));
                                            BarDataSet hazardousDataSet = new BarDataSet(hazardousData, "Hazardous");
                                            hazardousDataSet.setColor(getResources().getColor(R.color.hazardous));

                                            List<IBarDataSet> dataSets = new ArrayList<>();
                                            dataSets.add(goodDataSet);
                                            dataSets.add(moderateDataSet);
                                            dataSets.add(unhealthyForSensitiveGroupsDataSet);
                                            dataSets.add(unhealthyDataSet);
                                            dataSets.add(veryUnhealthyDataSet);
                                            dataSets.add(hazardousDataSet);

                                            BarData data = new BarData(dataSets);
                                            data.setDrawValues(false);
                                            data.setValueTextSize(15f);
                                            data.setBarWidth(0.9f);

                                            barChart.setTouchEnabled(false);
                                            barChart.setData(data);

                                            List<String> list_x_axis_name = new ArrayList<>();
                                            for(int i = 0; i < 24; ++i) {
                                                list_x_axis_name.add(Integer.toString(i + 1));
                                                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(list_x_axis_name));
                                            }

                                            barChart.invalidate();
                                        }
                                        catch(JSONException e) {
                                            e.printStackTrace();
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
                                    case "You have not air sensor":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_NO_DATA);
                                        barChart.clear();
                                        barChart.invalidate();
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
    private BarChart barChart;

    public PreviousDataFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_previous_data, container, false);
        barChart = view.findViewById(R.id.chaPreviousData);
        viewFilter = view.findViewById(R.id.spiFilter);
        dpkDate = view.findViewById(R.id.dpkDate);
        dpkDate.setMaxDate(System.currentTimeMillis() - 1000);

        viewSearch = view.findViewById(R.id.btnSearch);
        viewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                year = dpkDate.getYear();
                month = dpkDate.getMonth();
                date = dpkDate.getDayOfMonth();

                SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
                String strToken = preferences.getString(PreferenceName.preferenceToken, null);
                try {
                    JSONObject rootObject = new JSONObject();
                    rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                    rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
                    rootObject.put(NetworkInterface.REQUEST_DATE, year + "-" + ((month + 1) < 10 ? "0" + (month + 1) : (month + 1)) + "-" + (date < 10 ? "0" + date : date));
                    new RequestMessage(NetworkInterface.REST_AIR_QUALITY_HISTORICAL, "POST", rootObject, handler).start();
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }
}
