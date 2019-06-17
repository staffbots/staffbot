package ru.staffbot.windows;

import java.awt.Dimension;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.webserver.WebServer;

import java.io.IOException;
import java.net.URI;

public class MainWindow extends JFrame {

    public static Boolean USED = true;

    public static void init(String windowTilte) {
        if (USED) {
            new MainWindow(windowTilte);
            Journal.add("Главное окно приложения открыто");
        }
    }

    public MainWindow(String tilte) {
        super(tilte);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try {
            setIconImage(new ImageIcon(ImageIO.read(
                    getClass().getResourceAsStream("/img/logo.png"))).getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Панель содержимого
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 25));
        // Создание кнопки
        JButton button = new JButton("Управление");
        button.setToolTipText("Перейти к управлению через веб-интерфейс");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI("http://localhost:" + WebServer.PORT));
                } catch (Exception exception) {
                    Journal.add("Неудачная попытка открыть браузер", NoteType.ERROR);
                }
            }
        });
        container.add(button);
        // Размеры и положение окна
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 350;
        int height = 100;
        int locationX = (screenSize.width - width) / 2;
        int locationY = (screenSize.height - height) / 2;
        setBounds(locationX, locationY, width, height);
        setResizable(false);
        // Открываем окно
        setSize(width, height);
        setVisible(true);
    }

}
