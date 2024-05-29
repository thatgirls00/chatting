module login {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.mail;


    opens login to javafx.fxml;
    exports login;
    exports chat;
}