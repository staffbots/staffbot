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
            if (new Executor().execUpdate("CREATE TABLE IF NOT EXISTS " + name + " (" + fields + ")") > 0)
                Journal.add(NoteType.WARNING, "create_table", name, fields);
        } catch (Exception exception) {
            //connection = null;
            Journal.add(NoteType.ERROR, "create_table", name, exception.getMessage());
            return false;
        }
        return true;
    }

    public void eraseTable(){
        Executor executor = new Executor("erase_table", name);
        executor.execUpdate("DELETE FROM " + name);
    }

    public boolean dropTable(){
        return Database.dropTable(name);
    }

    public long deleteFromTableByCondition(String condition){
        return deleteFromTable("DELETE FROM " + getTableName() + " WHERE " + condition);
    }

    public long deleteFromTable(String update){
        Executor executor = new Executor("delete_table", getTableName());
        return executor.execUpdate(update);
    }

    public long getRows(){
        Executor<Long> executor = new Executor();
        return executor.execQuery(
                "SELECT COUNT(1) FROM " + getTableName(),
                (resultSet) -> {
                    return resultSet.next() ? resultSet.getLong(1) : 0;
                });
    }

}
