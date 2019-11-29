package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        Parent root = FXMLLoader.load(getClass().getResource("/sample/sample.fxml"));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/sample/1.png")));
        primaryStage.setTitle("Sorter");
        primaryStage.setScene(new Scene(root, 500, 250));
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }


    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://server:5433/archive", "admin", "adminus")) {
            System.out.println("Connected to database!");
            Statement statement = connection.createStatement();
            System.out.println("Reading records...");
            System.out.printf("%-30.30s  %-30.30s%n", "Код", "Исполнитель");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Исполнители");
            while (resultSet.next()) {
                System.out.printf("%-30.30s  %-30.30s%n", resultSet.getString("Код_исполнителя"), resultSet.getString("Исполнитель"));
            }

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        launch(args);
    }
}