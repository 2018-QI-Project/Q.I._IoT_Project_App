package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;
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

    // Toast 메세지를 위한 Handler
    private Handler handler;
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

        handler = new Handler();

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

                    new Thread() {
                        @Override
                        public void run() {
                            HttpURLConnection serverConnection = null;
                            String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("SIGN_UP");

                            try {
                                URL url = new URL(serverURL);

                                // POST 데이터 만들기
                                JSONObject rootObject = new JSONObject();
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("EMAIL"), viewEmail.getText().toString());
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("PASSWORD"), viewPassword.getText().toString());
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("FULL_NAME"), viewFullName.getText().toString());
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("AGE"), Integer.parseInt(viewAge.getText().toString()));
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("GENDER"), gender.getText().toString().equals("Male") ? "M" : "F");
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("BREATHE"), viewRespiratory.isChecked() ? 1 : 0);
                                rootObject.put(NetworkInterface.SIGN_UP_MESSAGE.get("HEART"), viewCardiovascular.isChecked() ? 1 : 0);

                                byte[] postDataBytes = rootObject.toString().getBytes(NetworkInterface.ENCODE);

                                // URL을 통한 서버와의 연결 설정
                                serverConnection = (HttpURLConnection)url.openConnection();
                                serverConnection.setRequestMethod("POST");
                                serverConnection.setRequestProperty("Content-Type", NetworkInterface.JSON_HEADER);
                                serverConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                                // 서버의 입력 설정 및 데이터 추가
                                serverConnection.setDoOutput(true);
                                serverConnection.getOutputStream().write(postDataBytes);

                                // 요청 결과
                                InputStream is = serverConnection.getInputStream();
                                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                String readLine;
                                StringBuilder response = new StringBuilder();
                                while ((readLine = br.readLine()) != null) {
                                    response.append(readLine);
                                }
                                br.close();

                                // 응답 메세지 JSON 파싱
                                JSONObject returnObject = new JSONObject(response.toString());

                                if (returnObject.getString(NetworkInterface.SIGN_UP_MESSAGE.get("TYPE")).equals(NetworkInterface.SIGN_UP_MESSAGE.get("FAILED"))) {
                                    switch (returnObject.getString(NetworkInterface.SIGN_UP_MESSAGE.get("MESSAGE"))) {
                                        case "already existed":
                                            Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_DUPLICATE_EMAIL);
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                }
                                else {
                                    Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_CHECK_MAIL);
                                    finish();
                                }
                            }
                            catch(MalformedURLException e) {
                                Log.e(this.getClass().getName(), "URL ERROR!");
                                Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_EXCEPTION);
                            }
                            catch(JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_EXCEPTION);
                            }
                            catch(IOException e) {
                                Log.e(this.getClass().getName(), "IO ERROR!");
                                Utility.displayToastMessage(handler, SignUpActivity.this, TOAST_EXCEPTION);
                            }
                            finally {
                                progressDialog.dismiss();
                                if(serverConnection != null) {
                                    serverConnection.disconnect();
                                }
                            }
                        }
                    }.start();
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
