package login;

import chat.ChatClient;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import chat.ChatClient; // ChatClient 클래스 임포트


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen {

    // MySQL 연결 정보
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat";
    private static final String DB_USERNAME = "root"; // MySQL 사용자 이름
    private static final String DB_PASSWORD = "0000"; // MySQL 비밀번호

    public static void show(Stage primaryStage) {
        primaryStage.setTitle("로그인");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("아이디");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호");

        Button loginButton = new Button("로그인");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            // 로그인 처리 메소드 호출
            handleLogin(username, password, primaryStage);
        });

        Button signupButton = new Button("회원가입");
        signupButton.setOnAction(e -> {
            // 회원가입 화면을 보여주는 메소드 호출
            SignupScreen.show(primaryStage);
        });

        root.getChildren().addAll(usernameField, passwordField, loginButton, signupButton);

        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();
    }

    private static void handleLogin(String username, String password, Stage primaryStage) {
        // 데이터베이스 연결 및 쿼리 실행
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT * FROM REGISTRATION WHERE username = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) { // 쿼리 결과가 존재하는 경우
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("로그인되었습니다.");
                        alert.showAndWait();
                        // 로그인 성공시 채팅 클라이언트 화면을 띄움
                        openChatClient(primaryStage, username);
                    } else { // 쿼리 결과가 존재하지 않는 경우
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("아이디 또는 비밀번호가 잘못되었습니다.");
                        alert.showAndWait();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("로그인 처리 중 오류가 발생했습니다.");
            alert.showAndWait();
        }
    }
    private static void openChatClient(Stage primaryStage, String username) {
        try {
            // ChatClient 객체 생성
            ChatClient chatClient = new ChatClient();
            // ChatClient에 사용자 이름 설정
            chatClient.setUserName(username);
            // ChatClient를 시작
            chatClient.start(new Stage());
            // 로그인 화면 닫기
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("채팅 클라이언트를 열던 중 오류가 발생했습니다.");
            alert.showAndWait();
        }
    }
}
