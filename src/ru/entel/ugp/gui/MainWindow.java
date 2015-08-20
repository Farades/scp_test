package ru.entel.ugp.gui;

import com.jcraft.jsch.JSchException;
import ru.entel.ugp.owen.ssh.Exec;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindow extends WindowPanel {
    private final String ipDefault = "192.168.19.214";
    private JButton downloadLogs;
    private JButton openMonitoring;
    private JLabel currentIP;
    private JTextField ipField;
    private JButton ipButton;
    private Exec exec;
    private String ipAdr;
    private boolean ipStatus;

    public MainWindow(JFrame frame, Exec exec) {
        super(frame);
        this.exec = exec;

        JLabel welcomeLabel = new JLabel("Добро пожаловать в ПО УГП ENTEL");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBounds(100, 15, 300, 30);
        add(welcomeLabel);

        downloadLogs = new JButton("Скачать журнал данных");
        downloadLogs.setBounds(30, 60, 200, 50);
        downloadLogs.setEnabled(false);
        downloadLogs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                giveControlToExec();
            }
        });
        add(downloadLogs);

        openMonitoring = new JButton("Система мониторинга");
        openMonitoring.setBounds(250, 60, 200, 50);
        openMonitoring.setEnabled(false);
        openMonitoring.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMonitoring();
            }
        });
        add(openMonitoring);

        currentIP = new JLabel("Выбранный IP-адрес: ");
        currentIP.setFont(new Font("Arial", Font.PLAIN, 14));
        currentIP.setBounds(30, 250, 280, 30);
        add(currentIP);

        addLogo();

        addSettings();
    }

    private void addLogo() {
        try {
            BufferedImage logo = ImageIO.read(new File("res/logo.jpg"));
            JLabel picLabel = new JLabel(new ImageIcon(logo));
            picLabel.setBounds(300, 240, 150, 45);
            add(picLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disableAll() {
        downloadLogs.setEnabled(false);
        openMonitoring.setEnabled(false);
        ipField.setEnabled(false);
        ipButton.setEnabled(false);
    }

    public void enableAll() {
        downloadLogs.setEnabled(true);
        openMonitoring.setEnabled(true);
        ipField.setEnabled(true);
        ipButton.setEnabled(true);
    }

    public void openMonitoring() {
        if(Desktop.isDesktopSupported())
        {
            try {
                String webAdr = "http://" + this.ipAdr + ":8080/webvisu.htm";
                Desktop.getDesktop().browse(new URI(webAdr));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void giveControlToExec() {
        if (this.ipStatus == true) {
            exec.setHost(this.ipAdr);
            try {
                disableAll();
                exec.downloadFiles();

                JOptionPane.showMessageDialog(new JFrame(), "Журнад данных успешно скачан", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
            } catch (JSchException e) {
               JOptionPane.showMessageDialog(new JFrame(), "Нет связи с устройством", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (FileNotFoundException ex) {

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                enableAll();
            }
        }
    }

    public void checkIP() {
        if (ipStatus == true) {
            ipAdr = ipField.getText();
            downloadLogs.setEnabled(true);
            openMonitoring.setEnabled(true);
            currentIP.setText("Выбранный IP-адрес: " + ipAdr);
//            ipField.setEnabled(false);
//            ipButton.setEnabled(false);
//            JOptionPane.showMessageDialog(new JFrame(), "IP-адрес принят.", "Внимание", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(new JFrame(), "IP-адрес введен неверно.", "Внимание", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addSettings() {
        JLabel settingsLabel = new JLabel("Введите IP-адрес УГП ENTEL");
        settingsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        settingsLabel.setBounds(120, 140, 300, 30);
        add(settingsLabel);

        MaskFormatter mf = null;
        try {
            mf = new MaskFormatter("###.###.###.###");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ipField = new JFormattedTextField();

        ipField.setInputVerifier(new InputVerifier() {
            Pattern pat = Pattern.compile("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
            public boolean shouldYieldFocus(JComponent input) {
                boolean inputOK = verify(input);
                if (inputOK) {
                    ipStatus = true;
                    return true;
                } else {
                    ipStatus = false;
                    return false;
                }
            }
            public boolean verify(JComponent input) {
                JTextField field = (JTextField) input;
                Matcher m = pat.matcher(field.getText());
                return m.matches();
            }
        });

        ipField.setBounds(30, 180, 200, 50);
        ipField.setFont(new Font("Arial", Font.BOLD, 16));
        ipField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setText(ipDefault);

        ipButton = new JButton("Принять");
        ipButton.setBounds(250, 180, 200, 50);
        ipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkIP();
            }
        });

        add(ipField);
        add(ipButton);
    }
}
