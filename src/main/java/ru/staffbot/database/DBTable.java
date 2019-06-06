package ru.staffbot.database;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;

import java.sql.*;

public abstract class DBTable {

    private String tableName;
    private String tableFields;

    public String getTableName(){
        return tableName;
    }

    public DBTable(String tableName, String tableFields){
        this.tableName = tableName;
        this.tableFields = tableFields;
        createTable();
    }

    public DBTable(String tableName, String tableFields, boolean dbStorage){
        this.tableName = tableName;
        this.tableFields = tableFields;
        if (dbStorage) createTable();
    }

    public boolean tableExists(){
        if(!Database.connected())return false;
        try {
            DatabaseMetaData metaData = Database.getConnection().getMetaData();
            ResultSet tables = metaData.getTables(Database.NAME, null, tableName, null);
            return (tables.next());
        } catch (SQLException exception) {
            Journal.add("Ошибка поиска таблицы " + tableName + " - " + exception.getMessage(), NoteType.ERROR);
            return false;
        }
    }

    public boolean createTable(){
        return createTable(false);
    }

    public boolean createTable(boolean drop){
        if (!Database.connected()) return false;
        if (drop) dropTable();
        if (!tableExists())
            try {
                getStatement("CREATE TABLE " + tableName + " (" + tableFields + ")").execute();
                Journal.add("Создана таблица " + tableName + " (" + tableFields + ")", NoteType.WRINING);
            } catch (Exception exception) {
                //connection = null;
                Journal.add("Не удалось создать таблицу " + tableName + " - " + exception.getMessage(), NoteType.ERROR);
                return false;
            }

        return true;
    }

    public boolean eraseTable(){
        if(!Database.connected())return false;
        try {
            if (tableExists()) {
                getStatement("DELETE FROM " + tableName).execute();
                Journal.add("Таблица " + tableName + " очищена");
                return true;
            }
        } catch (Exception exception) {
            Journal.add(exception.getMessage(), NoteType.ERROR);
        }
        return false;
    }

    public boolean dropTable(){
        if(!Database.connected())return false;
        try {
            if (tableExists()){
                getStatement("DROP TABLE " + tableName).execute();
                Journal.add("Удалена таблица " + tableName, NoteType.WRINING);
            }
        } catch (Exception exception) {
            Journal.add("Не удалось удалить таблицу " + tableName + exception.getMessage(), NoteType.ERROR);
            return false;
        }
        return true;
    }

    protected PreparedStatement statement;

    protected PreparedStatement getStatement(String query) throws Exception {
        if (!Database.connected())
            throw Database.getException();
        return Database.getConnection().prepareStatement(query);
    }

    public ResultSet getSelectResult(String fields, String condition){
        if (!Database.connected()) return null;
        if (!tableExists()) return null;
        try {
            PreparedStatement statement = getStatement(
                    "SELECT " + fields + " FROM " + getTableName() + " WHERE " + condition);
            statement.execute();
            if (statement.execute())
                return statement.getResultSet();
        } catch (Exception exception) {
            Journal.add(exception.getMessage(), NoteType.ERROR);
        }
        return null;
    }

    public int deleteFromTable(String condition){
        if (!Database.connected()) return 0;
        if (!tableExists()) return 0;
        try {
            PreparedStatement statement = getStatement(
                    "DELETE FROM " + getTableName() + " WHERE " + condition);
            return statement.executeUpdate();
        } catch (Exception exception) {
            Journal.add(exception.getMessage(), NoteType.ERROR);
            return 0;
        }
    }

}
