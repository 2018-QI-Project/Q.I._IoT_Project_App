package qualcomminstitute.iot;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpActivity extends AppCompatActivity {
    private EditText viewEmail, viewPassword, viewRepeatPassword, viewFullName, viewAge;
    private Button viewSubmit;
    private TextView viewSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        viewEmail = findViewById(R.id.txtSignUpEmail);
        viewPassword = findViewById(R.id.txtSignUpPassword);
        viewRepeatPassword = findViewById(R.id.txtSignUpRepeatPassword);
        viewFullName = findViewById(R.id.txtSignUpFullName);
        viewAge = findViewById(R.id.txtSignUpAge);
        viewSubmit = findViewById(R.id.btnSingUpSubmit);
        viewSignIn = findViewById(R.id.txtSignUpSignIn);

        viewSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Creating Account...");
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

    public boolean validate() {
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

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            viewPassword.setError("Password must between 4 and 10 alphanumeric characters");
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

        if(!age.isEmpty() && !age.matches("^[0-9]*$")) {
            viewAge.setError("Age is incorrect");
            viewAge.requestFocus();
            return false;
        } else {
            viewAge.setError(null);
        }

        return true;
    }
}
