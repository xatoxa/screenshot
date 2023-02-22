module com.xatoxa.screenshot {
    requires javafx.controls;
    requires javafx.swing;

    requires java.desktop;

    opens com.xatoxa.screenshot;
    exports com.xatoxa.screenshot;
}
