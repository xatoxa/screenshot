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

        //получение stage для всех экранов
        List<Stage> stages = getStagesForAllScreens(primaryStage);

        //иконка для трея
        URL url = System.class.getResource("/image/icon.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        final TrayIcon trayIcon = new TrayIcon(image, "Скриншот");
        final SystemTray tray = SystemTray.getSystemTray();

        //listener для иконки в трее
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    //TODO добавить изменение цвета курсора
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
                //TODO добавить эвент для ПКМ:
                //      -настройка горячей клавиши для вызова функции
                //      -выход
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        Platform.setImplicitExit(false);
    }

    private List<Stage> getStagesForAllScreens(Stage primaryStage){
        List<Stage> stages = new ArrayList<>();
        ObservableList<Screen> screens = Screen.getScreens();
        for (int i = 0; i < screens.size(); i++){
            Rectangle2D bounds = screens.get(i).getVisualBounds();
            StackPane root = new StackPane();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            scene.setFill(Color.color(0f, 0f, 0f, 0.1));

            addSceneListeners(scene, stages);

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

        return stages;
    }

    private void addSceneListeners(Scene scene, List<Stage> stages){
        Screenshot screenshot = new Screenshot();

        //TODO добавить выделение области до отпускания кнопки мыши
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                event -> {
                    screenshot.setPressedCoords((int) event.getScreenX(), (int) event.getScreenY());
                    System.out.println("screenX: " + (int)event.getScreenX() + "; screenY: " + (int)event.getScreenY());
                });

        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                event -> {
                    screenshot.setReleasedCoords((int) event.getScreenX(), (int) event.getScreenY());
                    System.out.println("screenX: " + event.getScreenX() + "; screenY: " + event.getScreenY());
                    stages.forEach(Stage::hide);
                    stateWindow = 0;

                    BufferedImage capture;
                    File imageFile = new File("screenshot.png");
                    try {
                        capture = new Robot().createScreenCapture(screenshot.getRectangle());
                        ImageIO.write(capture, "png", imageFile );
                    } catch (AWTException e){
                        throw new RuntimeException(e.getMessage());
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }

                    //TODO когда скрин сделан, на месте верхнего левого угла появляется выбранная область
                    //	- в оранжевой и черной рамке, каждая толщиной в один пиксель
                    //	- нижняя оранжевая толще - 20 пикс
                    //	на нижней оранжевой две кнопки справа
                    //	- закрыть - крестик
                    //	- сохранить выбранную область в буфер обмена и закрыть
                    //	- при этом скрин висит поверх всех окон

                    //TODO масштабирование скриншота и конпка для возврата к исходному масштабу
                });
    }
}
