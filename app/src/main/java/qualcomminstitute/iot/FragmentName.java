package qualcomminstitute.iot;

public enum FragmentName {
    REAL_DATA("Real Time Data"),
    SENSOR_INFORMATION("Sensor Information"),
    MAP("Map"),
    PREVIOUS_DATA("Historical Air Quality Data"),
    CHANGE_PASSWORD("Change Password"),
    ID_CANCEL("ID Cancel"),
    SIGN_OUT("Sign Out");

    final String name;

    FragmentName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
