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

    public DBTable(String tableName, String sql, boolean dbStorage){
        this.tableName = tableName;
        if (dbStorage) createTable();
    }

    public boolean tableExists(String tableName) throws Exception{
        if(!Database.connected())return false;
        boolean result = false;
        DatabaseMetaData metaData = Database.getConnection().getMetaData();
        ResultSet rs = metaData.getTables(tableName,"","%",null);
        while (rs.next()) {
            if (tableName.toLowerCase().equals(rs.getString(3))) {
                result = true;
                break;
            }
        }
        rs.close();
        return result;
    }

    public boolean createTable(){
        return createTable(false);
    }

    public boolean createTable(boolean drop){
        if(!Database.connected())return false;
        try {
            if (drop) dropTable();
            if(!tableExists(tableName)) {
                Statement statement = Database.getConnection().createStatement();
                statement.execute("CREATE TABLE " + tableName + " (" + tableFields + ")");
                Journal.add("Создана таблица " + tableName + " (" + tableFields + ")", NoteType.WRINING);
            }
        } catch (Exception e) {
            //connection = null;
            Journal.add("Не удалось создать таблицу " + tableName + e.getMessage(), NoteType.ERROR);
            return false;
        }
        return true;
    }

    public boolean eraseTable(){
        if(!Database.connected())return false;
        try {
            Statement statement = Database.getConnection().createStatement();
            statement.execute("DELETE FROM " + tableName);
            statement.close();
            Journal.add("Таблица " + tableName + " очищена");
            return true;
        } catch (SQLException e) {
            Journal.add(e.getMessage(), NoteType.ERROR);
            return false;
        }
    }

    public boolean dropTable(){
        if(!Database.connected())return false;
        try {
            if (tableExists(tableName)){
                Statement statement = Database.getConnection().createStatement();
                statement.execute("DROP TABLE " + tableName);
                Journal.add("Удалена таблица " + tableName, NoteType.WRINING);
            }
        } catch (Exception e) {
            //connection = null;
            Journal.add("Не удалось создать таблицу " + tableName + e.getMessage(), NoteType.ERROR);
            return false;
        }
        return true;
    }

}
