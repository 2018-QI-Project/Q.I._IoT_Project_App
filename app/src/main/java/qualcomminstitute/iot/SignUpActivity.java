package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.os.Bundle;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;

public class SignUpActivity extends AppCompatActivity {
    private EditText viewEmail, viewPassword, viewRepeatPassword, viewFullName, viewAge;
    private RadioGroup viewGender;
    private Button viewSubmit;
    private TextView viewSignIn;
    private CheckBox viewRespiratory, viewCardiovascular;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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
                    final RadioButton gender = findViewById(viewGender.getCheckedRadioButtonId());

                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Creating Account...");

                    new Thread() {
                        @Override
                        public void run() {
                            HttpURLConnection serverConnection = null;
                            String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("SIGN_UP");

                            try {
                                URL url = new URL(serverURL);

                                // POST 데이터 만들기
                                Map<String,Object> params = new LinkedHashMap<>();
                                params.put("email", viewEmail.getText().toString());
                                params.put("password", viewPassword.getText().toString());
                                params.put("name", viewFullName.getText().toString());
                                params.put("age", Integer.parseInt(viewAge.getText().toString()));
                                params.put("gender", gender.getText().toString().equals("Male") ? "M" : "F");
                                params.put("respiratoryDisease", viewRespiratory.isChecked());
                                params.put("cardiovascularDisease", viewCardiovascular.isChecked());

                                // POST 데이터들을 UTF-8로 인코딩
                                StringBuilder postData = new StringBuilder();
                                for(Map.Entry<String,Object> param : params.entrySet()) {
                                    if(postData.length() != 0) postData.append('&');
                                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                                    postData.append('=');
                                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                                }
                                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                                Log.d("POST", postData.toString());

                                // URL을 통한 서버와의 연결 설정
                                serverConnection = (HttpURLConnection)url.openConnection();
                                serverConnection.setRequestMethod("POST");
                                serverConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                serverConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                                // 서버의 입력 설정 및 데이터 추가
                                serverConnection.setDoOutput(true);
                                serverConnection.getOutputStream().write(postDataBytes);

                                // POST 메세지 전송용 로그
                                Log.d("SignUpActivity", Integer.toString(serverConnection.getResponseCode()));
                            }
                            catch(MalformedURLException e) {
                                Log.e(this.getClass().getName(), "URL ERROR!");
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

                    progressDialog.show();
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    // On complete call either onSignupSuccess or onSignupFailed
                                    // depending on success
                                    progressDialog.dismiss();
                                    finish();
                                }
                            }, 3000);
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

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewEmail.setError("Enter a valid email address");
            viewEmail.requestFocus();
            return false;
        } else {
            viewEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 12) {
            viewPassword.setError("Password must between 4 and 12 alphanumeric characters");
            viewPassword.requestFocus();
            return false;
        } else {
            viewPassword.setError(null);
        }

        if(!password.equals(repeatPassword)) {
            viewRepeatPassword.setError("Password isn't match");
            viewRepeatPassword.requestFocus();
            return false;
        } else {
            viewRepeatPassword.setError(null);
        }

        if(!name.matches("^[[a-zA-Z]*\\s?]*[a-zA-Z]+$")) {
            viewFullName.setError("Name is incorrect");
            viewFullName.requestFocus();
            return false;
        } else {
            viewFullName.setError(null);
        }

        if(age.isEmpty() || !age.matches("^[0-9]*$")) {
            viewAge.setError("Age is incorrect");
            viewAge.requestFocus();
            return false;
        } else {
            viewAge.setError(null);
        }

        return true;
    }
}
