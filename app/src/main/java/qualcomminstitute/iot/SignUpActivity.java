package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
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
import android.widget.Toast;

import com.google.android.gms.common.oob.SignUp;

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
import java.util.LinkedHashMap;
import java.util.Map;

import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;

public class SignUpActivity extends AppCompatActivity {
    private final String TOAST_DUPLICATE_EMAIL = "Duplicate Email. Check Email.";

    private EditText viewEmail, viewPassword, viewRepeatPassword, viewFullName, viewAge;
    private RadioGroup viewGender;
    private Button viewSubmit;
    private TextView viewSignIn;
    private CheckBox viewRespiratory, viewCardiovascular;

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

        // Submit 버튼에 대한 클릭 이벤트
        viewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
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
                                Map<String,Object> params = new LinkedHashMap<>();
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("EMAIL"), viewEmail.getText().toString());
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("PASSWORD"), viewPassword.getText().toString());
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("FULL_NAME"), viewFullName.getText().toString());
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("AGE"), Integer.parseInt(viewAge.getText().toString()));
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("GENDER"), gender.getText().toString().equals("Male") ? "M" : "F");
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("BREATHE"), viewRespiratory.isChecked());
                                params.put(NetworkInterface.SIGN_UP_MESSAGE.get("HEART"), viewCardiovascular.isChecked());

                                // POST 데이터들을 UTF-8로 인코딩
                                StringBuilder postData = new StringBuilder();
                                for(Map.Entry<String,Object> param : params.entrySet()) {
                                    if(postData.length() != 0) postData.append('&');
                                    postData.append(URLEncoder.encode(param.getKey(), NetworkInterface.ENCODE));
                                    postData.append('=');
                                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), NetworkInterface.ENCODE));
                                }
                                byte[] postDataBytes = postData.toString().getBytes(NetworkInterface.ENCODE);

                                Log.d("POST", postData.toString());

                                // URL을 통한 서버와의 연결 설정
                                serverConnection = (HttpURLConnection)url.openConnection();
                                serverConnection.setRequestMethod("POST");
                                serverConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
                                JSONObject rootObject = new JSONObject(response.toString());

                                // Toast 메세지를 위한 Looper 준비
                                Looper.prepare();

                                if(rootObject.getString(NetworkInterface.SIGN_UP_MESSAGE.get("TYPE")).equals(NetworkInterface.SIGN_UP_MESSAGE.get("FAILED"))) {
                                    switch(rootObject.getString(NetworkInterface.SIGN_UP_MESSAGE.get("MESSAGE"))){
                                        case "already existed":
                                            progressDialog.dismiss();
                                            Toast.makeText(SignUpActivity.this, TOAST_DUPLICATE_EMAIL, Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                                else {
                                    progressDialog.dismiss();
                                    finish();
                                }
                            }
                            catch(MalformedURLException e) {
                                Log.e(this.getClass().getName(), "URL ERROR!");
                            }
                            catch(JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                            }
                            catch(IOException e) {
                                Log.e(this.getClass().getName(), "IO ERROR!");
                            }
                            finally {
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

    // View에 입력한 데이터가 양식에 맞는지 검사
    private boolean validate() {
        String email = viewEmail.getText().toString();
        String password = viewPassword.getText().toString();
        String repeatPassword = viewRepeatPassword.getText().toString();
        String name = viewFullName.getText().toString();
        String age = viewAge.getText().toString();

        if (!email.matches(InputFormCondition.EMAIL_CONDITION)) {
            viewEmail.setError(InputFormCondition.ERROR_EMAIL);
            viewEmail.requestFocus();
            return false;
        } else {
            viewEmail.setError(null);
        }

        if (!password.matches(InputFormCondition.PASSWORD_CONDITION)) {
            viewPassword.setError(InputFormCondition.ERROR_PASSWORD);
            viewPassword.requestFocus();
            return false;
        } else {
            viewPassword.setError(null);
        }

        if(!password.equals(repeatPassword)) {
            viewRepeatPassword.setError(InputFormCondition.ERROR_REPEAT_PASSWORD);
            viewRepeatPassword.requestFocus();
            return false;
        } else {
            viewRepeatPassword.setError(null);
        }

        if(!name.matches(InputFormCondition.FULL_NAME_CONDITION)) {
            viewFullName.setError(InputFormCondition.ERROR_FULL_NAME);
            viewFullName.requestFocus();
            return false;
        } else {
            viewFullName.setError(null);
        }

        if(!age.matches(InputFormCondition.AGE_CONDITION)) {
            viewAge.setError(InputFormCondition.ERROR_AGE);
            viewAge.requestFocus();
            return false;
        } else {
            viewAge.setError(null);
        }

        return true;
    }
}
