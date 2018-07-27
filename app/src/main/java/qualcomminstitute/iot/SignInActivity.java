package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_SIGN_IN_PASSWORD_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_SIGN_IN_REGISTER;
import static qualcomminstitute.iot.NetworkInterface.TOAST_SIGN_IN_VERIFY;

public class SignInActivity extends AppCompatActivity {
    private EditText viewEmail, viewPassword;
    private Button viewSignIn, viewSignUp;
    private TextView viewForgotPassword;

    private Handler handler;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        handler = new Handler();

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
                if(Utility.validateInputForm(SignInActivity.this, viewEmail, viewPassword)) {
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
                                params.put(NetworkInterface.SIGN_IN_MESSAGE.get("EMAIL"), viewEmail.getText().toString());
                                params.put(NetworkInterface.SIGN_IN_MESSAGE.get("PASSWORD"), viewPassword.getText().toString());

                                // POST 데이터들을 UTF-8로 인코딩
                                StringBuilder postData = new StringBuilder();
                                for(Map.Entry<String,Object> param : params.entrySet()) {
                                    if(postData.length() != 0) postData.append('&');
                                    postData.append(URLEncoder.encode(param.getKey(), NetworkInterface.ENCODE));
                                    postData.append('=');
                                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), NetworkInterface.ENCODE));
                                }
                                byte[] postDataBytes = postData.toString().getBytes(NetworkInterface.ENCODE);

                                // URL을 통한 서버와의 연결 설정
                                serverConnection = (HttpURLConnection)url.openConnection();
                                serverConnection.setRequestMethod("POST");
                                serverConnection.setRequestProperty("Content-Type", NetworkInterface.POST_HEADER);
                                serverConnection.setRequestProperty(NetworkInterface.SIGN_IN_MESSAGE.get("CLIENT_KEY"), NetworkInterface.SIGN_IN_MESSAGE.get("CLIENT_VALUE"));
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

                                // 응답 메세지 JSON 파싱
                                JSONObject rootObject = new JSONObject(response.toString());

                                if(rootObject.has(NetworkInterface.SIGN_IN_MESSAGE.get("SUCCESS"))) {
                                    SharedPreferences token = getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                    SharedPreferences.Editor tokenEditor = token.edit();

                                    Utility.displayToastMessage(handler, SignInActivity.this, "Success !");

                                    tokenEditor.putString(PreferenceName.preferenceToken, rootObject.getString(NetworkInterface.SIGN_IN_MESSAGE.get("SUCCESS")));
                                    tokenEditor.apply();

                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    switch(rootObject.getString(NetworkInterface.SIGN_IN_MESSAGE.get("MESSAGE"))) {
                                        case "Unauthorized User":
                                            Utility.displayToastMessage(handler, SignInActivity.this, TOAST_SIGN_IN_VERIFY);
                                            break;
                                        case "Wrong Password " :
                                            Utility.displayToastMessage(handler, SignInActivity.this, TOAST_SIGN_IN_PASSWORD_FAILED);
                                            break;
                                        case "Unregistered User":
                                            Utility.displayToastMessage(handler, SignInActivity.this, TOAST_SIGN_IN_REGISTER);
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, SignInActivity.this, TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                }
                            }
                            catch(MalformedURLException e) {
                                Log.e(this.getClass().getName(), "URL ERROR!");
                                Utility.displayToastMessage(handler, SignInActivity.this, TOAST_EXCEPTION);
                            }
                            catch(JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                Utility.displayToastMessage(handler, SignInActivity.this, TOAST_EXCEPTION);
                            }
                            catch(IOException e) {
                                Log.e(this.getClass().getName(), "IO ERROR!");
                                Utility.displayToastMessage(handler, SignInActivity.this, TOAST_EXCEPTION);
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
        viewEmail.requestFocus();
    }
}
