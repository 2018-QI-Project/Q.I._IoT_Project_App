package qualcomminstitute.iot;

import android.widget.TextView;

public class Utility {
    public static void initView(TextView...views) {
        for(TextView view : views) {
            view.setText("");
            view.setError(null);
        }
    }
}
