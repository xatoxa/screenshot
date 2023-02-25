module com.xatoxa.screenshot {
    requires javafx.controls;
    requires javafx.swing;

    requires java.desktop;

    requires com.github.kwhat.jnativehook;

    opens com.xatoxa.screenshot;
    exports com.xatoxa.screenshot;
}
