package com.joel.lectorcodigo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CodeBarApp extends Application {

    public static Stage stageApp;

    public static Stage getStageApp() {
        return stageApp;
    }

    @Override
    public void start(Stage stage) throws IOException {

        Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN);
        //Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        //Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(CodeBarApp.class.getResource("codebar.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        stage.setTitle("ZBarcode");
        stage.getIcons().add(new Image(Objects.requireNonNull(CodeBarApp.class.getResourceAsStream("/barcode.png"))));
        stage.setScene(scene);
        stage.setOnCloseRequest(windowEvent -> {
            //TODO: persistir lo q queda en la db y cerrar la db
        });
        stage.show();
        stageApp = stage;

    }

    public static void main(String[] args) {
        launch();
    }
}