package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

import javafx.stage.DirectoryChooser;


public class Controller {
    @FXML
    private TextField path1;
    @FXML
    private TextField path2;
    @FXML
    private Button cancel;
    @FXML
    private Button sort;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    protected void SortButtonClicked(ActionEvent event) throws IOException {
        cancel.setDisable(false);
        sort.setDisable(true);
        copyFiles(new File(path1.getText()), new File(path2.getText()));
    }

    @FXML
    protected void CancelButtonClicked(ActionEvent event) {
        cancel.setDisable(true);
        sort.setDisable(false);
    }


    private static void copyFiles(File source, File dest) throws IOException {
        Stream<Path> files = Files.list(source.toPath());
        for (int i = 0; i < files.count(); i++) {

        }
        String[] parts = source.toString().split("_");
        Files.copy(new File(source.toString() + "\\1.txt").toPath(), new File(dest.toString() + "\\1.txt").toPath());
    }

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
