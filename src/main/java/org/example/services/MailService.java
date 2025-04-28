package org.example.services;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;

public class MailService {

    public static void envoyerPDFParMail(String destinataire, String sujet, String messageTexte, String cheminPDF) {
        System.out.println("📤 Tentative d'envoi via Brevo avec template HTML...");

        final String expediteur = "nourbrahem275@gmail.com"; // ✅ Adresse Brevo vérifiée
        final String username = "89b929001@smtp-brevo.com";   // ✅ Identique à l'expéditeur
        final String password = "aFOLZTUq7fSVQhXk";           // ✅ SMTP password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp-relay.brevo.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(expediteur));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject(sujet);

            // ✅ Partie texte alternative (fallback pour anciens clients)
            MimeBodyPart textePart = new MimeBodyPart();
            textePart.setText(messageTexte); // fallback text

            // ✅ Partie HTML stylisée
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent =
                    "<div style='font-family:Arial,sans-serif;padding:20px;border:1px solid #e0e0e0;border-radius:8px'>" +
                            "  <h2 style='color:#1a66ff;'>🎉 Détails de votre événement</h2>" +
                            "  <p>Merci pour votre confiance ! Veuillez trouver ci-joint votre fiche événement personnalisée.</p>" +
                            "  <p style='margin-top:20px;'>📎 Le document PDF est attaché à ce mail.</p>" +
                            "  <p style='margin-top:30px;font-size:12px;color:#777;'>Ceci est un email automatique. Merci de ne pas y répondre.</p>" +
                            "</div>";

            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            // ✅ Pièce jointe PDF
            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.attachFile(new File(cheminPDF));

            // ✅ Combinaison des parties
            Multipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(textePart);
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(pdfPart);

            msg.setContent(multipart);
            msg.setHeader("X-Mailin-track", "1");
            msg.setHeader("X-Mailin-track-click", "1");
            msg.setHeader("X-Mailin-track-open", "1");
            msg.setHeader("X-Mailin-client", "JavaApp");


            Transport.send(msg);
            System.out.println("✅ Email HTML avec PDF envoyé avec succès via Brevo !");
        } catch (Exception e) {
            System.out.println("❌ Erreur d'envoi : " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void envoyerQRCodeParMail(String destinataire, String sujet, String messageTexte, String cheminQRCode) {
        System.out.println("📤 Tentative d'envoi du QR code...");

        final String expediteur = "nourbrahem275@gmail.com";
        final String username = "89b929001@smtp-brevo.com";
        final String password = "aFOLZTUq7fSVQhXk";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp-relay.brevo.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(expediteur));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject(sujet);

            // ✅ Texte brut
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(messageTexte);

            // ✅ QR code en pièce jointe
            MimeBodyPart qrPart = new MimeBodyPart();
            qrPart.attachFile(new File(cheminQRCode));
            qrPart.setFileName("qr-code-ticket.png");

            // ✅ Structure de l'e-mail
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(qrPart);

            msg.setContent(multipart);
            Transport.send(msg);

            System.out.println("✅ QR code envoyé avec succès à " + destinataire + " !");
        } catch (Exception e) {
            System.out.println("❌ Erreur d'envoi du QR code : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
