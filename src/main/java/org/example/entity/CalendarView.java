package org.example.entity;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;



public class CalendarView extends VBox {

    private YearMonth currentMonth;
    private VBox eventDisplayBox = new VBox();

    private final List<Evenement> evenements;
    private final GridPane calendarGrid = new GridPane();
    private final VBox eventDetailsBox = new VBox();

    public CalendarView(List<Evenement> evenements) {
        this.evenements = evenements;
        this.currentMonth = YearMonth.now();
        getStyleClass().add("calendar-root");
        buildCalendar();
        this.getChildren().add(eventDisplayBox);
    }

    private void buildCalendar() {
        this.getChildren().clear();
        eventDetailsBox.getChildren().clear();
        eventDisplayBox.getChildren().clear();


        HBox header = new HBox(10);
        header.getStyleClass().add("calendar-header");

        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.setStyle("""
    -fx-background-color: linear-gradient(to right, #fce4ec, #e1bee7, #d1c4e9);
    -fx-background-radius: 15;
    -fx-border-radius: 15;
    -fx-text-fill: #6a1b9a;
    -fx-font-weight: bold;
    -fx-font-size: 14px;
    -fx-padding: 6 12;
    -fx-cursor: hand;
""");

        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        monthBox.getItems().addAll(monthNames);
        monthBox.getSelectionModel().select(currentMonth.getMonthValue() - 1);

        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.setStyle("""
    -fx-background-color: linear-gradient(to right, #fce4ec, #e1bee7, #d1c4e9);
    -fx-background-radius: 15;
    -fx-border-radius: 15;
    -fx-text-fill: #6a1b9a;
    -fx-font-weight: bold;
    -fx-font-size: 14px;
    -fx-padding: 6 12;
    -fx-cursor: hand;
""");
        for (int y = 2020; y <= 2030; y++) yearBox.getItems().add(y);
        yearBox.getSelectionModel().select((Integer) currentMonth.getYear());

        monthBox.setOnAction(e -> {
            currentMonth = YearMonth.of(yearBox.getValue(), monthBox.getSelectionModel().getSelectedIndex() + 1);
            buildCalendar();
        });
        yearBox.setOnAction(e -> {
            currentMonth = YearMonth.of(yearBox.getValue(), monthBox.getSelectionModel().getSelectedIndex() + 1);
            buildCalendar();
            this.getChildren().add(eventDisplayBox);
            eventDisplayBox.setAlignment(Pos.CENTER);
            eventDisplayBox.setSpacing(10);
            eventDisplayBox.setPadding(new Insets(10));

        });

        header.getChildren().addAll(monthBox, yearBox);
        this.getChildren().add(header);

        // 🔹 Calendrier
        calendarGrid.setGridLinesVisible(false);
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.setPadding(new Insets(10));
        calendarGrid.setStyle("""
    -fx-background-color: linear-gradient(to bottom right, #fce4ec, #e1bee7); 
    -fx-padding: 10;
    -fx-border-radius: 15;
    -fx-background-radius: 15;
""");


        LocalDate firstDay = currentMonth.atDay(1);
        int startIndex = firstDay.getDayOfWeek().getValue() % 7;
        int totalDays = currentMonth.lengthOfMonth();

        int row = 0, col = startIndex;
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = currentMonth.atDay(day);
            VBox cell = createDayCell(date);
            calendarGrid.add(cell, col, row);
            playCardEntranceAnimation(cell, day * 15);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        this.getChildren().add(calendarGrid);

        eventDetailsBox.setSpacing(8);
        eventDetailsBox.setPadding(new Insets(12, 0, 0, 0));
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox();
        cell.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #ce93d8;
        -fx-border-width: 1.5px;
        -fx-background-radius: 10;
        -fx-border-radius: 10;
        -fx-effect: dropshadow(gaussian, rgba(179,136,235,0.2), 5, 0.3, 0, 2);
    """);

        cell.setPrefSize(90, 70);
        cell.setSpacing(5);
        cell.setPadding(new Insets(6));
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cell);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });

        cell.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cell);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });



        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        List<Evenement> eventsForDate = evenements.stream()
                .filter(e -> date.equals(e.getDate()))
                .collect(Collectors.toList());

        cell.getChildren().add(dayNumber); // Toujour afficher le jour

        if (!eventsForDate.isEmpty()) {
            // Gradient violet
            cell.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #d7bfff, #b3d9ff);
            -fx-background-radius: 10; 
            -fx-border-radius: 10;
        """);

            Label eventSummary = new Label("📅 " + eventsForDate.size() + " événement" + (eventsForDate.size() > 1 ? "s" : ""));
            eventSummary.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            cell.getChildren().add(eventSummary);

            Tooltip.install(cell, new Tooltip(eventsForDate.stream()
                    .map(Evenement::getNom)
                    .collect(Collectors.joining("\n"))));

            cell.setOnMouseClicked(e -> {
                eventDisplayBox.getChildren().clear(); // Nettoyer avant d’ajouter

                Label title = new Label("📌 Événements du " + date);
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #9C27B0;");
                eventDisplayBox.getChildren().add(title);

                for (Evenement ev : eventsForDate) {
                    Label evLabel = new Label("• " + ev.getNom() + " à " + ev.getLieu());
                    evLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b388eb;");
                    eventDisplayBox.getChildren().add(evLabel);
                }
            });
        }

        return cell;
    }

    private void playCardEntranceAnimation(Node card, int delayMillis) {
        Timeline animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(card.opacityProperty(), 0),
                        new KeyValue(card.scaleXProperty(), 0.85),
                        new KeyValue(card.scaleYProperty(), 0.85)
                ),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(card.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(card.scaleYProperty(), 1, Interpolator.EASE_OUT)
                )
        );
        animation.setDelay(Duration.millis(delayMillis));
        animation.play();
    }

}
