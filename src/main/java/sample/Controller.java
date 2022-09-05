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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller implements Initializable {
    private static final int THREAD_COUNT = 20;
    private static final List<File> FILES = new ArrayList<>();
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    @FXML
    private ComboBox<String> comboBoxExecutors;
    @FXML
    private ComboBox<String> comboBoxPath1;
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
    private double k;
    private double count;
    private final Worker[] workers = new Worker[THREAD_COUNT];
    private final Service service = new Service();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String url = service.getPropertyValue(PropertyName.DB_URL);
        String user = service.getPropertyValue(PropertyName.DB_USER);
        String pass = service.getPropertyValue(PropertyName.DB_PASSWORD);
        try (Connection connection = DriverManager.getConnection(url, user, pass); Statement statement = connection.createStatement()) {
            System.out.println("Connected to database!");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Исполнители");
            while (resultSet.next()) {
                comboBoxExecutors.getItems().addAll(resultSet.getString("Исполнитель") + " :" + resultSet.getInt("Код_исполнителя"));
            }
        } catch (SQLException e) {
            errorLabel.setText("Ошибка подлючения к базе данных");
            e.printStackTrace();
        }
        comboBoxPath1.getItems().addAll(
                "I:\\Оцифровка\\Гимодудинов",
                "I:\\Оцифровка\\КолесниковаЕ",
                "I:\\Оцифровка\\Степаненко",
                "I:\\Оцифровка\\Фото");
    }

    private void listFilesForFolder(String directoryName) {
        FILES.clear();
        File directory = new File(directoryName);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile() && (file.toString().endsWith(".JPG") || file.toString().endsWith(".jpg"))) {
                FILES.add(file);
            } else if (file.isDirectory()) {
                listFilesForFolder(file.getAbsolutePath());
            }
        }
    }

    @FXML
    private void sortButtonClicked(ActionEvent event) {
        if (comboBoxExecutors.getValue() != null) {
            try {
                errorLabel.setText("");
                listFilesForFolder(comboBoxPath1.getValue());
                k = 1d / FILES.size();
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
        } else {
            errorLabel.setText("Укажите исполнителя");
        }
    }

    @FXML
    private void cancelButtonClicked(ActionEvent event) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            workers[i].interrupt();
        }
        cancel.setDisable(true);
        sort.setDisable(false);
    }

    @FXML
    private void path1ButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            comboBoxPath1.getItems().add(dir.getAbsolutePath());
            comboBoxPath1.setValue(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
            //
        }
    }

    @FXML
    private void path2ButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path2.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
            //
        }
    }

    class Worker extends Thread {
        private final int threadId;

        private Worker(int id) {
            threadId = id;
        }

        @Override
        public void run() {
            System.out.println("Started thread:" + threadId);
            String url = service.getPropertyValue(PropertyName.DB_URL);
            String user = service.getPropertyValue(PropertyName.DB_USER);
            String pass = service.getPropertyValue(PropertyName.DB_PASSWORD);
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                for (int i = threadId; i < FILES.size(); i += THREAD_COUNT) {
                    if (FILES.get(i) != null) {
                        if (processFile(connection, i)) {
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("Thread " + threadId + " finished task!");
            sort.setDisable(false);
            cancel.setDisable(true);
        }

        private boolean processFile(Connection connection, int i) throws SQLException {
            try (PreparedStatement prst = connection.prepareStatement("INSERT INTO digitization.Оцифровка VALUES(DEFAULT,?,?,?,?)")) {
                //Path to file in fonds
                StringBuilder path = new StringBuilder(path2.getText());
                String[] cat = FILES.get(i).getName().split("_");
                for (int j = 0; j < cat.length - 1; j++)
                    path.append(File.separator).append(cat[j]);
                File newFile = new File(path + File.separator + FILES.get(i).getName());

                //Insert into db
                String filesGetName = FILES.get(i).getName();
                String pathToFile = newFile.toString().startsWith("\\\\server") ? "#I:" + newFile.toString().substring(8) + '#' : '#' + newFile.toString() + '#';
                String[] executors = comboBoxExecutors.getValue().split(":");
                int executor = Integer.parseInt(executors[1]);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                prst.setString(1, filesGetName.substring(0, filesGetName.length() - 4));
                prst.setString(2, pathToFile);
                prst.setInt(3, executor);
                prst.setDate(4, Date.valueOf(sdf.format(FILES.get(i).lastModified())));
                prst.executeUpdate();

                //Move file
                new File(path.toString()).mkdirs();
                Files.move(FILES.get(i).toPath(),
                        newFile.toPath(),
                        REPLACE_EXISTING);

                System.out.println("Moved " + (i + 1) + " of " + FILES.size() + " " + FILES.get(i));
                Thread.sleep(10);
                synchronized (this) {
                    count += k;
                    progressBar.setProgress(count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println(threadId + " thread has been interrupted");
                return true;
            }
            return false;
        }
    }
}
