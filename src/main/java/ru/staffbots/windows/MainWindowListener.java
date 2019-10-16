package ru.staffbots.windows;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainWindowListener implements WindowListener {

    public void windowActivated(WindowEvent event) {

    }

    public void windowClosed(WindowEvent event) {

    }

    public void windowClosing(WindowEvent event) {
        Journal.add("Закрытие приложения из главного окна на сервере" , NoteType.WRINING);
    }

    public void windowDeactivated(WindowEvent event) {

    }

    public void windowDeiconified(WindowEvent event) {

    }

    public void windowIconified(WindowEvent event) {

    }

    public void windowOpened(WindowEvent event) {

    }

}
