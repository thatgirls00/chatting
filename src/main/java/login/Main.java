package login;

import javafx.application.Application;

import javafx.stage.Stage;
import static login.SignupScreen.sendEmail;
import login.LoginScreen;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 로그인 화면을 보여주는 메소드 호출
        LoginScreen.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);

    }
}