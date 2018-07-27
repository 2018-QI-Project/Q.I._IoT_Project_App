package qualcomminstitute.iot;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

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
}