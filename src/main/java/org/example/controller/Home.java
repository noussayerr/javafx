package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

public class Home {

    @FXML
    private WebView chatbotWebView;

    @FXML
    public void initialize() {
        // Charger directement le chatbot
        //String htmlContent = "<!DOCTYPE html>\n" +
        // "<html lang=\"fr\">\n" +
        //       "<head>\n" +
        //      "    <meta charset=\"UTF-8\">\n" +
        //      "    <title>Chatbot</title>\n" +
        //        "    <style>\n" +
        //       "        body { margin: 0; padding: 0; }\n" +
        //       "    </style>\n" +
        //       "</head>\n" +
        //       "<body>\n" +
        //       "<script>\n" +
        //       "    (function(){\n" +
        //       "        if(!window.chatbase || window.chatbase(\"getState\") !== \"initialized\") {\n" +
        //    "            window.chatbase = function() {\n" +
                        //       "                if(!window.chatbase.q) { window.chatbase.q = []; }\n" +
        //       "                window.chatbase.q.push(arguments);\n" +
        //       "            };\n" +
        //       "        }\n" +
                        //        "        const script = document.createElement(\"script\");\n" +
        //       "        script.src = \"https://www.chatbase.co/embed.min.js\";\n" +
        //       "        script.id = \"mvIwJ4lajfms0zYw9GuZk\";\n" +
        //       "        script.setAttribute(\"chatbotId\", \"votre-id-chatbot\");\n" +
        //       "        script.setAttribute(\"domain\", \"www.chatbase.co\");\n" +
        //       "        document.body.appendChild(script);\n" +
                        //      "    })();\n" +
        //       "</script>\n" +
        //       "</body>\n" +
        //      "</html>";

        //chatbotWebView.getEngine().loadContent(htmlContent);

        // Configuration supplémentaire si nécessaire
        //chatbotWebView.setContextMenuEnabled(false);
        //chatbotWebView.getEngine().setJavaScriptEnabled(true);
    }


    public void goToRegister(ActionEvent event) {
        loadPage(event, "/org/example/view/choix.fxml");
    }

    public void goToLogin(ActionEvent event) {
        loadPage(event, "/org/example/view/login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur (peut-être afficher un message à l'utilisateur)
        }
    }
}
