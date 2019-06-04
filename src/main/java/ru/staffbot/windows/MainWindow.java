package ru.staffbot.windows;

import javafx.application.Application;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.webserver.WebServer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

public class MainWindow extends Application{

    private static String title;

    public static void init(String windowTilte){
        title = windowTilte;
        Journal.add("Главное окно приложения открыто");
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
            Parent root = (new FXMLLoader()).load(new ByteArrayInputStream(
                getClass().getResourceAsStream("/fxml/MainWindow.fxml").readAllBytes()));
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/img/icon.png")));
            stage.setOnCloseRequest((WindowEvent event1) -> {
                Journal.add("Close application by GUI");
                System.exit(0);
            });
            stage.show();
        } catch (Exception exception) {
            Journal.add(exception.getMessage());
            System.exit(0);
        }
    }

    @FXML
    private void manageButtonClick(ActionEvent event) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI("http://localhost:"+ WebServer.PORT));
        } catch (Exception exception) {
            Journal.add("Неудачная попытка открыть браузер", NoteType.ERROR);
        }

    }

}
