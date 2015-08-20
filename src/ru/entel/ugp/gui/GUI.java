package ru.entel.ugp.gui;

import ru.entel.ugp.owen.ssh.Exec;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class GUI extends JFrame {
    public final static String TITLE = "УГП ENTEL";
    public final static int WIDTH = 500;
    public final static int HEIGHT = 330;
    private Exec exec;

    public GUI() {
        exec = new Exec();
        getContentPane().add(new MainWindow(this, exec));
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(300, 300, WIDTH, HEIGHT);
        setVisible(true);
        setResizable(false);
        setIcon();
    }

    private void setIcon() {
//        Image img = null;
//        try {
//            img = ImageIO.read(new FileInputStream("res/icon.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        this.setIconImage(img);
        URL url = getClass().getResource("/img/icon.png");
        ImageIcon icon = new ImageIcon(url);
        Image img = icon.getImage();
        this.setIconImage(img);
    }
}
