package qualcomminstitute.iot;

public interface InputFormCondition {
    String EMAIL_CONDITION = "^[a-zA-Z0-9]+@[[a-zA-Z0-9]*\\.]+[a-zA-Z0-9]+$";
    String PASSWORD_CONDITION = "^[a-zA-Z0-9]{4,10}$";
    String FULL_NAME_CONDITION = "^[[a-zA-Z]*\\s?]*[a-zA-Z]+$";
    String AGE_CONDITION = "^[0-9]+$";

    String ERROR_EMAIL = "Enter a valid email address";
    String ERROR_PASSWORD = "Password must between 4 and 10 alphanumeric characters";
    String ERROR_REPEAT_PASSWORD = "Password isn't match";
    String ERROR_FULL_NAME = "Name is incorrect";
    String ERROR_AGE = "Age is incorrect";
    String ERROR_AGREE = "If you erase information, Please Check it";
}