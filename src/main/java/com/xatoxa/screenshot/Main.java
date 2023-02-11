package com.xatoxa.screenshot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

        //для другого экрана
        Stage anotherStage = new Stage();

        //Listener left click XD
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    Platform.runLater(() -> {
                        if (stateWindow == 1) {
                            primaryStage.hide();
                            anotherStage.hide();
                            stateWindow = 0;
                        } else if (stateWindow == 0) {
                            primaryStage.show();
                            anotherStage.show();
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

        ObservableList<Screen> screens = Screen.getScreens();


        Rectangle2D bounds = screens.get(0).getVisualBounds();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

        scene.setFill(Color.RED);

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOpacity(0.2);
        primaryStage.setScene(scene);


        bounds = screens.get(1).getVisualBounds();

        root = new StackPane();
        scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        scene.setFill(Color.RED);
        anotherStage.setX(bounds.getMinX());
        anotherStage.setY(bounds.getMinY());

        anotherStage.initStyle(StageStyle.TRANSPARENT);
        anotherStage.setOpacity(0.2);
        anotherStage.setScene(scene);

        Platform.setImplicitExit(false);
    }
}
