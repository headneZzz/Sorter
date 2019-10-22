package org.openjfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private ProgressBar progressBar;

    private static int THREAD_COUNT = 20;

    private double k;
    private double count;

    private Worker[] workers = new Worker[THREAD_COUNT];

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
                        String[] cat = filesName.get(i).getName().split("-");
                        StringBuilder path = new StringBuilder(path2.getText());
                        for (int j = 0; j < cat.length - 1; j++)
                            path.append(File.separator).append(cat[j]);

                        new File(path.toString()).mkdirs();
                        Files.copy(filesName.get(i).toPath(),
                                new File(path.toString() + File.separator + filesName.get(i).getName()).toPath(),
                                REPLACE_EXISTING); // need change to move
                        Worker.sleep(50);
                        synchronized (this) {
                            count += k;
                            progressBar.setProgress(count);
                        }
                        System.out.println("Moved " + (i + 1) + " of " + filesName.size() + " " + filesName.get(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        System.out.println(threadId + " thread has been interrupted");
                        break;
                    }
            }
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
    protected void SortButtonClicked(ActionEvent event) {
        if (path1.getText() != null & path2.getText() != null) {
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
        }
    }

    @FXML
    protected void CancelButtonClicked(ActionEvent event) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            workers[i].interrupt();
        }
        cancel.setDisable(true);
        sort.setDisable(false);
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    protected void Path1ButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path1.setText(dir.getAbsolutePath());
        } catch (NullPointerException e){
            System.out.println("Specify a directory");
        }
    }

    @FXML
    protected void Path2ButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path2.setText(dir.getAbsolutePath());
        } catch (NullPointerException e) {
            System.out.println("Specify a directory");
        }

    }
}