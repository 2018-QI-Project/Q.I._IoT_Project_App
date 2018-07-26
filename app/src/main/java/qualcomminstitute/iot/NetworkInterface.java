package qualcomminstitute.iot;

import java.util.LinkedHashMap;
import java.util.Map;

public interface NetworkInterface {
    String SERVER_ADDRESS = "teamd-iot.calit2.net/";
    Map<String, String> REST_API = new LinkedHashMap<String, String>(){
        {
            put("SIGN_IN","accounts/signin");
            put("SIGN_UP","accounts/signup");
        }
    };
}