package com.xatoxa.screenshot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    int stateWindow = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        URL url = System.class.getResource("/image/icon.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);

        final TrayIcon trayIcon = new TrayIcon(image, "Скриншот");

        final SystemTray tray = SystemTray.getSystemTray();

        //поддержка нескольких экранов
        ObservableList<Screen> screens = Screen.getScreens();
        List<Stage> stages = new ArrayList<>();

        for (int i = 0; i < screens.size(); i++){
            Rectangle2D bounds = screens.get(i).getVisualBounds();
            StackPane root = new StackPane();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            scene.setFill(Color.color(0f, 0f, 0f, 0.1));

            Rectangle screenRect = new Rectangle();
            //scene listeners
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                    event -> {
                screenRect.setLocation((int) event.getScreenX(), (int) event.getScreenY());
                System.out.println("screenX: " + (int)event.getScreenX() + "; screenY: " + (int)event.getScreenY());
                    });

            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                    event -> {
                screenRect.setSize(
                        (int) (event.getScreenX() - screenRect.x),
                        (int) (event.getScreenY() - screenRect.y));
                System.out.println("screenX: " + event.getScreenX() + "; screenY: " + event.getScreenY());

                BufferedImage capture = null;
                try {
                    capture = new Robot().createScreenCapture(screenRect);
                } catch (AWTException e) {
                    throw new RuntimeException(e);
                }
                File imageFile = new File("screenshot.png");
                try {
                    ImageIO.write(capture, "png", imageFile );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                });

            Stage stage;
            if (i == 0){
                stage = primaryStage;
            }else {
                stage = new Stage();
            }
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setOpacity(0.2);
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());

            stage.setScene(scene);

            stages.add(stage);
        }

        //listener для иконки в трее
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    Platform.runLater(() -> {
                        if (stateWindow == 1) {
                            stages.forEach(Stage::hide);
                            stateWindow = 0;
                        } else if (stateWindow == 0) {
                            stages.forEach(Stage::show);
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
