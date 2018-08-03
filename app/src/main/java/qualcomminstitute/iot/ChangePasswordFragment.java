package qualcomminstitute.iot;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.content.Context.MODE_PRIVATE;
import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CHANGED_PASSWORD;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CLIENT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_PASSWORD_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_TOKEN_FAILED;

public class ChangePasswordFragment extends Fragment {
    private EditText viewCurrentPassword, viewNewPassword, viewRepeatNewPassword;
    private Button viewSubmit;

    // Toast 메세지를 위한 Handler
    private Handler handler;
    private ProgressDialog progressDialog;

    // Token 변수
    private String strToken;

    public ChangePasswordFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        handler = new Handler();

        // Token 얻어오기
        SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, "");

        // Progress Dialog 초기화
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Changing...");

        // View 가져오기
        viewCurrentPassword = view.findViewById(R.id.txtChangePasswordCurrentPassword);
        viewNewPassword = view.findViewById(R.id.txtChangePasswordNewPassword);
        viewRepeatNewPassword = view.findViewById(R.id.txtChangePasswordNewPasswordRepeat);
        viewSubmit = view.findViewById(R.id.btnChangePasswordSubmit);
        viewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utility.validateInputForm(getActivity(), viewCurrentPassword, viewNewPassword) && Utility.validatePassword(viewNewPassword, viewRepeatNewPassword)) {
                    // ProgressDialog 생성
                    progressDialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            HttpURLConnection serverConnection = null;
                            String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("CHANGE_PASSWORD");
                            try {
                                URL url = new URL(serverURL);
                                // POST 데이터 전송을 위한 자료구조
                                JSONObject rootObject = new JSONObject();
                                rootObject.put(NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("PASSWORD"), viewCurrentPassword.getText().toString());
                                rootObject.put(NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("NEW_PASSWORD"), viewNewPassword.getText().toString());
                                rootObject.put(NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("CLIENT_KEY"), NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("CLIENT_VALUE"));
                                rootObject.put(NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("TOKEN"), strToken);

                                byte[] postDataBytes = rootObject.toString().getBytes(NetworkInterface.ENCODE);

                                Log.d("POST", rootObject.toString());

                                // URL을 통한 서버와의 연결 설정
                                serverConnection = (HttpURLConnection)url.openConnection();
                                serverConnection.setRequestMethod("PUT");
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

                                if(returnObject.getString("type").equals(NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("SUCCESS"))) {
                                    Utility.displayToastMessage(handler, getActivity(), TOAST_CHANGED_PASSWORD);
                                    handler.post(new Thread(){
                                        @Override
                                        public void run() {
                                            viewCurrentPassword.setText("");
                                            viewNewPassword.setText("");
                                            viewRepeatNewPassword.setText("");
                                        }
                                    });
                                }
                                else {
                                    switch(returnObject.getString(NetworkInterface.CHANGE_PASSWORD_MESSAGE.get("MESSAGE"))) {
                                        case "invalid client type":
                                            Utility.displayToastMessage(handler, getActivity(), TOAST_CLIENT_FAILED);
                                            break;
                                        case "wrong password":
                                            Utility.displayToastMessage(handler, getActivity(), TOAST_PASSWORD_FAILED);
                                            break;
                                        case "invalid tokenApp":
                                            Utility.displayToastMessage(handler, getActivity(), TOAST_TOKEN_FAILED);
                                            getActivity().finish();
                                            break;
                                        default:
                                            Utility.displayToastMessage(handler, getActivity(), TOAST_DEFAULT_FAILED);
                                            break;
                                    }
                                }
                            }
                            catch(MalformedURLException e) {
                                Log.e(this.getClass().getName(), "URL ERROR!");
                                Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                            }
                            catch(JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                            }
                            catch(IOException e) {
                                Log.e(this.getClass().getName(), "IO ERROR!");
                                Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
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

        return view;
    }
}
