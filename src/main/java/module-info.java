module Project.main {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens sample to javafx.fxml;
    exports sample;
}