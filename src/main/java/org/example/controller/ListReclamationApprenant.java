package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.entity.Apprenant;
import org.example.entity.Reclamation;
import org.example.entity.User;
import org.example.services.ServiceReclamation;
import org.example.services.ServiceTransaction;
import org.example.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListReclamationApprenant implements Initializable {
    @FXML
    private TableView<Reclamation> tableReclamations;
    @FXML
    private TableColumn<Reclamation, String> colTitre;
    @FXML
    private TableColumn<Reclamation, String> colDescription;
    @FXML
    private TableColumn<Reclamation, String> colEtat;

    private final ServiceReclamation serviceReclamation = new ServiceReclamation();
    private final ServiceTransaction serviceTransaction = new ServiceTransaction();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        int apprenantId;

        try {
            Apprenant currentApprenant = serviceTransaction.getApprenantById(currentUser.getId());
            apprenantId = currentApprenant.getId();

            List<Reclamation> toutes = serviceReclamation.afficher();
            List<Reclamation> reclamationsFiltrees = toutes.stream()
                    .filter(rec -> rec.getApprenant() != null && rec.getApprenant().getId() == apprenantId)
                    .collect(Collectors.toList());

            tableReclamations.getItems().addAll(reclamationsFiltrees);

        } catch (SQLException e) {
            e.printStackTrace();
            // Optionnel : afficher une alerte utilisateur
        }
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

