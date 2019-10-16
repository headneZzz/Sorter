package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.*;
import java.util.stream.*;
import java.util.*;

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
        //copyFiles(new File(path1.getText()), new File(path2.getText()));
    }

    @FXML
    protected void CancelButtonClicked(ActionEvent event) {
        cancel.setDisable(true);
        sort.setDisable(false);
    }


    private void copyFiles(File source, File dest) throws IOException {
        //Stream<Path> files = Files.list(source.toPath());
        LinkedList<Files> files = new LinkedList<>();
        //Files.list(source.toPath()).forEach((files.add);
        String[] parts = source.getName().split("_");
        Files.copy(new File(source.toString() + files.listIterator()).toPath(), new File(dest.toString() + parts[0] + parts[1] + parts[2] + source.getName()).toPath());
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
