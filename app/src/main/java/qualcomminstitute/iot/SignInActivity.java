package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static qualcomminstitute.iot.NetworkInterface.TOAST_CLIENT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_PASSWORD_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_REGISTER;
import static qualcomminstitute.iot.NetworkInterface.TOAST_VERIFY;

public class SignInActivity extends AppCompatActivity {
    private EditText viewEmail, viewPassword;
    private Button viewSignIn, viewSignUp;
    private TextView viewForgotPassword;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL :
                    Utility.displayToastMessage(handler, SignInActivity.this, NetworkInterface.TOAST_EXCEPTION);
                    progressDialog.dismiss();
                    break;
                case NetworkInterface.REQUEST_SUCCESS :
                    try {
                        // 응답 메세지 JSON 파싱
                        JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                SharedPreferences data = getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                SharedPreferences.Editor dataEditor = data.edit();

                                dataEditor.putString(PreferenceName.preferenceToken, returnObject.getString(NetworkInterface.MESSAGE_TOKEN));
                                dataEditor.apply();

                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "not valid client, choose app or web":
                                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_CLIENT_FAILED);
                                        break;
                                    case "Unauthorized User":
                                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_VERIFY);
                                        break;
                                    case "Wrong password":
                                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_PASSWORD_FAILED);
                                        break;
                                    case "Unregistered User":
                                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_REGISTER);
                                        break;
                                    default:
                                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_DEFAULT_FAILED);
                                        break;
                                }
                                break;
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_EXCEPTION);
                    }
                    finally {
                        progressDialog.dismiss();
                    }
            }
        }
    };
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Progress Dialog 초기화
        progressDialog = new ProgressDialog(SignInActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sign In...");

        // View 가져오기
        viewEmail = findViewById(R.id.txtSignInEmail);
        viewPassword = findViewById(R.id.txtSignInPassword);
        viewSignIn = findViewById(R.id.btnSignIn);
        viewSignUp = findViewById(R.id.btnSignUp);
        viewForgotPassword = findViewById(R.id.txtSignInForgotPassword);
        viewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utility.validateInputForm(SignInActivity.this, viewEmail, viewPassword)) {
                    // ProgressDialog 생성
                    progressDialog.show();
                    // POST 데이터 전송을 위한 자료구조
                    try {
                        JSONObject rootObject = new JSONObject();
                        rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                        rootObject.put(NetworkInterface.REQUEST_EMAIL, viewEmail.getText().toString());
                        rootObject.put(NetworkInterface.REQUEST_PASSWORD, viewPassword.getText().toString());

                        new RequestMessage(NetworkInterface.REST_SIGN_IN, "POST", rootObject, handler).start();
                    }
                    catch(JSONException e) {
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, SignInActivity.this, TOAST_EXCEPTION);
                    }
                }
            }
        });
        viewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        viewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.initView(viewEmail, viewPassword);
        viewEmail.requestFocus();
    }
}
