package org.example.controller;

import org.example.dao.CategorieDAO;
import org.example.dao.EvenementDAO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.time.Month;


public class StatistiquesController {
    @FXML private FlowPane legendPane;

    @FXML private Label loadingIcon;
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML private Label lblTotalCategories;
    @FXML private Label lblCategoriePlusUtilisee;

    private final CategorieDAO categorieDAO = new CategorieDAO();
    private final EvenementDAO evenementDAO = new EvenementDAO();

    @FXML
    public void initialize() {
        setupAnimations();
        loadStatistics();
        barChart.setCategoryGap(20); // Espace entre les catégories
        barChart.setBarGap(10);      // Espace entre les barres
    }

    private void setupAnimations() {
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), loadingIcon);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        loadingIcon.visibleProperty().addListener((obs, old, visible) -> {
            if (visible) rotate.play(); else rotate.stop();
        });
    }

    private void loadStatistics() {
        loadingIcon.setVisible(true);
        new Thread(() -> {
            Map<String, Integer> repartition = categorieDAO.getRepartitionCategories();
            List<LocalDate> dates = evenementDAO.getDatesEvenements();
            Platform.runLater(() -> {
                afficherNombreTotalCategories();
                afficherCategoriePlusUtilisee();
                afficherHistogrammeEvenementsParMois(dates);
                afficherPieChartEvenementsParCategorie(repartition);
                loadingIcon.setVisible(false);
            });
        }).start();
    }

    private void afficherNombreTotalCategories() {
        int total = categorieDAO.getNombreTotalCategories();
        lblTotalCategories.setText(String.valueOf(total));
    }

    private void afficherCategoriePlusUtilisee() {
        lblCategoriePlusUtilisee.setText(categorieDAO.getCategorieLaPlusUtilisee());
    }

    private void afficherHistogrammeEvenementsParMois(List<LocalDate> dates) {
        barChart.getData().clear();
        Map<Integer, Integer> moisCounts = new TreeMap<>();
        for (int i = 1; i <= 12; i++) {
            moisCounts.put(i, 0);
        }

        for (LocalDate date : dates) {
            int mois = date.getMonthValue();
            moisCounts.put(mois, moisCounts.get(mois) + 1);
        }

        String[] moisFrancais = {
                "janvier", "février", "mars", "avril", "mai", "juin",
                "juillet", "août", "septembre", "octobre", "novembre", "décembre"
        };

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<Integer, Integer> entry : moisCounts.entrySet()) {
            int mois = entry.getKey();
            int count = entry.getValue();
            if (count > 0) {
                series.getData().add(new XYChart.Data<>(moisFrancais[mois - 1], count));
            }
        }

        barChart.getData().add(series);
        styleChartBars(series);

        // ✅ Correction de l'affichage des mois superposés
        CategoryAxis xAxis = (CategoryAxis) barChart.getXAxis();
        xAxis.setTickLabelRotation(45); // 45° pour lisibilité
        xAxis.setTickLabelGap(10);
        xAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 12));
    }


    private void afficherPieChartEvenementsParCategorie(Map<String, Integer> repartition) {
        pieChart.getData().clear();
        repartition.forEach((label, value) -> {
            PieChart.Data data = new PieChart.Data(label, value);
            pieChart.getData().add(data);
            data.getNode().setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), data.getNode());
                st.setToX(1.1); st.setToY(1.1); st.play();
            });
            data.getNode().setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), data.getNode());
                st.setToX(1.0); st.setToY(1.0); st.play();
            });
        });
        createInteractiveLegend(repartition);
    }
    private void styleChartBars(XYChart.Series<String, Number> series) {
        String[] colors = {"#6a11cb", "#2575fc", "#8ed2e2", "#a40000", "#1a2a6c", "#b21f1f"};

        for (XYChart.Data<String, Number> data : series.getData()) {
            int index = series.getData().indexOf(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + colors[index % colors.length] + ";");

                    // Animation au survol
                    newNode.setOnMouseEntered(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), newNode);
                        st.setToX(1.05);
                        st.setToY(1.05);
                        st.play();
                    });

                    newNode.setOnMouseExited(e -> {
                        ScaleTransition st = new ScaleTransition(Duration.millis(200), newNode);
                        st.setToX(1.0);
                        st.setToY(1.0);
                        st.play();
                    });
                }
            });
        }
    }


    private void createInteractiveLegend(Map<String, Integer> repartition) {
        legendPane.getChildren().clear();
        String[] colors = {"#FF6B6B", "#4D96FF", "#6BCB77", "#C084FC", "#FF8D3D", "#00B8A9"};
        int total = repartition.values().stream().mapToInt(i -> i).sum();
        int i = 0;
        for (Map.Entry<String, Integer> entry : repartition.entrySet()) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            Rectangle colorBox = new Rectangle(15, 15, Color.web(colors[i % colors.length]));
            Label label = new Label(String.format("%s (%.1f%%)", entry.getKey(), entry.getValue() * 100.0 / total));
            item.getChildren().addAll(colorBox, label);
            legendPane.getChildren().add(item);
            i++;
        }
    }
}
