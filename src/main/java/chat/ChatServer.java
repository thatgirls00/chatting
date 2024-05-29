package chat;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer extends Application {

    private TextArea chatLogsArea;
    private ServerSocket serverSocket;
    private List<PrintWriter> clientOutputs = new ArrayList<>();

    // 클라이언트 정보 저장
    private Map<String, Socket> clients = new HashMap<>();  // id, socket
    private Map<String, String> users = new HashMap<>();    // id, name

    public static void main(String[] args) {
        launch(args);
    }

    // gui 구성
    @Override
    public void start(Stage stage) {
        try {
            // 컨테이너 생성
            Pane pane = new Pane();

            // 채팅 내용
            chatLogsArea = new TextArea();
            chatLogsArea.setEditable(false);
            chatLogsArea.setLayoutX(25);
            chatLogsArea.setLayoutY(24);
            chatLogsArea.setPrefSize(350, 510);

            // 이용자 목록 버튼
            Button userList = new Button("이용자 목록");
            userList.setLayoutX(288);
            userList.setLayoutY(552);
            userList.setPrefSize(87, 30);
            userList.setOnAction(new userListHandler());

            // 공지사항 전송 버튼
            Button sendNoticeButton = new Button("공지사항 작성");
            sendNoticeButton.setLayoutX(182);
            sendNoticeButton.setLayoutY(552);
            sendNoticeButton.setPrefSize(99, 30);
            sendNoticeButton.setOnAction(new noticeHandler());

            // 컨테이너에 등록
            pane.getChildren().addAll(chatLogsArea, userList, sendNoticeButton);

            // 화면에 표시
            Scene scene = new Scene(pane, 400, 600);
            stage.setTitle("Chat-Server");
            stage.setScene(scene);
            stage.show();

            // 서버 시작
            new Thread(this::serverOn).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void serverOn() {
        try {
            // 서버 시작
            serverSocket = new ServerSocket(8585);
            System.out.println("서버 시작 성공");

            // 클라이언트와 계속해서 연결시도
            while (true) {
                clientConnect();
            }

        } catch (Exception e) {
            System.out.println("서버 시작 실패");
            e.printStackTrace();
        }
    }

    // 클라이언트와 연결 + 스레드를 통해 클라이언트 통신을 독립적으로 처리
    private void clientConnect() {
        try {
            Socket clientSocket = serverSocket.accept();
            System.out.println("클라이언트 연결 성공");

            Thread thread = new Thread(new ClientHandler(clientSocket));
            thread.start();

        } catch (Exception e) {
            System.out.println("클라이언트 연결 실패");
        }
    }

    private void disconnectClient(String userId) {
        try {
            Socket socket = clients.get(userId);

            // 강퇴 알림 채팅창에 전송
            String userName = users.get(userId);
            String message = userName + "(" + userId + ")님이 강퇴되었습니다.";
            chatLogsArea.appendText(message + "\n");
            for (PrintWriter writer : clientOutputs) {
                writer.println(message);
            }

            // 강퇴
            socket.close();
            clients.remove(userId);
            users.remove(userId);

        } catch (Exception e) {
            System.out.println("클라이언트 종료 실패");
        }
    }

    // 클라이언트 통신 처리
    private class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        private String userId;
        private String userName;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // 클라이언트 출력스트림 리스트에 저장
                clientOutputs.add(out);

                // 클라이언트로부터 userId, userName 가져옴
                userId = in.readLine();
                userName = in.readLine();

                // HashMap에 사용자 정보를 저장
                clients.put(userId, clientSocket);
                users.put(userId, userName);

                // 서버가 받은 메시지를 모든 클라이언트에게 전송
                String userInputMessage;
                while ((userInputMessage = in.readLine()) != null) {
                    // 메시지 - 이름(id)
                    String message = userName + "(" + userId + "): " + userInputMessage;
                    chatLogsArea.appendText(message + "\n");

                    for (PrintWriter writer : clientOutputs) {
                        writer.println(message);
                    }
                }

            } catch (Exception e) {
                System.out.println("클라이언트 통신 불량");
                e.printStackTrace();

            } finally {
                try {
                    clientOutputs.remove(out);
                    out.close();
                    in.close();
                    clientSocket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 공지사항 작성 버튼 클릭시 사용하는 내부클래스
    private class noticeHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            Stage stage = new Stage();
            Pane pane = new Pane();

            TextArea noticeTextArea = new TextArea();
            noticeTextArea.setLayoutX(16);
            noticeTextArea.setLayoutY(9);
            noticeTextArea.setPrefSize(268, 103);

            Button sendButton = new Button("전송");
            sendButton.setLayoutX(185);
            sendButton.setLayoutY(118);
            sendButton.setPrefSize(44, 27);
            sendButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        if (!noticeTextArea.getText().isBlank()) {
                            String notice = "[공지사항]\n" + noticeTextArea.getText();
                            chatLogsArea.appendText("\n" + notice + "\n\n");

                            for (PrintWriter writer : clientOutputs) {
                                writer.println("\n" + notice + "\n");
                            }
                            stage.close();

                        } else {
                            throw new IOException();
                        }

                    } catch (IOException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("공지사항 입력 오류");
                        alert.setHeaderText(null);
                        alert.setContentText("공지사항 입력에 오류가 발생했습니다.");
                        alert.showAndWait();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Button cancelButton = new Button("취소");
            cancelButton.setLayoutX(240);
            cancelButton.setLayoutY(118);
            cancelButton.setPrefSize(44, 27);
            cancelButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    stage.close();
                }
            });

            pane.getChildren().addAll(noticeTextArea, sendButton, cancelButton);

            Scene scene = new Scene(pane, 300, 150);
            stage.setTitle("공지사항 입력창");
            stage.setScene(scene);
            stage.show();
        }
    }

    // 이용자 목록 버튼 클릭시 사용하는 내부클래스
    private class userListHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            Stage stage = new Stage();
            VBox userList = new VBox();

            // 이용자, 강퇴 버튼 만들기
            for (Map.Entry<String, String> entry : users.entrySet()) {
                HBox userRow = new HBox();

                String name = entry.getValue();
                String id = entry.getKey();

                Label nameLabel = new Label(name + " (" + id + ")");

                Button kickButton = new Button("강퇴");
                kickButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        disconnectClient(id);
                    }
                });

                userRow.getChildren().addAll(nameLabel, kickButton);
                userList.getChildren().add(userRow);
            }

            ScrollPane scrollPane = new ScrollPane(userList);
            scrollPane.setPrefSize(200, 300);

            Scene scene = new Scene(scrollPane, 200, 300);
            stage.setScene(scene);
            stage.setTitle("이용자 목록");
            stage.show();
        }
    }
}
