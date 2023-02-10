package com.xatoxa.screenshot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class Main extends Application {
    int stateWindow = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        URL url = System.class.getResource("/image/icon.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);

        //image dimensions must be 16x16 on windows, works for me
        final TrayIcon trayIcon = new TrayIcon(image, "Скриншот");

        final SystemTray tray = SystemTray.getSystemTray();

        //Listener left click XD
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    Platform.runLater(() -> {
                        if (stateWindow == 1) {
                            primaryStage.hide();
                            stateWindow = 0;
                        } else if (stateWindow == 0) {
                            primaryStage.show();
                            stateWindow = 1;
                        }
                    });
                }

            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        Platform.setImplicitExit(false);
    }
}
