package qualcomminstitute.iot;

import java.util.LinkedHashMap;
import java.util.Map;

public interface NetworkInterface {
    // Network Connection
    String SERVER_ADDRESS = "teamd-iot.calit2.net/";
    String ENCODE = "UTF-8";
    String JSON_HEADER = "application/json";

    // Toast Message
    String TOAST_DEFAULT_FAILED = "Please Contact Server Manager";
    String TOAST_CLIENT_FAILED = "Client type isn't correct. Contact Manager";
    String TOAST_TOKEN_FAILED = "Session is Expired. Sign In Please";
    String TOAST_EXCEPTION = "Please Try Again";
    String TOAST_DUPLICATE_EMAIL = "Duplicate Email. Write another email";
    String TOAST_CHECK_MAIL = "Sending mail. Please Check mail";
    String TOAST_VERIFY = "Email isn't Valid. Please Check Verify mail";
    String TOAST_PASSWORD_FAILED = "Please Check Password";
    String TOAST_REGISTER = "Please Doing Registration First";
    String TOAST_CHANGED_PASSWORD = "Password is Changed!";
    String TOAST_ID_CANCELLATION = "ID Cancellation is complete. Thank you";

    // Network Request
    String REQUEST_CLIENT_TYPE = "client";
    String REQUEST_CLIENT = "app";
    String REQUEST_TOKEN = "tokenApp";
    String REQUEST_EMAIL = "email";
    String REQUEST_PASSWORD = "password";
    String REQUEST_FULL_NAME = "name";
    String REQUEST_AGE = "age";
    String REQUEST_GENDER = "gender";
    String REQUEST_BREATHE = "respiratoryDisease";
    String REQUEST_HEART = "cardiovascularDisease";
    String REQUEST_CURRENT_PASSWORD = "currentPassword";
    String REQUEST_NEW_PASSWORD = "newPassword";

    // Network Response
    String MESSAGE_TYPE = "type";
    String MESSAGE_SUCCESS = "success";
    String MESSAGE_FAIL = "error";
    String MESSAGE_VALUE = "value";
    String MESSAGE_TOKEN = "token_app";

    // Handler Message
    int REQUEST_FAIL = 0;
    int REQUEST_SUCCESS = 1;
    String RESPONSE_DATA = "Response";

    // REST API
    String REST_SIGN_IN = "accounts/authenticate";
    String REST_SIGN_UP = "accounts/signup";
    String REST_RESET_PASSWORD = "accounts/resetpassword";
    String REST_CHANGE_PASSWORD = "accounts/changepassword";
    String REST_ID_CANCELLATION = "accounts/IDcancellation";
    String REST_SIGN_OUT = "accounts/signout";
}