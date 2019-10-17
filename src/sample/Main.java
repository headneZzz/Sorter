package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {
    private static Stage primaryStage;

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }

    static Stage getPrimaryStage() {
        return Main.primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("1.png")));
        primaryStage.setTitle("Sorter");
        primaryStage.setScene(new Scene(root, 500, 250));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}