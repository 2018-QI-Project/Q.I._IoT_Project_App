package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import static qualcomminstitute.iot.NetworkInterface.TOAST_CHANGED_PASSWORD;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CHECK_MAIL;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_PASSWORD_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_REGISTER;
import static qualcomminstitute.iot.NetworkInterface.TOAST_TOKEN_FAILED;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText viewEmail;
    private Button viewSubmit;

    // Toast 메세지를 위한 Handler
    private Handler handler;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        handler = new Handler();

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

                    new Thread() {
                        @Override
                        public void run() {
                            HttpURLConnection serverConnection = null;
                            String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("RESET_PASSWORD");

                            try {
                                URL url = new URL(serverURL);

                                // POST 데이터 만들기
                                Map<String,Object> params = new LinkedHashMap<>();
                                params.put(NetworkInterface.RESET_PASSWORD_MESSAGE.get("EMAIL"), viewEmail.getText().toString());

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
                                serverConnection.setRequestMethod("PUT");
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

                                if(response.substring(0, 1).equals("{")) {
                                    // 응답 메세지 JSON 파싱
                                    JSONObject rootObject = new JSONObject(response.toString());

                                    if (rootObject.getString(NetworkInterface.RESET_PASSWORD_MESSAGE.get("TYPE")).equals(NetworkInterface.RESET_PASSWORD_MESSAGE.get("FAILED"))) {
                                        switch (rootObject.getString(NetworkInterface.RESET_PASSWORD_MESSAGE.get("MESSAGE"))) {
                                            case "Unregistered User":
                                                Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_REGISTER);
                                                finish();
                                                break;
                                            default:
                                                Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_DEFAULT_FAILED);
                                                break;
                                        }
                                    }
                                }
                                else {
                                    Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_CHECK_MAIL);
                                    finish();
                                }
                            }
                            catch(MalformedURLException e) {
                                Log.e(this.getClass().getName(), "URL ERROR!");
                                Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_EXCEPTION);
                            }
                            catch(JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_EXCEPTION);
                            }
                            catch(IOException e) {
                                Log.e(this.getClass().getName(), "IO ERROR!");
                                Utility.displayToastMessage(handler, ResetPasswordActivity.this, TOAST_EXCEPTION);
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
    }
}