package login;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VerifyEmailScreen {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/chat";
    private static final String JDBC_USERNAME = "root"; // MySQL 사용자명
    private static final String JDBC_PASSWORD = "0000"; // MySQL 비밀번호

    public static void show(Stage primaryStage, String username, String password, String naveremail, String authCode) {
        primaryStage.setTitle("이메일 인증");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        TextField codeField = new TextField();
        codeField.setPromptText("인증 코드");

        Button verifyButton = new Button("인증하기");
        verifyButton.setOnAction(e -> {
            String inputCode = codeField.getText();
            if (authCode.equals(inputCode)) {
                completeSignup(username, password, naveremail, primaryStage);
            } else {
                showAlert(Alert.AlertType.ERROR, "인증 코드가 일치하지 않습니다.");
            }
        });

        root.getChildren().addAll(codeField, verifyButton);

        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();
    }

    private static void completeSignup(String username, String password, String naveremail, Stage primaryStage) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO REGISTRATION (username, password) VALUES (?, ?)");
            insertStatement.setString(1, username);
            insertStatement.setString(2, password);
            int rowsInserted = insertStatement.executeUpdate();
            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "회원가입이 완료되었습니다.");
                LoginScreen.show(primaryStage);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "회원가입 중 오류가 발생했습니다.");
        }
    }

    private static void showAlert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}