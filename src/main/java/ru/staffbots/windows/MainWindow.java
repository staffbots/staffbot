package ru.staffbots.windows;

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

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.webserver.WebServer;

import java.net.URI;

/*
 * Главное оконо приложения,
 * Содержит только кнопку "Управление" для перехода к веб-интерфейсу,
 * Закрытие запущенного окна прекращает работу всего приложения
 * Необязателен для запуска: можно отключить параметром gui.used в файле staffbot.cfg
 */
public class MainWindow extends JFrame {

    /*
     * Парамер включения/отключения главного окна приложения:
     * true - включен, установлен по умолчанию
     * false - отключен
     * Значение можно выставить в файле staffbot.cfg параметром gui.used
     */
    public static Boolean USED = true;

    /*
     * Единственного экзэмпляра класса
     */
    private static MainWindow mainWindow = null;

    /*
     * Инициация единственного экзэмпляра класса
     */
    public static void init(String windowTilte) {
        if (mainWindow != null)
            return;
        if (USED) {
            mainWindow = new MainWindow(windowTilte);
            Journal.add("Главное окно приложения открыто");
        }
    }

    /*
     * Конструктор,
     * Запуск главного окна приложения в отдельном потоке
     */
    public MainWindow(String tilte) {
        super(tilte);
        // Закрытие окна прекращает работу всего приложения
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Подгружаем иконку из ресурсов
        String iconName = "/img/logo.png";
        try {
            setIconImage(new ImageIcon(ImageIO.read(
                    getClass().getResourceAsStream(iconName))).getImage());
        } catch (Exception exception) {
            Journal.add("Иконка приложения не загружена (" + iconName + ")", NoteType.ERROR, exception);
        }
        // Панель содержимого
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 25));
        // Создание кнопки
        JButton button = new JButton("Управление");
        button.setToolTipText("Перейти к управлению через веб-интерфейс");
        // Обработка нажатия кнопки
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(WebServer.getURL().toURI());
                } catch (Exception exception) {
                    Journal.add("Неудачная попытка открыть браузер", NoteType.ERROR, exception);
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
        setSize(width, height);
        // Открываем окно
        setVisible(true);
    }

}
