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
import javax.swing.JCheckBox;
import javax.swing.ImageIcon;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.webserver.WebServer;


/*
 * Главное окно приложения,
 * Содержит только кнопку "Управление" для перехода к веб-интерфейсу,
 * Закрытие запущенного окна прекращает работу всего приложения
 * Необязателен для запуска: можно отключить параметром gui.used в файле staffbot.cfg
 */
public class MainWindow extends JFrame {

    /*
     * Парамер включения/отключения главного окна приложения:
     * true - включен, установлен по умолчанию
     * false - отключен
     * Значение можно выставить в cfg-файле параметром gui.used
     * Загрузка из файла осуществляется в методе propertiesInit() класса Pattern
     */
    public static Boolean USED = true;

    /*
     * Единственный экзэмпляр класса
     */
    private static MainWindow mainWindow = null;

    /*
     * Инициация единственного экзэмпляра класса
     * Вызывается из метода windowInit() класса Pattern
     */
    public static void init(String windowTilte) {
        if (mainWindow != null)
            return;
        if (USED) {
            mainWindow = new MainWindow(windowTilte);
            Journal.add("Главное окно приложения открыто");
        } else {
            Journal.add("Приложение работает без запуска главного окна");
        }
    }

    /*
     * Конструктор,
     * Запуск главного окна приложения в отдельном потоке
     */
    private MainWindow(String tilte) {
        super(tilte);
        // Закрытие окна прекращает работу всего приложения
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new MainWindowListener());

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

        JCheckBox сheckBox = new JCheckBox("SSL");
        //JCheckBox сheckBox = new JCheckBox("中国人");
        сheckBox.setToolTipText("Использовать SSL-протокол");
        сheckBox.setSelected(true);

        // Создание кнопки
        JButton button = new JButton("Управление");
        button.setToolTipText("Перейти к управлению через веб-интерфейс");
        // Обработка нажатия кнопки
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(WebServer.getURL(сheckBox.isSelected()).toURI());
                } catch (Exception exception) {
                    Journal.add("Неудачная попытка открыть браузер", NoteType.ERROR, exception);
                }
            }
        });
        container.add(button);
        if (WebServer.HTTP_USED)
            container.add(сheckBox);
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
