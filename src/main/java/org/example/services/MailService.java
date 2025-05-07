package org.example.services;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;

public class MailService {

    public static void envoyerPDFParMail(String destinataire, String sujet, String messageTexte, String cheminPDF) {
        System.out.println("📤 Tentative d'envoi via Brevo avec template HTML...");

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

            // ✅ Partie texte alternative
            MimeBodyPart textePart = new MimeBodyPart();
            textePart.setText(messageTexte);

            // ✅ Partie HTML stylisée
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    margin: 0;
                    padding: 0;
                    background-color: #f8f9ff;
                    color: #333;
                }
                .container {
                    max-width: 600px;
                    margin: 20px auto;
                    background: white;
                    border-radius: 16px;
                    overflow: hidden;
                    box-shadow: 0 10px 30px rgba(100, 149, 237, 0.15);
                }
                .header {
                    background: linear-gradient(135deg, #87CEEB 0%, #DA70D6 50%, #9370DB 100%);
                    padding: 40px 20px;
                    text-align: center;
                    color: white;
                }
                .header h1 {
                    margin: 0;
                    font-size: 28px;
                    font-weight: 700;
                    text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
                }
                .content {
                    padding: 30px;
                    line-height: 1.6;
                }
                .attachment-card {
                    background: linear-gradient(135deg, #f5f7ff 0%, #fff0f9 100%);
                    border-left: 4px solid #9370DB;
                    padding: 20px;
                    margin: 25px 0;
                    border-radius: 8px;
                    display: flex;
                    align-items: center;
                }
                .attachment-icon {
                    font-size: 24px;
                    margin-right: 15px;
                    color: #9370DB;
                }
                .button {
                    display: inline-block;
                    background: linear-gradient(135deg, #87CEEB 0%, #DA70D6 100%);
                    color: white;
                    padding: 12px 30px;
                    text-decoration: none;
                    border-radius: 50px;
                    margin: 20px 0;
                    font-weight: bold;
                    text-align: center;
                    box-shadow: 0 4px 15px rgba(135, 206, 235, 0.3);
                }
                .footer {
                    background: #f5f7ff;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #666;
                    border-top: 1px solid #e0e0e0;
                }
                .gradient-text {
                    background: linear-gradient(135deg, #87CEEB 0%, #DA70D6 50%, #9370DB 100%);
                    -webkit-background-clip: text;
                    background-clip: text;
                    color: transparent;
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Votre document important</h1>
                </div>
                
                <div class="content">
                    <p>Bonjour,</p>
                    <p>Nous vous remercions pour votre confiance. Voici votre document personnalisé que vous avez demandé.</p>
                    
                    <div class="attachment-card">
                        <div class="attachment-icon">📎</div>
                        <div>
                            <p style="margin:0;font-weight:bold;">Document attaché</p>
                            <p style="margin:0;color:#666;">Fiche_Evenement.pdf</p>
                        </div>
                    </div>
                    
                    <p>Pour toute question, n'hésitez pas à nous contacter en répondant à cet email.</p>
                    
                    
                    <p>Cordialement,<br>
                    <span class="gradient-text">L'équipe Evenementiel</span></p>
                </div>
                
                <div class="footer">
                    <p>© 2023 Votre Entreprise. Tous droits réservés.</p>
                    <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                </div>
            </div>
        </body>
        </html>
        """;

            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            // ✅ Pièce jointe PDF
            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.attachFile(new File(cheminPDF));
            pdfPart.setFileName("Fiche_Evenement.pdf");

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

            // ✅ Partie HTML stylée (dégradé violet-rose)
            String htmlContent = String.format("""
    <html>
    <body style="margin:0; padding:0; font-family: Arial, sans-serif; background: linear-gradient(135deg, #8e2de2, #f27121); min-height: 100vh; color: white;">
        <div style="padding: 30px;">
            <h1 style="text-align: center;">🎟️ Votre Billet Électronique</h1>
            <p style="font-size: 18px;">%s</p>
            <div style="text-align: center; margin-top: 30px;">
                <p>📎 Votre QR Code est attaché en pièce jointe.</p>
                <p style="font-size:14px; color: #f5f5f5;">Merci pour votre confiance !</p>
            </div>
        </div>
    </body>
    </html>
    """, messageTexte);


            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            // ✅ QR code en pièce jointe
            MimeBodyPart qrPart = new MimeBodyPart();
            qrPart.attachFile(new File(cheminQRCode));
            qrPart.setFileName("qr-code-ticket.png");

            // ✅ Structure du message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(qrPart);

            msg.setContent(multipart);

            Transport.send(msg);

            System.out.println("✅ QR code envoyé avec succès à " + destinataire + " !");
        } catch (Exception e) {
            System.out.println("❌ Erreur d'envoi du QR code : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void envoyerTicketCompletParMail(String destinataire, String sujet, String messageTexte, String cheminQRCode, String cheminPDF) {
        System.out.println("📤 Tentative d'envoi du QR code + PDF...");

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

            // ✅ Partie texte alternative
            MimeBodyPart textePart = new MimeBodyPart();
            textePart.setText(messageTexte);

            // ✅ Partie HTML stylisée
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <style>
                body {
                    font-family: 'Arial', sans-serif;
                    margin: 0;
                    padding: 0;
                    background-color: #f8f9ff;
                    color: #333;
                }
                .container {
                    max-width: 600px;
                    margin: 20px auto;
                    background: white;
                    border-radius: 16px;
                    overflow: hidden;
                    box-shadow: 0 10px 30px rgba(100, 149, 237, 0.15);
                }
                .header {
                    background: linear-gradient(135deg, #87CEEB 0%, #DA70D6 50%, #9370DB 100%);
                    padding: 40px 20px;
                    text-align: center;
                    color: white;
                }
                .header h1 {
                    margin: 0;
                    font-size: 28px;
                    font-weight: 700;
                    text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
                }
                .content {
                    padding: 30px;
                    line-height: 1.6;
                }
                .qr-container {
                    text-align: center;
                    margin: 25px 0;
                    padding: 20px;
                    background: rgba(255, 255, 255, 0.7);
                    border-radius: 10px;
                    box-shadow: 0 5px 15px rgba(148, 85, 211, 0.1);
                }
                .attachment-card {
                    background: linear-gradient(135deg, #f5f7ff 0%, #fff0f9 100%);
                    border-left: 4px solid #9370DB;
                    padding: 20px;
                    margin: 25px 0;
                    border-radius: 8px;
                    display: flex;
                    align-items: center;
                }
                .attachment-icon {
                    font-size: 24px;
                    margin-right: 15px;
                    color: #9370DB;
                }
                .button {
                    display: inline-block;
                    background: linear-gradient(135deg, #87CEEB 0%, #DA70D6 100%);
                    color: white;
                    padding: 12px 30px;
                    text-decoration: none;
                    border-radius: 50px;
                    margin: 20px 0;
                    font-weight: bold;
                    text-align: center;
                    box-shadow: 0 4px 15px rgba(135, 206, 235, 0.3);
                }
                .footer {
                    background: #f5f7ff;
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                    color: #666;
                    border-top: 1px solid #e0e0e0;
                }
                .gradient-text {
                    background: linear-gradient(135deg, #87CEEB 0%, #DA70D6 50%, #9370DB 100%);
                    -webkit-background-clip: text;
                    background-clip: text;
                    color: transparent;
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Votre billet d'événement</h1>
                </div>
                
                <div class="content">
                    <p>Bonjour,</p>
                    <p>Votre réservation est confirmée ! Voici votre accès complet à l'événement.</p>
                    
                    <div class="qr-container">
                        <p><strong>Votre QR Code d'accès</strong></p>
                        <img src="cid:qr-code-image" alt="QR Code" style="width: 200px; height: 200px; margin: 10px auto; display: block;">
                        <p style="font-size:14px; color:#555;">Présentez ce code à l'entrée de l'événement</p>
                    </div>
                    
                    <div class="attachment-card">
                        <div class="attachment-icon">📎</div>
                        <div>
                            <p style="margin:0;font-weight:bold;">Document attaché</p>
                            <p style="margin:0;color:#666;">Details_Evenement.pdf</p>
                        </div>
                    </div>
                    
                    <p>Pour toute question, n'hésitez pas à répondre à cet email.</p>
                    
                    <p style="text-align:center;">
                        <a href="#" class="button">Accéder à votre espace client</a>
                    </p>
                    
                    <p>Cordialement,<br>
                    <span class="gradient-text">L'équipe Événementielle</span></p>
                </div>
                
                <div class="footer">
                    <p>© 2025 CMC. Tous droits réservés.</p>
                    <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                </div>
            </div>
        </body>
        </html>
        """;

            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            // ✅ QR code en pièce jointe (et intégré)
            MimeBodyPart qrPart = new MimeBodyPart();
            qrPart.attachFile(new File(cheminQRCode));
            qrPart.setContentID("<qr-code-image>");
            qrPart.setDisposition(MimeBodyPart.INLINE);

            // ✅ Pièce jointe PDF
            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.attachFile(new File(cheminPDF));
            pdfPart.setFileName("Details_Evenement.pdf");

            // ✅ Structure de l'e-mail
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);  // HTML design
            multipart.addBodyPart(qrPart);     // QR Code visible
            multipart.addBodyPart(pdfPart);    // PDF attaché

            msg.setContent(multipart);
            msg.setHeader("X-Mailin-track", "1");
            msg.setHeader("X-Mailin-track-click", "1");
            msg.setHeader("X-Mailin-track-open", "1");
            msg.setHeader("X-Mailin-client", "JavaApp");

            Transport.send(msg);
            System.out.println("✅ Mail complet (QR + PDF) envoyé avec succès à " + destinataire + " !");
        } catch (Exception e) {
            System.out.println("❌ Erreur d'envoi du mail : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
