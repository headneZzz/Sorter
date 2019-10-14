package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.Files;

import javafx.stage.DirectoryChooser;


public class Controller {
    @FXML
    private TextField path1;
    @FXML
    private TextField path2;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    protected void SortButtonClicked(ActionEvent event) throws IOException {
        copyFileUsingStream(new File(path1.getText()),new File(path2.getText()));
    }


    private static void copyFileUsingStream(File source, File dest) throws IOException {
        Files.copy(new File(source.toString()+"\\1.txt").toPath(),new File(dest.toString()+"\\1.txt").toPath());
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
