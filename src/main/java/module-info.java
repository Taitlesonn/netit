module com.netit {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires tomlj; // zamiast org.tomlj

    opens com.netit to javafx.fxml;
    exports com.netit;
}
