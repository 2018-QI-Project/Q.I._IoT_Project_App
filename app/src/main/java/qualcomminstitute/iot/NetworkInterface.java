package qualcomminstitute.iot;

import java.util.LinkedHashMap;
import java.util.Map;

public interface NetworkInterface {
    // Network Connection
    String SERVER_ADDRESS = "teamd-iot.calit2.net/";
    String ENCODE = "UTF-8";
    String POST_HEADER = "application/x-www-form-urlencoded";

    // Toast Message
    String TOAST_SIGN_IN_VERIFY = "Email isn't Valid. Please Check Verify Email.";
    String TOAST_SIGN_IN_PASSWORD_FAILED = "Sign In Failed. Please Check Password.";
    String TOAST_SIGN_IN_REGISTER = "Sign In Failed. Please Check Email.";
    String TOAST_DEFAULT_FAILED = "Please Contact Server Manager.";
    String TOAST_EXCEPTION = "Please Try Again.";
    String TOAST_TOKEN_FAILED = "Session is Expired. Sign In Please.";

    // For REST API
    Map<String, String> REST_API = new LinkedHashMap<String, String>(){
        {
            put("SIGN_IN","accounts/authenticate");
            put("SIGN_UP","accounts/signup");
            put("FORGOT_PASSWORD", "accounts/resetpassword");
            put("CHANGE_PASSWORD", "accounts/changepassword");
            put("ID_CANCEL", "accounts/IDCancellation");
        }
    };
    Map<String, String> SIGN_IN_MESSAGE = new LinkedHashMap<String, String>(){
        {
            put("CLIENT_KEY", "client");
            put("CLIENT_VALUE", "app");
            put("EMAIL","email");
            put("PASSWORD","password");
            put("SUCCESS", "token_app");
            put("MESSAGE", "value");
        }
    };
    Map<String, String> SIGN_UP_MESSAGE = new LinkedHashMap<String, String>(){
        {
            put("EMAIL","email");
            put("PASSWORD","password");
            put("FULL_NAME","name");
            put("AGE","age");
            put("GENDER","gender");
            put("BREATHE","respiratoryDisease");
            put("HEART","cardiovascularDisease");
            put("TYPE", "type");
            put("FAILED", "error");
            put("MESSAGE", "value");
        }
    };
    Map<String, String> CHANGE_PASSWORD_MESSAGE = new LinkedHashMap<String, String>(){
        {
            put("CLIENT_KEY", "Client");
            put("CLIENT_VALUE", "app");
            put("TOKEN", "tokenApp");
            put("PASSWORD","currentPassword");
            put("NEW_PASSWORD","newPassword");
            put("FAILED", "error");
            put("MESSAGE", "value");
        }
    };
}