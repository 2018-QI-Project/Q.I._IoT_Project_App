package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class SignInActivity extends AppCompatActivity {
    private final String TOAST_SIGN_IN_VERIFY = "Email isn't Valid. Please Check Verify Email.";
    private final String TOAST_SIGN_IN_PASSWORD_FAILED = "Sign In Failed. Please Check Password.";
    private final String TOAST_SIGN_IN_REGISTER = "Sign In Failed. Please Check Email.";
    private final String TOAST_SIGN_IN_DEFAULT_FAILED = "Sign In Failed. Please Contact Server Manager.";

    private EditText viewEmail, viewPassword;
    private Button viewSignIn, viewSignUp;
    private TextView viewForgotPassword;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        progressDialog = new ProgressDialog(SignInActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sign In...");

        viewEmail = findViewById(R.id.txtSignInEmail);
        viewPassword = findViewById(R.id.txtSignInPassword);
        viewSignIn = findViewById(R.id.btnSignIn);
        viewSignUp = findViewById(R.id.btnSignUp);
        viewForgotPassword = findViewById(R.id.txtSignInForgotPassword);

        viewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    // ProgressDialog 생성
                    progressDialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            HttpURLConnection serverConnection = null;
                            String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("SIGN_IN");
                            try {
                                URL url = new URL(serverURL);
                                // POST 데이터 전송을 위한 자료구조
                                Map<String,Object> params = new LinkedHashMap<>();
                                params.put("email", viewEmail.getText().toString());
                                params.put("password", viewPassword.getText().toString());

                                // POST 데이터들을 UTF-8로 인코딩
                                StringBuilder postData = new StringBuilder();
                                for(Map.Entry<String,Object> param : params.entrySet()) {
                                    if(postData.length() != 0) postData.append('&');
                                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                                    postData.append('=');
                                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                                }
                                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                                // URL을 통한 서버와의 연결 설정
                                serverConnection = (HttpURLConnection)url.openConnection();
                                serverConnection.setRequestMethod("POST");
                                serverConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                serverConnection.setRequestProperty("client", "app");
                                serverConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                                // POST 데이터를 설정
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

                                // ProgressDialog 제거
                                new android.os.Handler().post(
                                        new Runnable() {
                                            public void run() {
                                                progressDialog.dismiss();
                                                finish();
                                            }
                                        });

                                // 응답 메세지 JSON 파싱
                                JSONObject rootObject = new JSONObject(response.toString());

                                if(rootObject.has("token_app")) {
                                    SharedPreferences token = getSharedPreferences("Token", MODE_PRIVATE);
                                    SharedPreferences.Editor tokenEditor = token.edit();
                                    Toast.makeText(SignInActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                    tokenEditor.putString("auth", rootObject.getString("token_app"));
                                    tokenEditor.apply();

                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    switch(rootObject.getString("error")) {
                                        case "Unauthorized User":
                                            Toast.makeText(SignInActivity.this, TOAST_SIGN_IN_VERIFY, Toast.LENGTH_SHORT).show();
                                            break;
                                        case "Wrong Password " :
                                            Toast.makeText(SignInActivity.this, TOAST_SIGN_IN_PASSWORD_FAILED, Toast.LENGTH_SHORT).show();
                                            break;
                                        case "Unregistered User":
                                            Toast.makeText(SignInActivity.this, TOAST_SIGN_IN_REGISTER, Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            Toast.makeText(SignInActivity.this, TOAST_SIGN_IN_DEFAULT_FAILED, Toast.LENGTH_SHORT).show();
                                            break;
                                    }
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
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
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
    }

    private boolean validate() {
        String email = viewEmail.getText().toString();
        String password = viewPassword.getText().toString();

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

        return true;
    }
}
