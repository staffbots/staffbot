package ru.staffbots.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.WebServer;

/*
 * The main application window,
 * Contains only the "Management" button to go to the web interface,
 * Closing a running window terminates the entire application
 * Optional to run: can be disabled by ui.frame_used in cfg-file
 */
public class MainWindow extends JFrame {

    /*
     * Enable/disable the main application window:
     * true - enable, set by default
     * false - disable
     * The value can be set by ui.frame_used in cfg-file
     */
    private static boolean frameUsed = true;

    public static boolean getFrameUsed() {
        return frameUsed;
    }

    public static void setFrameUsed(Boolean value) {
        if (value == null) return;
        frameUsed = value;
    }

    /*
     * The single instance of the class
     */
    private static MainWindow mainWindow = null;

    /*
     * Initializing a single instance of a class - {@code mainWindow}
     */
    public static void init(String windowTilte) {
        if (mainWindow != null)
            return;
        if (frameUsed) {
            mainWindow = new MainWindow(windowTilte);
            Journal.add("init_window");
        } else {
            Journal.add(NoteType.WARNING, "init_window");
        }
    }

    /*
     * Constructor,
     * contains designing and launching the main application window in a separate thread
     */
    private MainWindow(String tilte) {
        super(tilte);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new MainWindowListener());
        setIconImage(Resources.getAsImage("png/icon.png"));
        Container container = getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 25));
        JCheckBox сheckBox = new JCheckBox(Languages.get().getValue("frame", "checkbox_caption"));
        сheckBox.setToolTipText(Languages.get().getValue("frame", "checkbox_hint"));
        сheckBox.setSelected(true);
        JButton button = new JButton(Languages.get().getValue("frame", "button_caption"));
        button.setToolTipText(Languages.get().getValue("frame", "button_hint"));
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(WebServer.getLocalURL(сheckBox.isSelected()).toURI());
                } catch (Exception exception) {
                    Journal.add(NoteType.ERROR, "open_browser", exception.getMessage());
                }
            }
        });
        container.add(button);
        if (WebServer.getHttpUsed())
            container.add(сheckBox);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 350;
        int height = 100;
        int locationX = (screenSize.width - width) / 2;
        int locationY = (screenSize.height - height) / 2;
        setBounds(locationX, locationY, width, height);
        setResizable(false);
        setSize(width, height);
        setVisible(true); // Open window
        //button.doClick(); // Run browser for management
    }

}
