package com.xatoxa.screenshot;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.util.List;
import java.util.Objects;

public class ScreenshotStage extends Stage {
    private WritableImage wrImage;

    public ScreenshotStage(ScreenshotRect screenshotRect) throws AWTException {
        MultiResolutionImage capture = new Robot().createMultiResolutionScreenCapture(screenshotRect.getRectangle());
        List<Image> resolutionVariants = capture.getResolutionVariants();

        Image bufImage;
        if (resolutionVariants.size() > 1){
            bufImage = resolutionVariants.get(1);
        }else {
            bufImage = resolutionVariants.get(0);
        }
        this.wrImage = new WritableImage(screenshotRect.getW(), screenshotRect.getH());

        this.wrImage = SwingFXUtils.toFXImage(toBufferedImage(bufImage), this.wrImage);

        ImageView imageView = new ImageView(this.wrImage);
        imageView.setFitWidth(this.wrImage.getWidth() / Screen.getPrimary().getOutputScaleX());
        imageView.setFitHeight(this.wrImage.getHeight() / Screen.getPrimary().getOutputScaleY());
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
                addCopyToClipboardButton(),
                addOriginalSizeButton(screenshotRect.getRectangle()),
                addCloseButton());
        hBox.setStyle("-fx-background-color: #ff7f32; -fx-min-height: 18; -fx-max-height: 18");
        hBox.setPadding(new Insets(0, 5, 0, 0));

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
        this.setScene(sceneImage);
        this.setX(screenshotRect.getX1());
        this.setY(screenshotRect.getY1());
        this.setAlwaysOnTop(true);
        this.initStyle(StageStyle.UNDECORATED);
        this.setMinHeight(56);
        this.setMinWidth(56);

        ResizeHelper.addResizeListener(this);
    }

    private Button makeButton(String imgRes) {
        javafx.scene.image.Image imgClose =
                new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream(imgRes)));
        ImageView view = new ImageView(imgClose);
        view.setFitHeight(18);
        view.setPreserveRatio(true);
        Button button = new Button();
        button.setStyle(
                "-fx-min-height: 18; -fx-max-height: 18; -fx-min-width: 18; -fx-max-width: 18; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0, 0, 1, 1; -fx-end-padding: 5px;");
        button.setGraphic(view);

        return button;
    }

    private javafx.scene.control.Button addCloseButton(){
        javafx.scene.control.Button button = makeButton("/com/xatoxa/screenshot/image/btnClose.png");
        button.setOnAction(event -> closeThisStage());
        button.setCancelButton(true);
        button.setFocusTraversable(false);

        return button;
    }

    private javafx.scene.control.Button addOriginalSizeButton(Rectangle rect){
        javafx.scene.control.Button button = makeButton("/com/xatoxa/screenshot/image/btnBack.png");
        button.setOnAction(event -> {
            this.setHeight(rect.getHeight() + 24);
            this.setWidth(rect.getWidth() + 4);
        });
        button.setFocusTraversable(false);

        return button;
    }

    private javafx.scene.control.Button addCopyToClipboardButton(){
        Button button = makeButton("/com/xatoxa/screenshot/image/btnClipboard.png");
        button.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putImage(this.wrImage);
            clipboard.setContent(content);

            closeThisStage();
        });

        return button;
    }

    private BufferedImage toBufferedImage(Image image)
    {
        if (image instanceof BufferedImage)
        {
            return (BufferedImage) image;
        }

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        return bufferedImage;
    }

    private void closeThisStage() {
        this.close();

        ResizeHelper.removeResizeListener(this);

        Scene scene = this.getScene();
        Parent parent = scene.getRoot();

        ObservableList<Node> children = parent.getChildrenUnmodifiable();
        for (Node child : children) {
            child = null;
        }

        wrImage = null;
        scene = null;
        parent = null;

        System.gc();
    }
}
