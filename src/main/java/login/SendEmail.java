package login;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class SendEmail {

    public static void sendEmail(String to, String subject, String text) {
        // 이메일 관련 정보
        final String username = "w__p__o__z@naver.com"; // 발신자 이메일 주소
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


}