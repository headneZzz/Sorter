package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.*;

public class Controller {
    @FXML
    private TextField path1;
    @FXML
    private TextField path2;
    @FXML
    private Button cancel;
    @FXML
    private Button sort;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressBar progressBar;

    private static String[] filesName;
    private static int THREAD_COUNT = 4;

    class Worker extends Thread {
        private int threadId;

        private Worker(int id) {
            threadId = id;
        }

        @Override
        public void run() {
            System.out.println("Started thread:" + threadId);
            for (int i = threadId; i < filesName.length; i += THREAD_COUNT) {
                if (filesName[i] != null)
                    try {
                        String[] cat = filesName[i].split("-");
                        StringBuilder path = new StringBuilder(path2.getText());
                        for (int j = 0; j < cat.length - 1; j++) {
                            path.append(File.separator).append(cat[j]);
                        }
                        new File(path.toString()).mkdirs();
                        Files.copy(new File(path1.getText() + File.separator + filesName[i]).toPath(),
                                new File(path.toString() + File.separator + filesName[i]).toPath(),
                                REPLACE_EXISTING);
                        progressBar.setProgress(i * 100.0 / filesName.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            sort.setDisable(false);
            cancel.setDisable(true);
        }
    }

    private void listFilesForFolder() {
        int filesCount = 0;
        try (Stream<Path> files = Files.list(Paths.get(path1.getText()))) {
            filesCount = (int) files.count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        filesName = new String[filesCount];
        File directory = new File(path1.getText());
        int i = 0;
        for (final File fileEntry : Objects.requireNonNull(directory.listFiles())) {
            if (fileEntry.isFile()) {
                filesName[i] = fileEntry.getName();
            }
            i++;
        }
    }

    @FXML
    protected void SortButtonClicked(ActionEvent event) {
        progressBar.setProgress(0);
        cancel.setDisable(false);
        sort.setDisable(true);
        listFilesForFolder();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Worker w = new Worker(i);
            w.start();
        }
    }

    @FXML
    protected void CancelButtonClicked(ActionEvent event) {
        cancel.setDisable(true);
        sort.setDisable(false);
        progressBar.setProgress(0);
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    protected void Path1ButtonClicked(ActionEvent event) {
        File dir = directoryChooser.showDialog(Main.getPrimaryStage());
        path1.setText(dir.getAbsolutePath());
    }

    @FXML
    protected void Path2ButtonClicked(ActionEvent event) {
        File dir = directoryChooser.showDialog(Main.getPrimaryStage());
        path2.setText(dir.getAbsolutePath());
    }
}
