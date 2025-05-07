package org.example.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private final String URL = "jdbc:mysql://localhost:3306/integrationpidev";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    private Connection connection;

    private static MyDatabase instance;

    private MyDatabase() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion à la base de données établie !");
        } catch (SQLException e) {
            System.err.println("❌ Échec de connexion : " + e.getMessage());
            connection = null; // ⚠️ très important
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("⚠️ Connexion fermée. Tentative de reconnexion...");
                connect();
            }
            if (connection == null || connection.isClosed()) {
                throw new SQLException("🔴 Connexion non disponible après tentative de reconnexion.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connexion invalide : " + e.getMessage());
        }
        return connection;
    }

    public static class QRCodeGenerator {
        public static void generateQRCode(String data, String filePath) throws WriterException, IOException {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);
            Path path = Paths.get(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        }
    }
}
