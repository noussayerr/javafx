package org.example.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.services.ServiceTransaction;
import org.example.utils.SessionManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListTransactionController implements Initializable {
    @FXML
    private TableColumn<Transaction, Void> pdfCol;
    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, Float> amountCol;

    @FXML
    private TableColumn<Transaction, String> transactionIdCol;

    @FXML
    private TableColumn<Transaction, LocalDateTime> transactionDateCol;

    @FXML
    private TableColumn<Transaction, String> statusCol;
    private Apprenant currentapprenant;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    private ServiceTransaction serviceTransaction = new ServiceTransaction();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setCurrentUser();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Utilisateur non trouvé. Veuillez réessayer.", ButtonType.OK);
            alert.setTitle("Erreur");
            alert.setHeaderText("Utilisateur non trouvé");
            alert.showAndWait();

        }
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));


        try {
            loadTransactionsForCurrentUser();
            addPdfButtonToTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void setCurrentUser() throws SQLException {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        this.currentapprenant=serviceTransaction.getApprenantById(currentUser.getId());
    }

    private void loadTransactionsForCurrentUser() throws SQLException {

        List<Transaction> allTransactions = serviceTransaction.afficher();


        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> t.getApprenant().getId() == this.currentapprenant.getId())
                .collect(Collectors.toList());

        transactionList.setAll(filtered);
        transactionTable.setItems(transactionList);
    }
    private void addPdfButtonToTable() {
        pdfCol.setCellFactory(col -> new TableCell<>() {
            private final Button pdfButton = new Button();

            {
                Image pdfImage = new Image(getClass().getResourceAsStream("/icons/pdf_icon.png"), 20, 20, true, true);

                pdfButton.setGraphic(new ImageView(pdfImage));
                pdfButton.setStyle("-fx-background-color: transparent;");
                pdfButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    try {
                        exportTransactionToPdf(transaction);
                    } catch (IOException | WriterException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pdfButton);
                }
            }
        });
    }
    public static void exportTransactionToPdf(Transaction t) throws IOException, WriterException {
        String filePath = "Transaction_" + t.getTransactionId() + ".pdf";

        // Création du writer et du document PDF
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Police
        PdfFont fontBold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(FontConstants.HELVETICA);

        // Titre du document
        document.add(new Paragraph("Détail de la transaction")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Ajout d'un tableau avec des bordures pour une meilleure présentation
        Table table = new Table(2); // Deux colonnes : une pour l'étiquette, une pour la valeur
        table.setWidth(100f);

        // En-têtes du tableau (Labels)
        table.addCell(new Cell().add(new Paragraph("Transaction ID:").setFont(fontBold)));
        table.addCell(new Cell().add(new Paragraph(t.getTransactionId()).setFont(fontNormal)));

        table.addCell(new Cell().add(new Paragraph("Montant:").setFont(fontBold)));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f", t.getAmount()) + " TND").setFont(fontNormal)));

        table.addCell(new Cell().add(new Paragraph("Date de la transaction:").setFont(fontBold)));
        table.addCell(new Cell().add(new Paragraph(t.getTransactionDate().toString()).setFont(fontNormal)));

        table.addCell(new Cell().add(new Paragraph("Statut:").setFont(fontBold)));
        table.addCell(new Cell().add(new Paragraph(t.getStatus()).setFont(fontNormal)));

        // Ajout du tableau au document
        document.add(table);

        // Ajouter une ligne de séparation pour l'esthétique
        document.add(new Paragraph("\n").setFont(fontNormal));  // Un peu d'espace
        document.add(new Paragraph("--------------------------------------------------------").setFont(fontNormal).setTextAlignment(TextAlignment.CENTER));

        // Un peu de padding avant la fin
        document.add(new Paragraph("\n").setFont(fontNormal));

        // Ajouter un message de remerciement
        document.add(new Paragraph("Merci de faire affaire avec nous !")
                .setFont(fontNormal)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));

        BufferedImage qrCodeImage = qrcodeGeneratorTransaction(t);

// Convertir BufferedImage en ImageData pour iText 7
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        ImageData imageData = ImageDataFactory.create(imageBytes);


        com.itextpdf.layout.element.Image qrCode = new com.itextpdf.layout.element.Image(imageData);
        qrCode.setWidth(100);
        qrCode.setHeight(100);


        document.add(qrCode);



        // Fermer le document
        document.close();

        // Ouvrir automatiquement le PDF une fois généré (facultatif)
        Desktop.getDesktop().open(new File(filePath));
    }
    private static BufferedImage qrcodeGeneratorTransaction(Transaction t) throws WriterException {
        String qrContent = "Transaction ID: " + t.getTransactionId() + "\n"
                + "Montant: " + t.getAmount() + " TND\n"
                + "Date: " + t.getTransactionDate() + "\n"
                + "Statut: " + t.getStatus();

        int width = 200;
        int height = 200;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int grayValue = (bitMatrix.get(x, y) ? 0 : 255);
                image.setRGB(x, y, (grayValue == 0 ? 0xFF000000 : 0xFFFFFFFF));
            }
        }

        return image;
    }
    public void gotodash(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/view/ApprenantDashboard.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
