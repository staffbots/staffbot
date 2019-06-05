package ru.staffbot.database;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
                Statement statement = Database.getConnection().createStatement();
                statement.execute("CREATE TABLE " + tableName + " (" + tableFields + ")");
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
                Statement statement = Database.getConnection().createStatement();
                statement.execute("DELETE FROM " + tableName);
                statement.close();
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
                Statement statement = Database.getConnection().createStatement();
                statement.execute("DROP TABLE " + tableName);
                Journal.add("Удалена таблица " + tableName, NoteType.WRINING);
            }
        } catch (Exception exception) {
            Journal.add("Не удалось удалить таблицу " + tableName + exception.getMessage(), NoteType.ERROR);
            return false;
        }
        return true;
    }

}
