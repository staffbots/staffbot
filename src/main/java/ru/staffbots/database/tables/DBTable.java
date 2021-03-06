package ru.staffbots.database.tables;

import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;

/*
 * Database table interface for Journal, Variables, Users, LeversSets and Value
 */
public abstract class DBTable {

    private String name;

    private String fields;

    public String getTableName(){
        return name;
    }

    public DBTable(String name, String fields){
        this.name = name;
        this.fields = fields;
        createTable();
    }

    public DBTable(String name, String fields, boolean isStorable){
        this.name = name;
        this.fields = fields;
        if (isStorable) createTable();
    }

    public boolean tableExists(){
        return (Database.findTables(name).size() > 0);
    }

    public boolean createTable(){
        return createTable(false);
    }

    public boolean createTable(boolean drop){
        if (drop) dropTable();
        if (!drop && tableExists())
            return true;
        Executor executor = new Executor("create_table", name);
        return (executor.execUpdate("CREATE TABLE IF NOT EXISTS " + name + " (" + fields + ")") > 0);
    }

    public void eraseTable(){
        Executor executor = new Executor("erase_table", name);
        executor.execUpdate("DELETE FROM " + name);
    }

    public boolean dropTable(){
        Executor executor = new Executor("drop_table", name);
        return  (executor.execUpdate("DROP TABLE IF EXISTS " + name) > 0);
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
