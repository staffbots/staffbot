package ru.staffbots.database;

import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Database statement executor. Statement may be query or update
 * @param <T> type of return value as query answer
 */
public class Executor<T> {

    private String noteName = Journal.defaultNoteName;

    String[] noteParameters = new String[0];

    public Executor () {}

    public Executor (String noteName, String... noteParameters){
        this.noteName = noteName;
        this.noteParameters = noteParameters;
    }

    public T execQuery(String query, ResultHandler<T> handler, String... parameters) {
        T result = null;
        try {
            if(Database.disconnected()) {
                if (Database.getException() == null)
                    return result;
                else
                    throw Database.getException();
            }
            if (parameters.length == 0) {
                Statement statement = Database.getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                result = handler.handle(resultSet);
                resultSet.close();
                statement.close();
            } else {
                PreparedStatement prepareStatement = Database.getConnection().prepareStatement(query);
                for(int i = 0; i < parameters.length; i++)
                    prepareStatement.setString(i + 1, parameters[i]);
                ResultSet resultSet = prepareStatement.executeQuery();
                result = handler.handle(resultSet);
                resultSet.close();
                prepareStatement.close();
            }
            if (noteName != null)
                if (!noteName.equals(Journal.defaultNoteName) || noteParameters.length > 0)
                    Journal.add(noteName, noteParameters);
        } catch (Exception exception) {
            if (noteName != null) {
                //exception.printStackTrace();
                List<String> noteParametersList = new ArrayList(Arrays.asList(noteParameters));
                noteParametersList.add(exception.getMessage());
                Journal.add(NoteType.ERROR, noteName, noteParametersList.stream().toArray(String[]::new));
            }
        }
        return result;
    }

    public int execUpdate(String update, String... parameters) {
        int result = 0;
        try {
            if(Database.disconnected()) {
                if (Database.getException() == null)
                    return 0;
                else
                    throw Database.getException();
            }
            PreparedStatement prepareStatement = Database.getConnection().prepareStatement(update);
            for(int i = 0; i < parameters.length; i++)
                prepareStatement.setString(i + 1, parameters[i]);
            result = prepareStatement.executeUpdate();
            if (result == 0) {
                String prefix = update.toUpperCase().trim();
                if (prefix.startsWith("DROP") || prefix.startsWith("CREATE"))
                    result = 1;
            }
            prepareStatement.close();
            if (noteName != null && result > 0)
                if (!noteName.equals(Journal.defaultNoteName) || noteParameters.length > 0)
                    Journal.add(noteName, noteParameters);
        } catch (Exception exception) {
            if (noteName != null) {
                //exception.printStackTrace();
                List<String> noteParametersList = new ArrayList(Arrays.asList(noteParameters));
                noteParametersList.add(exception.getMessage());
                Journal.add(NoteType.ERROR, noteName, noteParametersList.stream().toArray(String[]::new));
            }
        }
        return result;
    }

}
