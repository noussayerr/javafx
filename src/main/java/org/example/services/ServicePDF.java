package org.example.services;

import org.example.entity.Evenement;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServicePDF {

    public static void generateEvenementPDF(String path, Evenement event) throws FileNotFoundException, MalformedURLException {
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);
        Document doc = new Document(pdf);
        doc.setMargins(50, 50, 50, 50);

        // Logo watermark
        URL logoUrl = ServicePDF.class.getResource("/images/logo.png");
        if (logoUrl != null) {
            ImageData imageData = ImageDataFactory.create(logoUrl);
            Image logo = new Image(imageData);
            float x = pdf.getDefaultPageSize().getWidth() / 2;
            float y = pdf.getDefaultPageSize().getHeight() / 2;
            logo.setFixedPosition(x - 200, y - 370);
            logo.setOpacity(0.08f);
            doc.add(logo);
        }

        // Document title
        Paragraph titre = new Paragraph("📋 Détails de l'Événement")
                .setFontSize(24)
                .setBold()
                .setFontColor(new DeviceRgb(26, 102, 255))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        doc.add(titre);

        // Separation line
        SolidLine solidLine = new SolidLine(1f);
        solidLine.setColor(new DeviceGray(0.6f));
        doc.add(new LineSeparator(solidLine).setMarginBottom(20));

        // Subtitle
        Paragraph sousTitre = new Paragraph("Informations essentielles sur l'événement")
                .setFontSize(12)
                .setFontColor(new DeviceGray(0.4f))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        doc.add(sousTitre);

        // Information table
        float[] colWidths = {150, 350};
        Table table = new Table(colWidths);
        table.setWidth(500);
        table.setMarginBottom(30);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addCell(getStyledCell("Nom", true));
        table.addCell(getStyledCell(event.getNom(), false));

        table.addCell(getStyledCell("Lieu", true));
        table.addCell(getStyledCell(event.getLieu(), false));

        table.addCell(getStyledCell("Date", true));
        table.addCell(getStyledCell(event.getDate().toString(), false));

        table.addCell(getStyledCell("Heure de début", true));
        table.addCell(getStyledCell(event.getHeureDebut().toString(), false));

        table.addCell(getStyledCell("Heure de fin", true));
        table.addCell(getStyledCell(event.getHeureFin().toString(), false));

        doc.add(table);

        // Dashed separator
        Table dashedSeparator = new Table(1);
        dashedSeparator.setWidth(500);
        dashedSeparator.setBorder(new DashedBorder(new DeviceGray(0.7f), 0.8f));
        dashedSeparator.setMarginBottom(20);
        doc.add(dashedSeparator);

        // Terms and conditions
        doc.add(new Paragraph("📝 Termes et conditions")
                .setFontSize(14)
                .setBold()
                .setFontColor(new DeviceGray(0.2f))
                .setMarginBottom(10));

        List<String> conditions = List.of(
                "1. L'événement peut être modifié sans préavis.",
                "2. Aucun remboursement ne sera effectué en cas d'annulation.",
                "3. La participation implique l'acceptation des conditions générales."
        );

        Table condBox = new Table(1);
        condBox.setWidth(500);
        condBox.setBackgroundColor(new DeviceGray(0.95f));
        condBox.setBorder(new SolidBorder(new DeviceGray(0.7f), 0.5f));

        for (String condition : conditions) {
            Paragraph p = new Paragraph(condition)
                    .setFontSize(10)
                    .setMargin(5)
                    .setFontColor(new DeviceGray(0.2f));
            condBox.addCell(new Cell().add(p).setBorder(Border.NO_BORDER));
        }
        doc.add(condBox);

        // Footer
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("📅 Généré le : " + getCurrentDate())
                .setFontSize(9)
                .setFontColor(new DeviceGray(0.4f))
                .setTextAlignment(TextAlignment.RIGHT));
        doc.add(new Paragraph("© CMC Tunisia – Tous droits réservés")
                .setFontSize(9)
                .setFontColor(new DeviceGray(0.4f))
                .setTextAlignment(TextAlignment.RIGHT));

        doc.close();
    }

    public static void generateRapportPDF(String path, String body) throws FileNotFoundException, MalformedURLException {
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);
        Document doc = new Document(pdf);
        doc.setMargins(50, 50, 50, 50);

        // Logo watermark
        URL logoUrl = ServicePDF.class.getResource("/images/logo.png");
        if (logoUrl != null) {
            ImageData imageData = ImageDataFactory.create(logoUrl);
            Image logo = new Image(imageData);
            float x = pdf.getDefaultPageSize().getWidth() / 2;
            float y = pdf.getDefaultPageSize().getHeight() / 2;
            logo.setFixedPosition(x - 200, y - 370);
            logo.setOpacity(0.08f);
            doc.add(logo);
        }

        // Document title
        Paragraph titre = new Paragraph("📊 Rapport de Connectivité Utilisateur")
                .setFontSize(24)
                .setBold()
                .setFontColor(new DeviceRgb(26, 102, 255))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        doc.add(titre);

        // Separation line
        SolidLine solidLine = new SolidLine(1f);
        solidLine.setColor(new DeviceGray(0.6f));
        doc.add(new LineSeparator(solidLine).setMarginBottom(20));

        // Subtitle
        Paragraph sousTitre = new Paragraph("Analyse de l'engagement et de l'activité de l'utilisateur")
                .setFontSize(12)
                .setFontColor(new DeviceGray(0.4f))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        doc.add(sousTitre);

        // Report content
        Table contentBox = new Table(1);
        contentBox.setWidth(500);
        contentBox.setBackgroundColor(new DeviceGray(0.95f));
        contentBox.setBorder(new SolidBorder(new DeviceGray(0.7f), 0.5f));
        contentBox.setMarginBottom(20);
        contentBox.setHorizontalAlignment(HorizontalAlignment.CENTER);

        Paragraph content = new Paragraph(body)
                .setFontSize(11)
                .setFontColor(new DeviceGray(0.2f))
                .setMargin(10)
                .setMultipliedLeading(1.2f);
        contentBox.addCell(new Cell().add(content).setBorder(Border.NO_BORDER));

        doc.add(contentBox);

        // Dashed separator
        Table dashedSeparator = new Table(1);
        dashedSeparator.setWidth(500);
        dashedSeparator.setBorder(new DashedBorder(new DeviceGray(0.7f), 0.8f));
        dashedSeparator.setMarginBottom(20);
        doc.add(dashedSeparator);

        // Notes section
        doc.add(new Paragraph("📝 Notes")
                .setFontSize(14)
                .setBold()
                .setFontColor(new DeviceGray(0.2f))
                .setMarginBottom(10));

        List<String> notes = List.of(
                "1. Les données sont basées sur l'activité enregistrée dans la plateforme.",
                "2. Les recommandations doivent être validées par un administrateur.",
                "3. Contactez le support pour toute question sur ce rapport."
        );

        Table notesBox = new Table(1);
        notesBox.setWidth(500);
        notesBox.setBackgroundColor(new DeviceGray(0.95f));
        notesBox.setBorder(new SolidBorder(new DeviceGray(0.7f), 0.5f));

        for (String note : notes) {
            Paragraph p = new Paragraph(note)
                    .setFontSize(10)
                    .setMargin(5)
                    .setFontColor(new DeviceGray(0.2f));
            notesBox.addCell(new Cell().add(p).setBorder(Border.NO_BORDER));
        }
        doc.add(notesBox);

        // Footer
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("📅 Généré le : " + getCurrentDate())
                .setFontSize(9)
                .setFontColor(new DeviceGray(0.4f))
                .setTextAlignment(TextAlignment.RIGHT));
        doc.add(new Paragraph("© CMC Tunisia – Tous droits réservés")
                .setFontSize(9)
                .setFontColor(new DeviceGray(0.4f))
                .setTextAlignment(TextAlignment.RIGHT));

        doc.close();
    }

    // Styled cell
    private static Cell getStyledCell(String text, boolean isBold) {
        Paragraph p = new Paragraph(text)
                .setFontSize(11)
                .setMargin(5);
        if (isBold)
            p.setBold().setFontColor(new DeviceRgb(26, 102, 255));
        return new Cell().add(p)
                .setBorder(new SolidBorder(new DeviceGray(0.8f), 0.5f))
                .setPadding(5);
    }

    public static String getCurrentDate() {
        LocalDate date = LocalDate.now();
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}