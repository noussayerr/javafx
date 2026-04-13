package spring.boot.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class ForumApplication extends Application {
    
    private ApplicationContext springContext;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger l'interface FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/spring/boot/ui/views/post.fxml"));
        Parent root = loader.load();
        
        // Créer la scène
        Scene scene = new Scene(root, 1200, 700);
        
        // Configurer la fenêtre principale
        primaryStage.setTitle("E-Sport Forum - Gestion des Posts");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        if (springContext != null) {
            SpringApplication.exit(springContext);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

