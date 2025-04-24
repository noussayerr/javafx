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

        // TODO: Move to configuration file or environment variables for security
        senderEmail = "saidazizz132@gmail.com"; // Replace with your Gmail address
        senderPassword = "qlgwxgyogrysgbie"; // Replace with your app-specific password
    }

    // Reusable method to send an email
    private void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {
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
        message.setSubject(subject);
        message.setText(body);

        // Send the email
        Transport.send(message);
    }

    public void sendVerificationEmail(String recipientEmail, String verificationCode) throws MessagingException {
        String subject = "Code de vérification pour votre inscription";
        String body = "Bonjour,\n\nVotre code de vérification est : " + verificationCode +
                "\n\nCe code est valable pendant 10 minutes.\n\nCordialement,\nVotre Équipe";
        sendEmail(recipientEmail, subject, body);
    }

    public void sendChurnWarningEmail(String recipientEmail, String userName) throws MessagingException {
        String subject = "Nous souhaitons vous garder avec nous !";
        String body = String.format("Bonjour %s,\n\nNous avons remarqué que vous n'avez pas été actif récemment sur notre plateforme. " +
                "Nous aimerions vous inviter à revenir explorer nos fonctionnalités, cours, et événements ! " +
                "Si vous avez besoin d'aide ou de recommandations, n'hésitez pas à nous contacter.\n\nCordialement,\nVotre Équipe", userName);
        sendEmail(recipientEmail, subject, body);
    }

    public void sendResetCodeEmail(String recipientEmail, String resetCode) throws MessagingException {
        String subject = "Code de réinitialisation du mot de passe";
        String body = "Bonjour,\n\nVotre code de réinitialisation du mot de passe est : " + resetCode +
                "\n\nCe code est valide pendant 30 minutes.\n\nCordialement,\nVotre Application";
        sendEmail(recipientEmail, subject, body);
    }

    public String generateVerificationCode() {
        // Generate a 6-digit random code
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}