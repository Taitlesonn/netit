package com.netit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.util.Objects;


public class Main  extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Init
        Application_run_time user = new Application_run_time();
        user.setPrimary_stage(primaryStage);
        
        // Icon
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/files/Network-Angel.png")));
        primaryStage.getIcons().add(icon);

        // Załaduj plik FXML z folderu resources
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/first_ui.fxml")));

        Scene scene = new Scene(root, user.getY_run(), user.getX_run());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        primaryStage.setOnCloseRequest(e -> Topology.stop());


        primaryStage.setTitle("NETIT");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
