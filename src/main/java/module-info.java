module Project.main {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens sample to javafx.fxml;
    exports sample;
}
