package qualcomminstitute.iot;

public enum FragmentName {
    REAL_DATA("Real Time Data"),
    SENSOR_INFORMATION("Sensor Information"),
    PREVIOUS_DATA("Previous Sensor Data"),
    CHANGE_PASSWORD("Change Password"),
    ID_CANCEL("ID Cancel");

    final String name;

    FragmentName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
