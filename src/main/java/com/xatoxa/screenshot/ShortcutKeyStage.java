package com.xatoxa.screenshot;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.prefs.Preferences;

public class ShortcutKeyStage extends Stage {
    Preferences preferences;
    public ShortcutKeyStage(ShortcutKeyListener shortcutKeyListener, Preferences prefs){
        this.preferences = prefs;
        javafx.scene.control.Label label = new javafx.scene.control.Label();
        label.setFont(Font.font("Segue UI", 15));
        label.setAlignment(Pos.CENTER);
        label.setText(getPreferencesTextShortcut());

        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (!event.getCode().isModifierKey()) {
                label.setText(createCombo(event).getDisplayText());
                shortcutKeyListener.refreshShortcutKey(
                        event.getCode().getCode(),
                        event.isAltDown(),
                        event.isShiftDown(),
                        event.isMetaDown(),
                        event.isControlDown());
                prefs.putInt("keyCode", event.getCode().getCode());
                prefs.putBoolean("isAlt", event.isAltDown());
                prefs.putBoolean("isShift", event.isShiftDown());
                prefs.putBoolean("isMeta", event.isMetaDown());
                prefs.putBoolean("isCtrl", event.isControlDown());
            }
        });

        Button buttonOk = new Button("ОK");
        buttonOk.setOnAction(actionEvent -> this.close());
        buttonOk.setAlignment(Pos.CENTER);

        GridPane gridPane = new GridPane();
        gridPane.add(label, 0, 0);
        gridPane.add(buttonOk, 0, 1);
        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setHalignment(buttonOk, HPos.CENTER);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getRowConstraints().addAll(new RowConstraints(50), new RowConstraints(50));

        this.setScene(new Scene(gridPane, 300, 100));
        this.setResizable(false);
        this.initStyle(StageStyle.UNIFIED);

    }


    private String getPreferencesTextShortcut(){
        return (preferences.getBoolean("isAlt", true) ? "Alt+" : "") +
                (preferences.getBoolean("isShift", false) ? "Shift+" : "") +
                (preferences.getBoolean("isMeta", true) ? "Meta+" : "") +
                (preferences.getBoolean("isCtrl", false) ? "Ctrl+" : "") +
                (char) (preferences.getInt("keyCode", 88));
    }


    private KeyCombination createCombo(KeyEvent event) {
        var modifiers = new ArrayList<KeyCombination.Modifier>();
        if (event.isControlDown()) {
            modifiers.add(KeyCombination.CONTROL_DOWN);
        }
        if (event.isMetaDown()) {
            modifiers.add(KeyCombination.META_DOWN);
        }
        if (event.isAltDown()) {
            modifiers.add(KeyCombination.ALT_DOWN);
        }
        if (event.isShiftDown()) {
            modifiers.add(KeyCombination.SHIFT_DOWN);
        }
        return new KeyCodeCombination(event.getCode(), modifiers.toArray(KeyCombination.Modifier[]::new));
    }
}
