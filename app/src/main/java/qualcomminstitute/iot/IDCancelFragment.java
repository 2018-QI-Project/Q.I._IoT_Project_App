package qualcomminstitute.iot;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import static android.content.Context.MODE_PRIVATE;
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
                        SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, MODE_PRIVATE);
                        SharedPreferences.Editor dataEditor = data.edit();
                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                Utility.displayToastMessage(handler, getActivity(), TOAST_ID_CANCELLATION);
                                dataEditor.clear();
                                dataEditor.apply();
                                getActivity().finish();
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_CLIENT_FAILED);
                                        break;
                                    case "wrong password":
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_PASSWORD_FAILED);
                                        break;
                                    case "invalid token":
                                        Utility.displayToastMessage(handler, getActivity(), TOAST_TOKEN_FAILED);
                                        dataEditor.remove(PreferenceName.preferenceToken);
                                        dataEditor.apply();
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

    private String strToken;

    public IDCancelFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_id_cancellation, container, false);

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

                            try {
                                // POST 데이터 전송을 위한 자료구조
                                JSONObject rootObject = new JSONObject();
                                rootObject.put(NetworkInterface.REQUEST_PASSWORD, viewPassword.getText().toString());
                                rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                                rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);

                                new RequestMessage(NetworkInterface.REST_ID_CANCELLATION, "DELETE", rootObject, handler).start();
                            } catch (JSONException e) {
                                Log.e(this.getClass().getName(), "JSON ERROR!");
                                Utility.displayToastMessage(handler, getActivity(), TOAST_EXCEPTION);
                            }
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