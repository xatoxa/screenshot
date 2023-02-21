module com.example.javafxdemo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;

    opens com.xatoxa.screenshot to javafx.fxml;
    exports com.xatoxa.screenshot;
}
