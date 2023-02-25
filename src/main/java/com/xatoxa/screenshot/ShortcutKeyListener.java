package com.xatoxa.screenshot;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.List;

public class ShortcutKeyListener implements NativeKeyListener {
    int keyCode;
    boolean isAlt;
    boolean isShift;
    boolean isMeta;
    boolean isCtrl;
    boolean isAltPressed = false;
    boolean isShiftPressed = false;
    boolean isMetaPressed = false;
    boolean isCtrlPressed = false;

    List<Stage> stages;

    public ShortcutKeyListener(List<Stage> stages, int keyCode, boolean isAlt, boolean isShift, boolean isMeta, boolean isCtrl){
        this.stages = stages;
        this.keyCode = keyCode;
        this.isAlt = isAlt;
        this.isShift = isShift;
        this.isMeta = isMeta;
        this.isCtrl = isCtrl;
    }

    public void refreshShortcutKey(int keyCode, boolean isAlt, boolean isShift, boolean isMeta, boolean isCtrl){
        this.keyCode = keyCode;
        this.isAlt = isAlt;
        this.isShift = isShift;
        this.isMeta = isMeta;
        this.isCtrl = isCtrl;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        isAltPressed = (nativeEvent.getModifiers() & NativeKeyEvent.ALT_MASK) != 0;
        isShiftPressed = (nativeEvent.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
        isMetaPressed = (nativeEvent.getModifiers() & NativeKeyEvent.META_MASK) != 0;
        isCtrlPressed = (nativeEvent.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;

        if (nativeEvent.getRawCode() == this.keyCode
                && (!isAlt || isAlt == isAltPressed)
                && (!isShift || isShift == isShiftPressed)
                && (!isMeta || isMeta == isMetaPressed)
                && (!isCtrl || isCtrl == isCtrlPressed)){
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
