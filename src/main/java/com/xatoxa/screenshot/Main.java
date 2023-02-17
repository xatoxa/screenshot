package com.xatoxa.screenshot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static int stateWindow = 0;

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
        List<Stage> screenshotStages = getStagesForAllScreens(primaryStage);

        //иконка для трея
        URL url = System.class.getResource("/image/icon.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        final TrayIcon trayIcon = new TrayIcon(image, "Скриншот");
        final SystemTray tray = SystemTray.getSystemTray();

        PopupMenu menu = new PopupMenu("Меню");

        //TODO добавить item "настройка горячей клавиши"
        MenuItem itemExit = new MenuItem("Выход");
        itemExit.setActionCommand("Выход");
        itemExit.addActionListener(e -> {
            tray.remove(trayIcon);
            Platform.runLater(Platform::exit);
        });
        menu.add(itemExit);
        trayIcon.setPopupMenu(menu);

        //listener для иконки в трее
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    //TODO добавить изменение цвета курсора
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
                        showStageScreenshot(screenshot);
                    } catch (AWTException e){
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }

    private void showStageScreenshot(ScreenshotRect screenshotRect) throws AWTException {
        Stage stageImage = new Stage();

        BufferedImage capture = new Robot().createScreenCapture(screenshotRect.getRectangle());
        javafx.scene.image.Image image = SwingFXUtils.toFXImage(capture, null);
        ImageView imageView = new ImageView(image);
        ImageViewPane viewPane = new ImageViewPane(imageView);

        //чёрная граница вокруг скриншота
        BorderPane blackBorder = new BorderPane(viewPane);
        blackBorder.setStyle(
                "-fx-border-style: solid; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: #000000;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //нижний бар для кнопок
        HBox hBox = new HBox(
                spacer,
                addCopyToClipboardButton(capture, stageImage),
                addOriginalSizeButton(stageImage, screenshotRect.getRectangle()),
                addCloseButton(stageImage));
        hBox.setStyle("-fx-background-color: #ff7f32; -fx-min-height: 20; -fx-max-height: 20");

        //оранжевая граница вокруг скриншота
        BorderPane root = new BorderPane();
        root.setCenter(blackBorder);
        root.setBottom(hBox);
        root.setStyle(
                "-fx-border-style: solid; " +
                "-fx-border-width: 1; " +
                "-fx-border-color: #ff7f32;");

        Scene sceneImage = new Scene(root);

        //настройки stage
        stageImage.setScene(sceneImage);
        stageImage.setX(screenshotRect.getX1());
        stageImage.setY(screenshotRect.getY1());
        stageImage.setAlwaysOnTop(true);
        stageImage.initStyle(StageStyle.UNDECORATED);

        ResizeHelper.addResizeListener(stageImage);

        stageImage.show();
    }

    private Button makeButton(String imgRes) {
        javafx.scene.image.Image imgClose = new javafx.scene.image.Image(imgRes);
        ImageView view = new ImageView(imgClose);
        view.setFitHeight(18);
        view.setPreserveRatio(true);
        Button btn = new Button();
        btn.setStyle(
                "-fx-min-height: 18; -fx-max-height: 18; -fx-min-width: 18; -fx-max-width: 18;");
        btn.setGraphic(view);

        return btn;
    }

    private Button addCloseButton(Stage stageImage){
        Button btnClose = makeButton("/image/btnClose.png");
        btnClose.setOnAction(event -> stageImage.close());

        return btnClose;
    }

    private Button addOriginalSizeButton(Stage stageImage, Rectangle rect){
        Button btnClose = makeButton("/image/btnBack.png");
        btnClose.setOnAction(event -> {
            stageImage.setHeight(rect.getHeight() + 24);
            stageImage.setWidth(rect.getWidth() + 4);
        });

        return btnClose;
    }

    private Button addCopyToClipboardButton(BufferedImage image, Stage stage){
        Button btnClose = makeButton("/image/btnClipboard.png");
        btnClose.setOnAction(event -> {
            CopyImgToClipboard clipBoard = new CopyImgToClipboard(image);
            clipBoard.copy();
            stage.close();
        });

        return btnClose;
    }
}
