package chat;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ChatClient extends Application {

    private TextArea chatInputArea;
    private TextArea chatLogsArea;
    private Button sendButton;

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/chat";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "0000";

    private String userId = "";
    private String userName = "";

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // 컨테이너 생성
            Pane pane = new Pane();

            // 채팅 내용
            chatLogsArea = new TextArea();
            chatLogsArea.setEditable(false);
            chatLogsArea.setLayoutX(25);
            chatLogsArea.setLayoutY(58);
            chatLogsArea.setPrefSize(350, 395);

            // 채팅 입력창
            chatInputArea = new TextArea();
            chatInputArea.setLayoutX(25);
            chatInputArea.setLayoutY(459);
            chatInputArea.setPrefSize(350, 83);
            chatInputArea.setPromptText("채팅 입력");

            // 메시지 전송 버튼
            sendButton = new Button("전송");
            sendButton.setLayoutX(310);
            sendButton.setLayoutY(555);
            sendButton.setPrefSize(65, 27);

            // 전송 버튼 클릭 이벤트
            sendButton.setOnAction(event -> {
                String message = chatInputArea.getText();
                if (!message.isEmpty()) {
                    out.println(message);
                    chatInputArea.clear();
                }
            });

            // 컨테이너에 등록
            pane.getChildren().addAll(chatLogsArea, chatInputArea, sendButton);

            // 화면에 표시
            Scene scene = new Scene(pane, 400, 600);
            stage.setTitle("Chat-Client");
            stage.setScene(scene);
            stage.show();

            // 서버와 연결
            serverConnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 서버와 연결
    private void serverConnect() {
        try {
            clientSocket = new Socket("localhost", 8585);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("서버 연결 성공");

            // userId와 userName을 서버에 전달
            out.println(userId);
            out.println(userName);

            Thread serverThread = new Thread(new ServerHandler());
            serverThread.start();

        } catch (Exception e) {
            System.out.println("서버 연결 실패");
            e.printStackTrace();
        }
    }

    // 사용자 이름 설정 및 목록에 추가
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // 서버로부터 수신받는 것에 대한 처리
    private class ServerHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    chatLogsArea.appendText(message + "\n");
                    // 강퇴알림을 받았을경우 창띄우고 클라이언트 창 종료
                    if (message.equals(userName + "(" + userId + ")님이 강퇴되었습니다.")) {
                        System.exit(0);
                    }
                }

            } catch (Exception e) {
                System.out.println("서버 응답 오류 발생");
            }
        }
    }
}