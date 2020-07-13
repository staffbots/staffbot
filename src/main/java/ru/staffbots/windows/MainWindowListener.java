package ru.staffbots.windows;

import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainWindowListener implements WindowListener {

    public void windowActivated(WindowEvent event) {

    }

    public void windowClosed(WindowEvent event) {

    }

    public void windowClosing(WindowEvent event) {
        Journal.add(NoteType.WARNING, "close_window");
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
