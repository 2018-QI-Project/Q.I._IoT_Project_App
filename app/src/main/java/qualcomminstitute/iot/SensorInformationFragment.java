package qualcomminstitute.iot;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SensorInformationFragment extends Fragment {
    private final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public SensorInformationFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_information, container, false);



        return view;
    }
}