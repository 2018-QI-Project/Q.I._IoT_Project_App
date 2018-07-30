package qualcomminstitute.iot;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

import static android.content.Context.MODE_PRIVATE;
import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CLIENT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_ID_CANCELLATION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_PASSWORD_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_TOKEN_FAILED;

public class IDCancelFragment extends Fragment {
    private EditText viewPassword, viewPasswordRepeat;
    private CheckBox viewAgree;
    private Button viewSubmit;

    // Toast 메세지를 위한 Handler
    private Handler handler;
    private ProgressDialog progressDialog;

    private String strToken;

    public IDCancelFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_id_cancellation, container, false);

        handler = new Handler();

        // Token 얻어오기
        SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, "");

        // Progress Dialog 초기화
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Erasing...");

        // View 가져오기
        viewPassword = view.findViewById(R.id.txtIDCancellationPassword);
        viewPasswordRepeat = view.findViewById(R.id.txtIDCancellationPasswordRepeat);
        viewAgree = view.findViewById(R.id.ckbIDCancellationAgree);
        viewSubmit = view.findViewById(R.id.btnIDCancellationSubmit);
        viewSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Utility.validateInputForm(getActivity(), viewPassword, viewPasswordRepeat) && Utility.validatePassword(viewPassword, viewPasswordRepeat)) {
                        if (viewAgree.isChecked()) {
                            viewAgree.setError(null);
                            // ProgressDialog 생성
                            progressDialog.show();

                            new Thread() {
                                @Override
                                public void run() {
                                    HttpURLConnection serverConnection = null;
                                    String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("ID_CANCELLATION");
                                    try {
                                        URL url = new URL(serverURL);
                                        // POST 데이터 전송을 위한 자료구조
                                        Map<String, Object> params = new LinkedHashMap<>();
                                        params.put(NetworkInterface.ID_CANCELLATION_MESSAGE.get("PASSWORD"), viewPassword.getText().toString());

                                        // POST 데이터들을 UTF-8로 인코딩
                                        StringBuilder postData = new StringBuilder();
                                        for (Map.Entry<String, Object> param : params.entrySet()) {
                                            if (postData.length() != 0) postData.append('&');
                                            postData.append(URLEncoder.encode(param.getKey(), NetworkInterface.ENCODE));
                                            postData.append('=');
                                            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), NetworkInterface.ENCODE));
                                        }
                                        byte[] postDataBytes = postData.toString().getBytes(NetworkInterface.ENCODE);

                                        // URL을 통한 서버와의 연결 설정
                                        serverConnection = (HttpURLConnection) url.openConnection();
                                        serverConnection.setRequestMethod("DELETE");
                                        serverConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                        serverConnection.setRequestProperty(NetworkInterface.ID_CANCELLATION_MESSAGE.get("CLIENT_KEY"), NetworkInterface.ID_CANCELLATION_MESSAGE.get("CLIENT_VALUE"));
                                        serverConnection.setRequestProperty(NetworkInterface.ID_CANCELLATION_MESSAGE.get("TOKEN"), strToken);
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

                                        if (response.toString().equals(NetworkInterface.ID_CANCELLATION_MESSAGE.get("SUCCESS"))) {
                                            Utility.displayToastMessage(handler, getActivity(), TOAST_ID_CANCELLATION);
                                            getActivity().finish();
                                        } else {
                                            // 응답 메세지 JSON 파싱
                                            JSONObject rootObject = new JSONObject(response.toString());

                                            switch (rootObject.getString(NetworkInterface.ID_CANCELLATION_MESSAGE.get("MESSAGE"))) {
                                                case "invalid client type":
                                                    Utility.displayToastMessage(handler, getActivity(), TOAST_CLIENT_FAILED);
                                                    break;
                                                case "wrong password":
                                                    Utility.displayToastMessage(handler, getActivity(), TOAST_PASSWORD_FAILED);
                                                    break;
                                                case "invalid token":
                                                    Utility.displayToastMessage(handler, getActivity(), TOAST_TOKEN_FAILED);
                                                    getActivity().finish();
                                                    break;
                                                default:
                                                    Utility.displayToastMessage(handler, getActivity(), TOAST_DEFAULT_FAILED);
                                                    break;
                                            }
                                        }
                                    } catch (MalformedURLException e) {
                                        Log.e(this.getClass().getName(), "URL ERROR!");
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                                    } catch (JSONException e) {
                                        Log.e(this.getClass().getName(), "JSON ERROR!");
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                                    } catch (IOException e) {
                                        Log.e(this.getClass().getName(), "IO ERROR!");
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                                    } finally {
                                        progressDialog.dismiss();
                                        if (serverConnection != null) {
                                            serverConnection.disconnect();
                                        }
                                    }
                                }
                            }.start();
                        }
                        else {
                            viewAgree.setError(InputFormCondition.ERROR_AGREE);
                            viewAgree.requestFocus();
                        }
                    }
                }
            });

        return view;
    }
}