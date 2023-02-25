package com.xatoxa.screenshot;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ShortcutKeyListener implements NativeKeyListener {
    boolean isAltPressed = false;
    boolean isShiftPressed = false;
    boolean isMetaPressed = false;
    boolean isCtrlPressed = false;

    List<Stage> stages;

    public ShortcutKeyListener(List<Stage> stages){
        this.stages = stages;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        isAltPressed = (nativeEvent.getModifiers() & NativeKeyEvent.ALT_MASK) != 0;
        isShiftPressed = (nativeEvent.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
        isMetaPressed = (nativeEvent.getModifiers() & NativeKeyEvent.META_MASK) != 0;
        isCtrlPressed = (nativeEvent.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;

        if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_X && isAltPressed && isMetaPressed){
            Platform.runLater(() -> stages.forEach(Stage::show));
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
        isAltPressed = (nativeEvent.getModifiers() & NativeKeyEvent.ALT_MASK) != 0;
        isShiftPressed = (nativeEvent.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
        isMetaPressed = (nativeEvent.getModifiers() & NativeKeyEvent.META_MASK) != 0;
        isCtrlPressed = (nativeEvent.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;


    }
}
