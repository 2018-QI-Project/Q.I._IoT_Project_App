package qualcomminstitute.iot;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
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
}