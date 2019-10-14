package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }

    static public Stage getPrimaryStage() {
        return Main.primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Pic2Fold");
        primaryStage.setScene(new Scene(root, 500, 250));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
