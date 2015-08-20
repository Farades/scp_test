package ru.entel.ugp.gui;

import javax.swing.*;

public class WindowPanel extends JPanel {
    protected JFrame frame;     // ”казатель на фрейм нужен дл€ смены текущей панели (костыль или нет?)

    public WindowPanel(JFrame frame) {
        this.frame = frame;
        setLayout(null);
    }
}
