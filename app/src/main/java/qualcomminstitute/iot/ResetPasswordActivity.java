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
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import static qualcomminstitute.iot.NetworkInterface.TOAST_CHECK_MAIL;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_REGISTER;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText viewEmail;
    private Button viewSubmit;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL :
                    Utility.displayToastMessage(handler, ResetPasswordActivity.this, NetworkInterface.TOAST_EXCEPTION);
                    break;
                case NetworkInterface.REQUEST_SUCCESS :
                    try {
                        // 응답 메세지 JSON 파싱
                        JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_CHECK_MAIL);
                                finish();
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "Unregistered User":
                                        Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_REGISTER);
                                        finish();
                                        break;
                                    default:
                                        Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_DEFAULT_FAILED);
                                        break;
                                }
                                break;
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_EXCEPTION);
                    }
                    finally {
                        progressDialog.dismiss();
                    }
            }
        }
    };
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // ProgressDialog 초기화
        progressDialog = new ProgressDialog(ResetPasswordActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Checking...");

        viewEmail = findViewById(R.id.txtResetPasswordEmail);
        viewSubmit = findViewById(R.id.btnResetPasswordSubmit);
        viewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utility.validateInputForm(ResetPasswordActivity.this, viewEmail)) {
                    progressDialog.show();
                    // POST 데이터 전송을 위한 자료구조
                    try {
                        // POST 데이터 만들기
                        JSONObject rootObject = new JSONObject();
                        rootObject.put(NetworkInterface.REQUEST_EMAIL, viewEmail.getText().toString());

                        new RequestMessage(NetworkInterface.REST_RESET_PASSWORD, "PUT", rootObject, handler).start();
                    }
                    catch(JSONException e) {
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_EXCEPTION);
                    }
                }
            }
        });
    }
}