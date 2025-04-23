package org.example.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    private final Properties properties;
    private final String senderEmail;
    private final String senderPassword;

    public EmailService() {
        // SMTP configuration for Gmail
        properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Replace with your email and app-specific password
        senderEmail = "saidazizz132@gmail.com"; // Replace with your Gmail address
        senderPassword = "qlgwxgyogrysgbie"; // Replace with your app-specific password
    }

    public void sendVerificationEmail(String recipientEmail, String verificationCode) throws MessagingException {
        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        // Create the email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Code de vérification pour votre inscription");
        message.setText("Bonjour,\n\nVotre code de vérification est : " + verificationCode +
                "\n\nCe code est valable pendant 10 minutes.\n\nCordialement,\nVotre Équipe");

        // Send the email
        Transport.send(message);
    }

    public void sendChurnWarningEmail(String recipientEmail, String userName) throws MessagingException {
        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        // Create the email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Nous souhaitons vous garder avec nous !");
        message.setText(String.format("Bonjour %s,\n\nNous avons remarqué que vous n'avez pas été actif récemment sur notre plateforme. " +
                "Nous aimerions vous inviter à revenir explorer nos fonctionnalités, cours, et événements ! " +
                "Si vous avez besoin d'aide ou de recommandations, n'hésitez pas à nous contacter.\n\nCordialement,\nVotre Équipe", userName));

        // Send the email
        Transport.send(message);
    }

    public String generateVerificationCode() {
        // Generate a 6-digit random code
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}