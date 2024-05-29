package login;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;
import java.sql.*;

public class SignupScreen {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/chat";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "0000";

    public static void show(Stage primaryStage) {
        primaryStage.setTitle("회원가입");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("아이디");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호");

        TextField emailField = new TextField();
        emailField.setPromptText("이메일");

        Label captchaLabel = new Label(generateCaptcha());
        TextField captchaField = new TextField();
        captchaField.setPromptText("문자를 입력하세요.");

        Button signupButton = new Button("가입하기");
        signupButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            String enteredCaptcha = captchaField.getText();

            if (!isValidUsername(username)) {
                showAlert(Alert.AlertType.ERROR, "아이디는 최소 하나의 영문자와 하나의 숫자를 포함해야 합니다.");
                return;
            }
            if (!isValidPassword(password)) {
                showAlert(Alert.AlertType.ERROR, "비밀번호는 최소 하나의 영문자와 하나의 숫자를 포함해야 합니다.");
                return;
            }
            if (!enteredCaptcha.equals(captchaLabel.getText())) {
                showAlert(Alert.AlertType.ERROR, "문자가 일치하지 않습니다.");
                return;
            }
            handleSignup(username, password, email, primaryStage);
        });

        root.getChildren().addAll(usernameField, passwordField, emailField, captchaLabel, captchaField, signupButton);

        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    private static boolean isValidUsername(String username) {
        return username.matches(".*[a-zA-Z].*") && username.matches(".*\\d.*");
    }

    private static boolean isValidPassword(String password) {
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    private static void showAlert(Alert.AlertType alertType, String content) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void handleSignup(String username, String password, String email, Stage primaryStage) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM REGISTRATION WHERE username = ?");
            checkStatement.setString(1, username);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                showAlert(Alert.AlertType.ERROR, "이미 존재하는 아이디입니다.");
                return;
            }

            String authCode = generateAuthCode();
            sendEmail(email, "인증 코드", "인증 코드: " + authCode);

            VerifyEmailScreen.show(primaryStage, username, password, email, authCode);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "회원가입 중 오류가 발생했습니다.");
        }
    }

    private static String generateAuthCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    public static void sendEmail(String to, String subject, String text) {
        // 이메일 관련 정보
        final String username; // 발신자 이메일 주소
        username = "w__p__o__z@naver.com";
        final String password = "BB6D11XGEEJ2"; // 발신자 이메일 비밀번호

        // 이메일 서버 설정
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.naver.com");
        props.put("mail.smtp.port", "587");
        // 여기에 TLS 프로토콜을 하나씩 설정
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // 디버깅을 위해 SSL 디버그 정보 출력 (필요시 주석 해제)
        // System.setProperty("javax.net.debug", "all");

        // 세션 생성
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 메시지 생성
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // 발신자 설정
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 수신자 설정
            message.setSubject(subject); // 제목 설정
            message.setText(text); // 내용 설정

            // 메시지 전송
            Transport.send(message);

            System.out.println("이메일 전송 완료");

        } catch (MessagingException e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
            throw new RuntimeException(e);
        }
    }

    private static String generateCaptcha() {
        // 간단한 랜덤 문자열을 생성합니다.
        String characters = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnpqrstuvwxyz123456789";
        StringBuilder captcha = new StringBuilder();
        Random rnd = new Random();
        while (captcha.length() < 6) { // 6자리의 랜덤 문자열 생성
            int index = (int) (rnd.nextFloat() * characters.length());
            captcha.append(characters.charAt(index));
        }
        return captcha.toString();
    }
}
