package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.nio.file.StandardCopyOption.*;

public class Controller implements Initializable {
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private TextField path1;
    @FXML
    private TextField path2;
    @FXML
    private Button cancel;
    @FXML
    private Button sort;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label errorLabel;

    private static int THREAD_COUNT = 20;

    private double k;
    private double count;

    private String url = "jdbc:postgresql://server:5433/archive";

    private Worker[] workers = new Worker[THREAD_COUNT];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try (Connection connection = DriverManager.getConnection(url, "admin", "adminus")) {
            System.out.println("Connected to database!");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Исполнители");
            while (resultSet.next()) {
                comboBox.getItems().addAll(resultSet.getString("Исполнитель") + " :" + resultSet.getInt("Код_исполнителя"));
            }
        } catch (SQLException e) {
            errorLabel.setText("Ошибка подлючения к базе данных");
            e.printStackTrace();
        }
    }

    class Worker extends Thread {
        private int threadId;

        private Worker(int id) {
            threadId = id;
        }

        @Override
        public void run() {
            System.out.println("Started thread:" + threadId);
            for (int i = threadId; i < filesName.size(); i += THREAD_COUNT) {
                if (filesName.get(i) != null)
                    try {
                        //Path to file in fonds
                        StringBuilder path = new StringBuilder(path2.getText());
                        String[] cat = filesName.get(i).getName().split("_");
                        for (int j = 0; j < cat.length - 1; j++)
                            path.append(File.separator).append(cat[j]);
                        File newFile = new File(path.toString() + File.separator + filesName.get(i).getName());

                        //Insert into db
                        Connection connection = DriverManager.getConnection(url, "admin", "adminus");
                        PreparedStatement prst = connection.prepareStatement("INSERT INTO digitization.Оцифровка VALUES(DEFAULT,?,?,?,?)");
                        String filesGetName = filesName.get(i).getName();
                        String pathToFile = newFile.toString().startsWith("\\\\server") ? "#I:" + newFile.toString().substring(8) + '#' : '#' + newFile.toString() + '#';
                        String[] executors = comboBox.getValue().split(":");
                        int executor = Integer.parseInt(executors[1]);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        prst.setString(1, filesGetName.substring(0, filesGetName.length() - 4));
                        prst.setString(2, pathToFile);
                        prst.setInt(3, executor);
                        prst.setDate(4, Date.valueOf(sdf.format(filesName.get(i).lastModified())));
                        prst.executeUpdate();

                        //Move file
                        new File(path.toString()).mkdirs();
                        Files.move(filesName.get(i).toPath(),
                                newFile.toPath(),
                                REPLACE_EXISTING);

                        System.out.println("Moved " + (i + 1) + " of " + filesName.size() + " " + filesName.get(i));
                        Worker.sleep(10);
                        synchronized (this) {
                            count += k;
                            progressBar.setProgress(count);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        System.out.println(threadId + " thread has been interrupted");
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
            }
            System.out.println("Thread " + threadId + " finished task!");
            sort.setDisable(false);
            cancel.setDisable(true);
        }
    }

    private static List<File> filesName = new ArrayList<>();

    private void listFilesForFolder(String directoryName) {
        filesName.clear();
        File directory = new File(directoryName);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile()) {
                filesName.add(file);
            } else if (file.isDirectory()) {
                listFilesForFolder(file.getAbsolutePath());
            }
        }
    }

    @FXML
    private void SortButtonClicked(ActionEvent event) {
        if (comboBox.getValue() != null) {
            try {
                errorLabel.setText("");
                listFilesForFolder(path1.getText());
                k = 1d / filesName.size();
                count = 0;
                progressBar.setProgress(0);
                cancel.setDisable(false);
                sort.setDisable(true);
                for (int i = 0; i < THREAD_COUNT; i++) {
                    Worker w = new Worker(i);
                    workers[i] = w;
                    w.start();
                }
            } catch (NullPointerException e) {
                errorLabel.setText("Укажите директорию");
            }
        } else errorLabel.setText("Укажите исполнителя");
    }

    @FXML
    private void CancelButtonClicked(ActionEvent event) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            workers[i].interrupt();
        }
        cancel.setDisable(true);
        sort.setDisable(false);
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    private void Path1ButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path1.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    private void Path2ButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path2.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
        }
    }
}