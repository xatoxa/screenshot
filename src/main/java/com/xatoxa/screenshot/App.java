package com.xatoxa.screenshot;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

public class App extends Application {
    private static int stateWindow = 0;
    private static List<Stage> screenshotStages;
    private static ShortcutKeyStage stageShortcut;

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
        screenshotStages = getStagesForAllScreens(primaryStage);

        //загрузка настроек юзера
        Preferences prefs = Preferences.userRoot();

        //Объект слушателя горячих клавиш
        ShortcutKeyListener shortcutKeyListener = new ShortcutKeyListener(
                        screenshotStages,
                        prefs.getInt("keyCode", 88),
                        prefs.getBoolean("isAlt", true),
                        prefs.getBoolean("isShift", false),
                        prefs.getBoolean("isMeta", true),
                        prefs.getBoolean("isCtrl", false));

        //Stage для настройки горячей клавиши
        stageShortcut = new ShortcutKeyStage(shortcutKeyListener, prefs);

        //иконка для трея
        final TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(App.class.getResource("/com/xatoxa/screenshot/image/icon.png")),
                "Скриншот");
        final SystemTray tray = SystemTray.getSystemTray();

        //меню в трее
        trayIcon.setPopupMenu(getMenuForTray(tray, trayIcon));

        //listener для иконки в трее
        trayIcon.addMouseListener(getAdapterForTray());

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

        //глобальный прослушиватель нажатий клавиш
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(shortcutKeyListener);
        } catch (NativeHookException e) {
            System.out.println("Global key listener error");
        }

        Platform.setImplicitExit(false);
    }

    private List<Stage> getStagesForAllScreens(Stage primaryStage){
        List<Stage> stages = new ArrayList<>();
        ObservableList<Screen> screens = Screen.getScreens();

        for (int i = 0; i < screens.size(); i++){
            Rectangle2D bounds = screens.get(i).getVisualBounds();
            Group root = new Group();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            scene.setFill(Color.color(0f, 0f, 0f, 0.1));

            addSceneListeners(scene, root, stages);

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
            stage.setAlwaysOnTop(true);

            stages.add(stage);
        }

        return stages;
    }

    private void addSceneListeners(Scene scene, Group group, List<Stage> screenshotStages){
        ScreenshotRect screenshot = new ScreenshotRect();

        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                event -> screenshot.setPressedCoordinates(
                        (int) event.getScreenX(),
                        (int) event.getScreenY(),
                        (int) event.getSceneX(),
                        (int) event.getSceneY()));

        //FIXME не продолжается область выделения, начатая на другом мониторе
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED,
                event -> {
                    Region shape = new Region();
                    shape.setLayoutX(Math.min(screenshot.getSceneX1(), event.getSceneX()));
                    shape.setLayoutY(Math.min(screenshot.getSceneY1(), event.getSceneY()));
                    shape.setMinWidth(Math.max(screenshot.getSceneX1(), event.getSceneX()) - shape.getLayoutX());
                    shape.setMinHeight(Math.max(screenshot.getSceneY1(), event.getSceneY()) - shape.getLayoutY());
                    shape.setStyle(
                            "-fx-border-style: solid; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-color: #ff7f32;");

                    group.getChildren().clear();
                    group.getChildren().add(shape);
                });

        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                event -> {
                    screenshot.setReleasedCoordinates((int) event.getScreenX(), (int) event.getScreenY());
                    screenshotStages.forEach(Stage::hide);
                    stateWindow = 0;
                    group.getChildren().clear();

                    try {
                        final ScreenshotStage[] scrStage = {new ScreenshotStage(screenshot)};
                        scrStage[0].show();
                        scrStage[0].setOnCloseRequest(windowEvent -> scrStage[0] = null);
                    } catch (AWTException e) {
                        throw new RuntimeException(e);
                    }

                });
        javafx.scene.image.Image image = new javafx.scene.image.Image(Objects.requireNonNull(App.class.getResource("/com/xatoxa/screenshot/image/arrow.png")).toExternalForm());
        scene.setCursor(new ImageCursor(image, 0, 0));
    }

    private PopupMenu getMenuForTray(SystemTray tray, TrayIcon trayIcon){
        PopupMenu menu = new PopupMenu("Меню");

        MenuItem itemShortcut = new MenuItem("Горячая клавиша");
        itemShortcut.setActionCommand("Горячая клавиша");
        itemShortcut.addActionListener(e -> Platform.runLater(stageShortcut::show));

        MenuItem itemExit = new MenuItem("Выход");
        itemExit.setActionCommand("Выход");
        itemExit.addActionListener(e -> {
            tray.remove(trayIcon);
            Platform.runLater(Platform::exit);
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ex) {
                throw new RuntimeException(ex);
            }
        });

        menu.add(itemShortcut);
        menu.add(itemExit);

        return menu;
    }

    private MouseAdapter getAdapterForTray(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Platform.runLater(() -> {
                        if (stateWindow == 1) {
                            screenshotStages.forEach(Stage::hide);
                            stateWindow = 0;
                        } else if (stateWindow == 0) {
                            screenshotStages.forEach(Stage::show);
                            stateWindow = 1;
                        }
                    });
                }
            }
        };
    }
}
