package qualcomminstitute.iot;

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
    String TOAST_USED_SENSOR = "Already Registered Sensor";
    String TOAST_SENSOR_EXIST = "Already Have Sensor";
    String TOAST_SENSOR_NOTHING = "Please Registration Sensor First";
    String TOAST_UNUSED_SENSOR = "This Sensor Isn't Registered";
    String TOAST_NO_DATA = "No Sensor Data";

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
    String REQUEST_ADDRESS = "address";
    String REQUEST_TYPE = "type";
    String REQUEST_AIR = "air";
    String REQUEST_HEART_SENSOR = "heart";
    String REQUEST_USER_TYPE = "allUser";
    String REQUEST_ALL = "true";
    String REQUEST_USER = "false";
    String REQUEST_LAT = "latitude";
    String REQUEST_LON = "longitude";
    String REQUEST_HEART_RATE = "heartRate";
    String REQUEST_RR_INTERVAL = "rrInterval";
    String REQUEST_DATE = "day";
    String REQUEST_TIMESTAMP = "date";
    String REQUEST_TEMPERATURE = "temperature";

    // Network Response
    String MESSAGE_TYPE = "type";
    String MESSAGE_SUCCESS = "success";
    String MESSAGE_FAIL = "error";
    String MESSAGE_VALUE = "value";
    String MESSAGE_TOKEN = "token_app";
    String MESSAGE_AIR_ADDRESS = "airAddress";
    String MESSAGE_HEART_ADDRESS = "heartAddress";
    String MESSAGE_AIR_DATA = "airData";
    String MESSAGE_HEART_DATA = "heartData";
    String MESSAGE_HOUR = "hour";
    String MESSAGE_TIME_STAMP = "timestamp";
    String MESSAGE_TEMPERATURE = "temp";

    // Sensor Data
    String MESSAGE_CO = "co";
    String MESSAGE_SO2 = "so2";
    String MESSAGE_NO2 = "no2";
    String MESSAGE_O3 = "o3";
    String MESSAGE_PM25 = "pm25";
    String MESSAGE_DATE = "date";
    String MESSAGE_LAT = "latitude";
    String MESSAGE_LON = "longitude";
    String MESSAGE_HEART_RATE = "heartRate";
    String MESSAGE_RR_INTERVAL = "rrInterval";

    // Sensor Data
    String[] SENSOR_DATA = {"co", "no2", "so2", "o3", "pm25"};
    String[] SENSOR_AQI_DATA = {"coAqi", "no2Aqi", "so2Aqi", "o3Aqi", "pm25Aqi"};
    String[] MESSAGE_AQI_DATA = {"co_8h_aqi", "no2_1h_aqi", "so2_1h_aqi", "so2_24h_aqi", "o3_1h_aqi", "o3_8h_aqi", "pm25_aqi"};

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
    String REST_SENSOR_REGISTRATION = "sensor/register";
    String REST_SENSOR_LIST = "sensor/sensorlist";
    String REST_SENSOR_DEREGISTRATION = "sensor/deregister";
    String REST_AIR_QUALITY_INSERT = "data/AQInsert";
    String REST_HEART_INSERT = "data/HRInsert";
    String REST_AIR_QUALITY_REAL_TIME = "data/getRealtimeAQ";
    String REST_HEART_REAL_TIME = "data/getRealtimeHR";
    String REST_AIR_QUALITY_HISTORICAL = "data/getHistoricalAQ";
    String REST_HEART_HISTORICAL = "data/getHistoricalHR";

    // UUID
    String SECURE_UUID = "572f4ed8-c0ce-4f03-af85-2c44fccc3480";
    String INSECURE_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
}