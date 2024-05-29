package login;

import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LoggedInScreen {

    public static void show(Stage primaryStage) {
        primaryStage.setTitle("로그인 성공");

        StackPane root = new StackPane();
        root.setStyle("-fx-padding: 10;");

        Label label = new Label("로그인되었습니다.");
        root.getChildren().add(label);

        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();
    }
}