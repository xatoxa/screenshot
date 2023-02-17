package com.xatoxa.screenshot;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

//this class is written by Alexander Berg
public class ResizeHelper {
    public static void addResizeListener(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);
        ObservableList<Node> children = stage.getScene().getRoot().getChildrenUnmodifiable();
        for (Node child : children) {
            addListenerDeeply(child, resizeListener);
        }
    }

    public static void addListenerDeeply(Node node, EventHandler<MouseEvent> listener) {
        node.addEventHandler(MouseEvent.MOUSE_MOVED, listener);
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener);
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, listener);
        node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener);
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            ObservableList<Node> children = parent.getChildrenUnmodifiable();
            for (Node child : children) {
                addListenerDeeply(child, listener);
            }
        }
    }

    static class ResizeListener implements EventHandler<MouseEvent> {
        private final Stage stage;
        private final int border = 4;
        private Cursor cursorEvent = Cursor.DEFAULT;
        private double startX = 0;
        private double startY = 0;
        private double xForDrag = 0;
        private double yForDrag = 0;
        private double ratioWidth = 0;
        private double ratioHeight = 0;
        private boolean isShiftDown = false;

        public ResizeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
            Scene scene = stage.getScene();
            scene.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.SHIFT)) {
                    isShiftDown = true;
                }
            });
            scene.setOnKeyReleased(event -> {
                if (event.getCode().equals(KeyCode.SHIFT)){
                    isShiftDown = false;
                }
            });

            double mouseEventX = mouseEvent.getSceneX(),
                    mouseEventY = mouseEvent.getSceneY(),
                    sceneWidth = scene.getWidth(),
                    sceneHeight = scene.getHeight();

            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                if (mouseEventX < border && mouseEventY < border) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEventX < border) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (mouseEventX > sceneWidth - border) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (mouseEventY < border) {
                    cursorEvent = Cursor.N_RESIZE;
                } else if (mouseEventY > sceneHeight - border) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }
                scene.setCursor(cursorEvent);
            } else if(MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_EXITED_TARGET.equals(mouseEventType)){
                scene.setCursor(Cursor.DEFAULT);
            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                startX = stage.getWidth() - mouseEventX;
                startY = stage.getHeight() - mouseEventY;
                xForDrag = stage.getX() - mouseEvent.getScreenX();
                yForDrag = stage.getY() - mouseEvent.getScreenY();

                ratioHeight = stage.getHeight() / stage.getWidth();
                ratioWidth = stage.getWidth() / stage.getHeight();
            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                if (!Cursor.DEFAULT.equals(cursorEvent)) {
                    if (!Cursor.W_RESIZE.equals(cursorEvent) && !Cursor.E_RESIZE.equals(cursorEvent)) {
                        double minHeight = stage.getMinHeight() > (border*2) ? stage.getMinHeight() : (border*2);
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.N_RESIZE.equals(cursorEvent) || Cursor.NE_RESIZE.equals(cursorEvent)) {
                            if (stage.getHeight() > minHeight || mouseEventY < 0) {
                                stage.setHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
                                stage.setY(mouseEvent.getScreenY());
                                if (!isShiftDown) {
                                    stage.setWidth(stage.getHeight() * ratioWidth);
                                }
                            }
                        } else {
                            if (stage.getHeight() > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
                                stage.setHeight(mouseEventY + startY);
                                if (!isShiftDown) {
                                    stage.setWidth(stage.getHeight() * ratioWidth);
                                }
                            }
                        }
                    }

                    if (!Cursor.N_RESIZE.equals(cursorEvent) && !Cursor.S_RESIZE.equals(cursorEvent)) {
                        double minWidth = stage.getMinWidth() > (border*2) ? stage.getMinWidth() : (border*2);
                        if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.W_RESIZE.equals(cursorEvent) || Cursor.SW_RESIZE.equals(cursorEvent)) {
                            if (stage.getWidth() > minWidth || mouseEventX < 0) {
                                stage.setWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
                                stage.setX(mouseEvent.getScreenX());
                                if (!isShiftDown) {
                                    stage.setHeight(stage.getWidth() * ratioHeight);
                                }
                            }
                        } else {
                            if (stage.getWidth() > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
                                stage.setWidth(mouseEventX + startX);
                                if (!isShiftDown) {
                                    stage.setHeight(stage.getWidth() * ratioHeight);
                                }
                            }
                        }
                    }
                } else {
                    stage.setX(mouseEvent.getScreenX() + xForDrag);
                    stage.setY(mouseEvent.getScreenY() + yForDrag);
                }
            }
        }
    }
}
