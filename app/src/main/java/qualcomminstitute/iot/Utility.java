package qualcomminstitute.iot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String[] parseCSVFile(String fileData) {
        return fileData.split("\n");
    }

    public static String[] parseCSVString(String strData) {
        return strData.split(",");
    }
}