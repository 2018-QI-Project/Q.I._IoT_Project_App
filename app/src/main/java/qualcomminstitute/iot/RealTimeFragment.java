package qualcomminstitute.iot;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pavlospt.roundedletterview.RoundedLetterView;

import java.util.Locale;

public class RealTimeFragment extends Fragment {
    private TextView viewRealTimeCO, viewRealTimeSO2, viewRealTimeNO2, viewRealTimeO3, viewRealTimePM25;
    private RoundedLetterView viewAqiCO, viewAqiSO2, viewAqiNO2, viewAqiO3, viewAqiPM25;
    private TextView viewRealTimeAirDate;
    private TextView viewRealTimeHeartRate, viewRealTimeRRInterval;

    private Handler handler;
    private Thread realTimeThread;
    private SharedPreferences preferences;
    private boolean flag;

    public RealTimeFragment(){}

    @Override
    public void onResume() {
        super.onResume();
        flag = true;
        if(realTimeThread.isInterrupted()) {
            realTimeThread.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        flag = false;
        realTimeThread.interrupt();
    }

    @Override
    public void onPause() {
        super.onPause();
        flag = false;
        realTimeThread.interrupt();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        flag = true;
        handler = new Handler();

        // Token 얻어오기
        preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);

        viewRealTimeCO = view.findViewById(R.id.txtRealTimeCO);
        viewRealTimeSO2 = view.findViewById(R.id.txtRealTimeSO2);
        viewRealTimeNO2 = view.findViewById(R.id.txtRealTimeNO2);
        viewRealTimeO3 = view.findViewById(R.id.txtRealTimeO3);
        viewRealTimePM25 = view.findViewById(R.id.txtReaTimePM25);
        viewRealTimeAirDate = view.findViewById(R.id.txtRealTimeAirDate);
        viewRealTimeHeartRate = view.findViewById(R.id.txtRealTimeHeartRate);
        viewRealTimeRRInterval = view.findViewById(R.id.txtRealTimeRRInterval);
        viewAqiCO = view.findViewById(R.id.laRealTimeCO);
        viewAqiNO2 = view.findViewById(R.id.laRealTimeNO2);
        viewAqiO3 = view.findViewById(R.id.laRealTimeO3);
        viewAqiSO2 = view.findViewById(R.id.laRealTimeSO2);
        viewAqiPM25 = view.findViewById(R.id.laRealTimePM25);

        realTimeThread = new Thread() {
            @Override
            public void run() {
                while(flag){
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    handler.post(new Thread(){
                        @Override
                        public void run() {
                            try {
                                if (preferences.getString(NetworkInterface.SENSOR_DATA[0], null) != null) {
                                    viewRealTimeCO.setText(String.format(Locale.US, "%.2f", Double.parseDouble(preferences.getString(NetworkInterface.SENSOR_DATA[0], null))));
                                } else {
                                    viewRealTimeCO.setText(preferences.getString(NetworkInterface.SENSOR_DATA[0], null));
                                }
                                if (preferences.getString(NetworkInterface.SENSOR_DATA[1], null) != null) {
                                    viewRealTimeSO2.setText(String.format(Locale.US, "%.2f", Double.parseDouble(preferences.getString(NetworkInterface.SENSOR_DATA[1], null))));
                                } else {
                                    viewRealTimeSO2.setText(preferences.getString(NetworkInterface.SENSOR_DATA[1], null));
                                }
                                if (preferences.getString(NetworkInterface.SENSOR_DATA[2], null) != null) {
                                    viewRealTimeNO2.setText(String.format(Locale.US, "%.2f", Double.parseDouble(preferences.getString(NetworkInterface.SENSOR_DATA[2], null))));
                                } else {
                                    viewRealTimeNO2.setText(preferences.getString(NetworkInterface.SENSOR_DATA[2], null));
                                }
                                if (preferences.getString(NetworkInterface.SENSOR_DATA[3], null) != null) {
                                    viewRealTimeO3.setText(String.format(Locale.US, "%.2f", Double.parseDouble(preferences.getString(NetworkInterface.SENSOR_DATA[3], null))));
                                } else {
                                    viewRealTimeO3.setText(preferences.getString(NetworkInterface.SENSOR_DATA[3], null));
                                }
                                if (preferences.getString(NetworkInterface.SENSOR_DATA[4], null) != null) {
                                    viewRealTimePM25.setText(String.format(Locale.US, "%.2f", Double.parseDouble(preferences.getString(NetworkInterface.SENSOR_DATA[4], null))));
                                } else {
                                    viewRealTimePM25.setText(preferences.getString(NetworkInterface.SENSOR_DATA[4], null));
                                }
                                viewRealTimeHeartRate.setText(String.format(Locale.US, "%d", preferences.getInt(PreferenceName.preferenceRealHeart, 0)));
                                viewRealTimeRRInterval.setText(String.format(Locale.US, "%d", preferences.getInt(PreferenceName.preferenceRealRR, 0)));
                                

                                switch ((preferences.getInt(NetworkInterface.SENSOR_AQI_DATA[0], 500) - 1) / 50) {
                                    case 0:
                                        viewAqiCO.setBackgroundColor(getResources().getColor(R.color.good));
                                        break;
                                    case 1:
                                        viewAqiCO.setBackgroundColor(getResources().getColor(R.color.moderate));
                                        break;
                                    case 2:
                                        viewAqiCO.setBackgroundColor(getResources().getColor(R.color.unhealthy_for_sensitive_groups));
                                        break;
                                    case 3:
                                        viewAqiCO.setBackgroundColor(getResources().getColor(R.color.unhealthy));
                                        break;
                                    case 4:
                                    case 5:
                                        viewAqiCO.setBackgroundColor(getResources().getColor(R.color.very_unhealthy));
                                        break;
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                        viewAqiCO.setBackgroundColor(getResources().getColor(R.color.hazardous));
                                        break;
                                }
                                switch ((preferences.getInt(NetworkInterface.SENSOR_AQI_DATA[1], 500) - 1) / 50) {
                                    case 0:
                                        viewAqiNO2.setBackgroundColor(getResources().getColor(R.color.good));
                                        break;
                                    case 1:
                                        viewAqiNO2.setBackgroundColor(getResources().getColor(R.color.moderate));
                                        break;
                                    case 2:
                                        viewAqiNO2.setBackgroundColor(getResources().getColor(R.color.unhealthy_for_sensitive_groups));
                                        break;
                                    case 3:
                                        viewAqiNO2.setBackgroundColor(getResources().getColor(R.color.unhealthy));
                                        break;
                                    case 4:
                                    case 5:
                                        viewAqiNO2.setBackgroundColor(getResources().getColor(R.color.very_unhealthy));
                                        break;
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                        viewAqiNO2.setBackgroundColor(getResources().getColor(R.color.hazardous));
                                        break;
                                }
                                switch ((preferences.getInt(NetworkInterface.SENSOR_AQI_DATA[2], 500) - 1) / 50) {
                                    case 0:
                                        viewAqiSO2.setBackgroundColor(getResources().getColor(R.color.good));
                                        break;
                                    case 1:
                                        viewAqiSO2.setBackgroundColor(getResources().getColor(R.color.moderate));
                                        break;
                                    case 2:
                                        viewAqiSO2.setBackgroundColor(getResources().getColor(R.color.unhealthy_for_sensitive_groups));
                                        break;
                                    case 3:
                                        viewAqiSO2.setBackgroundColor(getResources().getColor(R.color.unhealthy));
                                        break;
                                    case 4:
                                    case 5:
                                        viewAqiSO2.setBackgroundColor(getResources().getColor(R.color.very_unhealthy));
                                        break;
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                        viewAqiSO2.setBackgroundColor(getResources().getColor(R.color.hazardous));
                                        break;
                                }
                                switch ((preferences.getInt(NetworkInterface.SENSOR_AQI_DATA[3], 500) - 1) / 50) {
                                    case 0:
                                        viewAqiO3.setBackgroundColor(getResources().getColor(R.color.good));
                                        break;
                                    case 1:
                                        viewAqiO3.setBackgroundColor(getResources().getColor(R.color.moderate));
                                        break;
                                    case 2:
                                        viewAqiO3.setBackgroundColor(getResources().getColor(R.color.unhealthy_for_sensitive_groups));
                                        break;
                                    case 3:
                                        viewAqiO3.setBackgroundColor(getResources().getColor(R.color.unhealthy));
                                        break;
                                    case 4:
                                    case 5:
                                        viewAqiO3.setBackgroundColor(getResources().getColor(R.color.very_unhealthy));
                                        break;
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                        viewAqiO3.setBackgroundColor(getResources().getColor(R.color.hazardous));
                                        break;
                                }
                                switch ((preferences.getInt(NetworkInterface.SENSOR_AQI_DATA[4], 500) - 1) / 50) {
                                    case 0:
                                        viewAqiPM25.setBackgroundColor(getResources().getColor(R.color.good));
                                        break;
                                    case 1:
                                        viewAqiPM25.setBackgroundColor(getResources().getColor(R.color.moderate));
                                        break;
                                    case 2:
                                        viewAqiPM25.setBackgroundColor(getResources().getColor(R.color.unhealthy_for_sensitive_groups));
                                        break;
                                    case 3:
                                        viewAqiPM25.setBackgroundColor(getResources().getColor(R.color.unhealthy));
                                        break;
                                    case 4:
                                    case 5:
                                        viewAqiPM25.setBackgroundColor(getResources().getColor(R.color.very_unhealthy));
                                        break;
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                        viewAqiPM25.setBackgroundColor(getResources().getColor(R.color.hazardous));
                                        break;
                                }

                                viewAqiCO.invalidate();
                                viewAqiNO2.invalidate();
                                viewAqiSO2.invalidate();
                                viewAqiO3.invalidate();
                                viewAqiPM25.invalidate();

                                viewRealTimeAirDate.setText(Utility.convertUnixTime(preferences.getLong(PreferenceName.preferenceDate, System.currentTimeMillis() / 1000)));
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };

        realTimeThread.start();

        return view;
    }
}