package org.example.utils;


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
        connect(); // initialisation directe
    }

    // ✅ Méthode de connexion robuste
    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion à la base de données établie !");
        } catch (SQLException e) {
            System.err.println("❌ Échec de la connexion à la base de données : " + e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    // ✅ Vérifie que la connexion est toujours valide
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("⚠️ Connexion fermée. Tentative de reconnexion...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la connexion : " + e.getMessage());
        }
        return connection;
    }
}
