package ru.staffbots.database;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.sql.*;

/*
 * Таблица БД,
 * Предоставляет интерфейс общий для всех таблиц БД,
 * является родителем для классов Value, Journal, Settings, Configs
 */
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

    public DBTable(String tableName, String tableFields, boolean isStorable){
        this.tableName = tableName;
        this.tableFields = tableFields;
        if (isStorable) createTable();
    }

    public boolean tableExists(){
        if(Database.disconnected())return false;
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
        if (Database.disconnected()) return false;
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
        if(Database.disconnected())return false;
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
        if(Database.disconnected())return false;
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

    public PreparedStatement getStatement(String query) throws Exception {
        if (Database.disconnected())
            throw Database.getException();
        return Database.getConnection().prepareStatement(query);
    }

    public ResultSet getSelectResult(String fields, String condition){
        if (Database.disconnected()) return null;
        if (!tableExists()) return null;
        try {
            PreparedStatement statement = getStatement(
                "SELECT " + fields + " FROM " + getTableName() +
                ((condition == null) ? "" : " WHERE " + condition));
            statement.execute();
            if (statement.execute())
                return statement.getResultSet();
        } catch (Exception exception) {
            Journal.add(exception.getMessage(), NoteType.ERROR);
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
                Journal.add("Удаление в таблице " + getTableName() + ". Удалено записей: " + count);
            return count;
        } catch (Exception exception) {
            return 0;
        }

    }

    public long deleteFromTable(String query){
        if (Database.disconnected()) return 0;
        if (!tableExists()) return 0;
        try {
            return deleteFromTable(getStatement(query));
        } catch (Exception exception) {
            Journal.add(exception.getMessage(), NoteType.ERROR);
            return 0;
        }
    }

    public long getRecordsCount(){
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
