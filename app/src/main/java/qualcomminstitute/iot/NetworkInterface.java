package qualcomminstitute.iot;

import java.util.LinkedHashMap;
import java.util.Map;

public interface NetworkInterface {
    String SERVER_ADDRESS = "teamd-iot.calit2.net/";
    String ENCODE = "UTF-8";
    Map<String, String> REST_API = new LinkedHashMap<String, String>(){
        {
            put("SIGN_IN","accounts/authenticate");
            put("SIGN_UP","accounts/signup");
        }
    };
    Map<String, String> SIGN_IN_MESSAGE = new LinkedHashMap<String, String>(){
        {
            put("TYPE", "app");
            put("EMAIL","email");
            put("PASSWORD","password");
            put("SUCCESS", "token_app");
            put("FAILED", "error");
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
        }
    };
}