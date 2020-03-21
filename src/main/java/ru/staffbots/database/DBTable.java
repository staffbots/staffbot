package ru.staffbots.database;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.sql.*;

/*
 * Таблица БД,
 * Предоставляет интерфейс общий для всех таблиц БД,
 * является родителем для классов Value, Journal, Settings, Configs, Users
 */
public abstract class DBTable {

    private String name;
    /**
     * <b>Описание</b>,
     */
    protected String note;
    private String fields;

    public String getTableName(){
        return name;
    }

    /**
     * <b>Получить описание</b><br>
     * @return описание
     */
    public String getNote(){
        return note;
    }


    public DBTable(String name, String note, String fields){
        this.name = name;
        this.note = (note == null ? "" : note);
        this.fields = fields;
        createTable();
    }

    public DBTable(String name, String note, String fields, boolean isStorable){
        this.name = name;
        this.note = (note == null ? "" : note);
        this.fields = fields;
        if (isStorable) createTable();
    }

    public boolean tableExists(){
        return Database.tableExists(name);
    }

    public boolean createTable(){
        return createTable(false);
    }

    public boolean createTable(boolean drop){
        if (Database.disconnected()) return false;
        if (drop) dropTable();
        try {
            if (new Executor<>().execUpdate("CREATE TABLE IF NOT EXISTS " + name + " (" + fields + ")") > 0)
                Journal.add(NoteType.WARNING, "create_table", name, fields);
        } catch (Exception exception) {
            //connection = null;
            Journal.add(NoteType.ERROR, "create_table", name, exception.getMessage());
            return false;
        }
        return true;
    }

    public boolean eraseTable(){
        if(Database.disconnected())return false;
        try {
            if (tableExists()) {
                getStatement("DELETE FROM " + name).execute();
                Journal.add(NoteType.WARNING, "erase_table", name);
                return true;
            }
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "erase_table", name, exception.getMessage());
        }
        return false;
    }

    public boolean dropTable(){
        return Database.dropTable(name);
    }

    protected PreparedStatement statement;

    public PreparedStatement getStatement(String query) throws Exception {
        return Database.getStatement(query);
    }

    public ResultSet getSelectResult(String fields, String condition){
        if (Database.disconnected()) return null;
        if (!tableExists()) return null;
        String tableName = getTableName();
        try {
            PreparedStatement statement = getStatement(
                "SELECT " + fields + " FROM " + tableName +
                ((condition == null) ? "" : " WHERE " + condition));
            statement.execute();
            if (statement.execute())
                return statement.getResultSet();
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "read_table", tableName, exception.getMessage());
        }
        return null;
    }

    public long deleteFromTableByCondition(String condition){
        return deleteFromTable("DELETE FROM " + getTableName() + " WHERE " + condition);
    }

    public long deleteFromTable(PreparedStatement statement){
        if (Database.disconnected()) return 0;
        if (!tableExists()) return 0;
        try {
            long count = statement.executeUpdate();
            if (count > 0)
                Journal.add(NoteType.WARNING, "delete_table", getTableName(), Long.toString(count));
            return count;
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "delete_table", getTableName(), exception.getMessage());
            return 0;
        }

    }

    public long deleteFromTable(String query){
        if (Database.disconnected()) return 0;
        if (!tableExists()) return 0;
        try {
            return deleteFromTable(getStatement(query));
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "delete_table", getTableName(), exception.getMessage());
            return 0;
        }
    }

    public long getRows(){
        ResultSet resultSet = getSelectResult("COUNT(1)", null);
        if (resultSet == null) return 0;
        try {
            if (resultSet.next())
                return resultSet.getLong(1);
            else
                return 0;
        } catch (SQLException e) {
            return 0;
        }
    }

}
