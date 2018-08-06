package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static qualcomminstitute.iot.NetworkInterface.TOAST_CHECK_MAIL;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DUPLICATE_EMAIL;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;

public class SignUpActivity extends AppCompatActivity {
    private EditText viewEmail, viewPassword, viewRepeatPassword, viewFullName, viewAge;
    private RadioGroup viewGender;
    private Button viewSubmit;
    private TextView viewSignIn;
    private CheckBox viewRespiratory, viewCardiovascular;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL :
                    Utility.displayToastMessage(handler, SignUpActivity.this, NetworkInterface.TOAST_EXCEPTION);
                    break;
                case NetworkInterface.REQUEST_SUCCESS :
                    try {
                        // 응답 메세지 JSON 파싱
                        JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_CHECK_MAIL);
                                finish();
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "already existed":
                                        Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_DUPLICATE_EMAIL);
                                        break;
                                    default:
                                        Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_DEFAULT_FAILED);
                                        break;
                                }
                                break;
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_EXCEPTION);
                    }
                    finally {
                        progressDialog.dismiss();
                    }
            }
        }
    };
    private ProgressDialog progressDialog;

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.initView(viewEmail, viewPassword, viewRepeatPassword, viewFullName, viewAge);
        viewEmail.requestFocus();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // ProgressDialog 초기화
        progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");

        // View 변수
        viewEmail = findViewById(R.id.txtSignUpEmail);
        viewPassword = findViewById(R.id.txtSignUpPassword);
        viewRepeatPassword = findViewById(R.id.txtSignUpRepeatPassword);
        viewFullName = findViewById(R.id.txtSignUpFullName);
        viewAge = findViewById(R.id.txtSignUpAge);
        viewGender = findViewById(R.id.ragSignUpGender);
        viewRespiratory = findViewById(R.id.ckbRespiratoryDisease);
        viewCardiovascular = findViewById(R.id.ckbCardiovascularDisease);
        viewSubmit = findViewById(R.id.btnSingUpSubmit);
        viewSignIn = findViewById(R.id.txtSignUpSignIn);
        viewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utility.validateInputForm(SignUpActivity.this, viewEmail, viewPassword, viewFullName, viewAge) && Utility.validatePassword(viewPassword, viewRepeatPassword)) {
                    progressDialog.show();

                    final RadioButton gender = findViewById(viewGender.getCheckedRadioButtonId());
                    // POST 데이터 전송을 위한 자료구조
                    try {
                        JSONObject rootObject = new JSONObject();
                        rootObject.put(NetworkInterface.REQUEST_EMAIL, viewEmail.getText().toString());
                        rootObject.put(NetworkInterface.REQUEST_PASSWORD, viewPassword.getText().toString());
                        rootObject.put(NetworkInterface.REQUEST_FULL_NAME, viewFullName.getText().toString());
                        rootObject.put(NetworkInterface.REQUEST_AGE, Integer.parseInt(viewAge.getText().toString()));
                        rootObject.put(NetworkInterface.REQUEST_GENDER, gender.getText().toString().equals("Male") ? "M" : "F");
                        rootObject.put(NetworkInterface.REQUEST_BREATHE, viewRespiratory.isChecked() ? 1 : 0);
                        rootObject.put(NetworkInterface.REQUEST_HEART, viewCardiovascular.isChecked() ? 1 : 0);

                        new RequestMessage(NetworkInterface.REST_SIGN_UP, "POST", rootObject, handler).start();
                    }
                    catch(JSONException e) {
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_EXCEPTION);
                    }
                }
            }
        });
        viewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
