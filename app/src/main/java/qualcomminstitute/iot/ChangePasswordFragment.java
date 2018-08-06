package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import static android.content.Context.MODE_PRIVATE;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CHANGED_PASSWORD;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CLIENT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_PASSWORD_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_TOKEN_FAILED;

public class ChangePasswordFragment extends Fragment {
    private EditText viewCurrentPassword, viewNewPassword, viewRepeatNewPassword;
    private Button viewSubmit;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL :
                    Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                    break;
                case NetworkInterface.REQUEST_SUCCESS :
                    try {
                        // 응답 메세지 JSON 파싱
                        JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                Utility.displayToastMessage(handler, getActivity(), TOAST_CHANGED_PASSWORD);
                                handler.post(new Thread(){
                                    @Override
                                    public void run() {
                                        viewCurrentPassword.setText("");
                                        viewNewPassword.setText("");
                                        viewRepeatNewPassword.setText("");
                                    }
                                });
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_CLIENT_FAILED);
                                        break;
                                    case "wrong password":
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_PASSWORD_FAILED);
                                        break;
                                    case "invalid tokenApp":
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_TOKEN_FAILED);
                                        SharedPreferences token = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                                        SharedPreferences.Editor tokenEditor = token.edit();
                                        tokenEditor.clear();
                                        tokenEditor.apply();
                                        getActivity().finish();
                                        break;
                                    default:
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_DEFAULT_FAILED);
                                        break;
                                }
                                break;
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                    }
                    finally {
                        progressDialog.dismiss();
                    }
            }
        }
    };
    private ProgressDialog progressDialog;

    // Token 변수
    private String strToken;

    public ChangePasswordFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

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

                    try {
                        // POST 데이터 전송을 위한 자료구조
                        JSONObject rootObject = new JSONObject();
                        rootObject.put(NetworkInterface.REQUEST_CURRENT_PASSWORD, viewCurrentPassword.getText().toString());
                        rootObject.put(NetworkInterface.REQUEST_NEW_PASSWORD, viewNewPassword.getText().toString());
                        rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                        rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);

                        new RequestMessage(NetworkInterface.REST_CHANGE_PASSWORD, "PUT", rootObject, handler).start();
                    } catch (JSONException e) {
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                    }
                }
            }
        });

        return view;
    }
}
