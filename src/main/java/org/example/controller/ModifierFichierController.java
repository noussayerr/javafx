package org.example.controller;

import javafx.scene.control.Alert;
import org.example.entity.Fichier;
import org.example.services.ServiceFichier;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModifierFichierController {

    @FXML private TextField nomFichierField;
    @FXML private TextField urlFichierField;
    @FXML private TextField typeFichierField;

    private Fichier fichier;
    private AfficherCoursFrontController.RefreshListener fichierModifieListener;
    private final ServiceFichier serviceFichier = new ServiceFichier();

    public void setFichier(Fichier fichier) {
        this.fichier = fichier;
        if (fichier != null) {
            nomFichierField.setText(fichier.getNomF());
            urlFichierField.setText(fichier.getUrlF());
            typeFichierField.setText(fichier.getType());
        }
    }

    public void setFichierModifieListener(AfficherCoursFrontController.RefreshListener listener) {
        this.fichierModifieListener = listener;
    }

    @FXML
    private void modifierFichier() {
        if (fichier == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun fichier sélectionné", "");
            return;
        }

        try {
            fichier.setNomF(nomFichierField.getText().trim());
            fichier.setUrlF(urlFichierField.getText().trim());
            fichier.setType(typeFichierField.getText().trim());
            serviceFichier.modifier(fichier);

            if (fichierModifieListener != null) {
                fichierModifieListener.refresh();
            }

            Stage stage = (Stage) nomFichierField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Modification échouée", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}