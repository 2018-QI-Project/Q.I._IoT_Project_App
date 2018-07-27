package qualcomminstitute.iot;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ChangePasswordFragment extends Fragment {
    private EditText viewCurrentPassword, viewNewPassword, viewRepeatNewPassword;
    private Button viewSubmit;

    public ChangePasswordFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        viewCurrentPassword = view.findViewById(R.id.txtChangePasswordCurrentPassword);
        viewNewPassword = view.findViewById(R.id.txtChangePasswordNewPassword);
        viewRepeatNewPassword = view.findViewById(R.id.txtChangePasswordNewPasswordRepeat);

        viewSubmit = view.findViewById(R.id.btnChangePasswordSubmit);
        viewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utility.validateInputForm(getActivity(), viewCurrentPassword, viewNewPassword) && Utility.validatePassword(viewNewPassword, viewRepeatNewPassword)) {
                    int a = 2;
                }
            }
        });

        return view;
    }
}
