package ru.entel.ugp.gui;

import javax.swing.*;

public class WindowPanel extends JPanel {
    protected JFrame frame;     // ��������� �� ����� ����� ��� ����� ������� ������ (������� ��� ���?)

    public WindowPanel(JFrame frame) {
        this.frame = frame;
        setLayout(null);
    }
}
