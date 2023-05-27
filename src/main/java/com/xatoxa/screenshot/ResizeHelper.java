package com.xatoxa.screenshot;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

//created by Alexander Berg
public class ResizeHelper {

    public static ResizeListener addResizeListener(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage);
        Scene scene = stage.getScene();
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);

        return resizeListener;
    }

    public static void removeResizeListener(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage);
        stage.getScene().removeEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
        stage.getScene().removeEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
        stage.getScene().removeEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
        stage.getScene().removeEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
        stage.getScene().removeEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);
        ObservableList<Node> children = stage.getScene().getRoot().getChildrenUnmodifiable();
        for (Node child : children) {
            removeListenerDeeply(child, resizeListener);
        }

        resizeListener = null;
    }

    public static void removeListenerDeeply(Node node, EventHandler<MouseEvent> listener) {
        node.removeEventHandler(MouseEvent.MOUSE_MOVED, listener);
        node.removeEventHandler(MouseEvent.MOUSE_PRESSED, listener);
        node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, listener);
        node.removeEventHandler(MouseEvent.MOUSE_EXITED, listener);
        node.removeEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener);
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            ObservableList<Node> children = parent.getChildrenUnmodifiable();
            for (Node child : children) {
                removeListenerDeeply(child, listener);
            }
        }
    }

    public static class ResizeListener implements EventHandler<MouseEvent> {
        private Stage stage;
        private Scene scene;
        private Cursor cursorEvent = Cursor.DEFAULT;
        private int border = 4;
        private double startX = 0;
        private double startY = 0;
        private double sceneOffsetX = 0;
        private double sceneOffsetY = 0;
        private double padTop = 0;
        private double padRight = 0;
        private double padBottom = 0;
        private double padLeft = 0;
        private double xForDrag = 0;
        private double yForDrag = 0;
        private double ratioWidth = 0;
        private double ratioHeight = 0;
        private boolean isShiftDown = false;

        public ResizeListener(Stage stage) {
            this.stage = stage;
            this.scene = stage.getScene();
        }


        public void setPadding(Insets padding) {
            padTop = padding.getTop();
            padRight = padding.getRight();
            padBottom = padding.getBottom();
            padLeft = padding.getLeft();
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
                    viewWidth = stage.getWidth() - padLeft - padRight,
                    viewHeight = stage.getHeight() - padTop - padBottom;

            if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                if (mouseEventX < border + padLeft && mouseEventY < border + padTop) {
                    cursorEvent = Cursor.NW_RESIZE;
                } else if (mouseEventX < border + padLeft && mouseEventY > viewHeight - border + padTop) {
                    cursorEvent = Cursor.SW_RESIZE;
                } else if (mouseEventX > viewWidth - border + padLeft && mouseEventY < border + padTop) {
                    cursorEvent = Cursor.NE_RESIZE;
                } else if (mouseEventX > viewWidth - border + padLeft && mouseEventY > viewHeight - border + padTop) {
                    cursorEvent = Cursor.SE_RESIZE;
                } else if (mouseEventX < border + padLeft) {
                    cursorEvent = Cursor.W_RESIZE;
                } else if (mouseEventX > viewWidth - border + padLeft) {
                    cursorEvent = Cursor.E_RESIZE;
                } else if (mouseEventY < border + padTop) {
                    cursorEvent = Cursor.N_RESIZE;
                } else if (mouseEventY > viewHeight - border + padTop) {
                    cursorEvent = Cursor.S_RESIZE;
                } else {
                    cursorEvent = Cursor.DEFAULT;
                }

                scene.setCursor(cursorEvent);
            } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_EXITED_TARGET.equals(mouseEventType)) {
                scene.setCursor(Cursor.DEFAULT);
            } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                startX = viewWidth - mouseEventX;
                startY = viewHeight - mouseEventY;
                sceneOffsetX = mouseEvent.getSceneX();
                sceneOffsetY = mouseEvent.getSceneY();
                xForDrag = stage.getX() - mouseEvent.getScreenX();
                yForDrag = stage.getY() - mouseEvent.getScreenY();
                ratioHeight = stage.getHeight() / stage.getWidth();
                ratioWidth = stage.getWidth() / stage.getHeight();
            } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType) && !Cursor.DEFAULT.equals(cursorEvent)) {
                if (!Cursor.W_RESIZE.equals(cursorEvent) && !Cursor.E_RESIZE.equals(cursorEvent)) {
                    double minHeight = stage.getMinHeight() > (border * 2) ? stage.getMinHeight() : (border * 2);

                    if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.N_RESIZE.equals(cursorEvent) || Cursor.NE_RESIZE.equals(cursorEvent)) {
                        if (stage.getHeight() > minHeight || mouseEventY < 0) {
                            double height = stage.getY() - mouseEvent.getScreenY() + stage.getHeight() + sceneOffsetY;
                            double y = mouseEvent.getScreenY() - sceneOffsetY;

                            stage.setHeight(height);
                            if (!(Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.NE_RESIZE.equals(cursorEvent)))
                                stage.setY(y);

                            if (!isShiftDown) {
                                stage.setWidth(stage.getHeight() * ratioWidth);
                            }
                        }
                    } else {
                        if (stage.getHeight() > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
                            stage.setHeight(mouseEventY + startY + padBottom + padTop);
                            if (!isShiftDown) {
                                stage.setWidth(stage.getHeight() * ratioWidth);
                            }
                        }
                    }
                }

                if (!Cursor.N_RESIZE.equals(cursorEvent) && !Cursor.S_RESIZE.equals(cursorEvent)) {
                    double minWidth = stage.getMinWidth() > (border * 2) ? stage.getMinWidth() : (border * 2);
                    if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.W_RESIZE.equals(cursorEvent) || Cursor.SW_RESIZE.equals(cursorEvent)) {
                        if (stage.getWidth() > minWidth || mouseEventX < 0) {
                            double width = stage.getX() - mouseEvent.getScreenX() + stage.getWidth() + sceneOffsetX;
                            double x = mouseEvent.getScreenX() - sceneOffsetX;

                            stage.setWidth(width);

                            if (!(Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.SW_RESIZE.equals(cursorEvent)))
                                stage.setX(x);

                            if (!isShiftDown) {
                                stage.setHeight(stage.getWidth() * ratioHeight);
                            }
                        }
                    } else {
                        if (stage.getWidth() > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
                            stage.setWidth(mouseEventX + startX + padLeft + padRight);

                            if (!isShiftDown) {
                                stage.setHeight(stage.getWidth() * ratioHeight);
                            }
                        }
                    }
                }
            } else if (Cursor.DEFAULT.equals(cursorEvent)) {
                stage.setX(mouseEvent.getScreenX() + xForDrag);
                stage.setY(mouseEvent.getScreenY() + yForDrag);
            }


        }
    }
}