package com.starterkit.javafx;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Startup extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {

        String langCode = getParameters().getNamed().get("lang");
        if (langCode != null && !langCode.isEmpty()) {
            Locale.setDefault(Locale.forLanguageTag(langCode));
        }
        
        String fxmlFile = "/com/starterkit/javafx/view/viewer.fxml";
        String bundlePath = "com/starterkit/javafx/bundle/base";
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile), ResourceBundle.getBundle(bundlePath));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/com/starterkit/javafx/css/standard.css");

        stage.setTitle("Images viewer");
        stage.setScene(scene);
        stage.show();
    }
}
