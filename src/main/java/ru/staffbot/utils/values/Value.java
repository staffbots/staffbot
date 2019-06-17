package ru.staffbot.utils.values;

import ru.staffbot.database.DBTable;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.webserver.WebServer;
import ru.staffbot.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

/**
 * <b>Контейнер значения</b> содержит именованное значение типа {@code Double}.<br>
 * Его дочерние классы предоставляют интерфейс для работы со значениями других типов данных:<br>
 *  - {@code BooleanValue} для значений типа {@code Boolean}<br>
 *  - {@code DoubleValue} для значений типа {@code Double}<br>
 *  - {@code DateValue} для значений типа {@code Date}<br>
 *  - {@code LongValue} для значений типа {@code Long}<br>
 *  - {@code EmptyValue} для внутренних нужд, а именно - для группировки рычагов управлкния в {@link WebServer}.<br>
 * Однако, вне зависимости от типа предоставляемого значения, все перечисленные дочерние классы для хранения используют тип Double,
 * используя функции преобразования типов из {@link Converter}.<br>
 * Благодоря этому, Value и все его дочерние классы имееют унифицированный интерфейс
 * и единый тип значений - {@code Double}, что значительно облегчает работу с БД.
 */
abstract public class Value extends DBTable {

    public static final String DB_TABLE_FIELDS = "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), value BIGINT";

    protected ValueType valueType;

    public ValueType getValueType() {
        return valueType;
    }

    /**
     * <b>Название</b><br>
     * соответствует имени таблицы в БД.
     */
    protected String name;

    /**
     * <b>Описание</b>,
     * используется при формировании веб-интерфейса в {@link WebServer}.
     */
    protected String note;

    /**
     * <b>Хранение в БД</b>:<br>
     *  - если да ({@code dbStorage = true}), значение хранится в БД, в value дублируется;<br>
     *  - если нет ({@code dbStorage = false}), значение хранится в value, БД не задействовано.<br>
     * нициализируется в конструкторе.
     */
    public boolean dbStorage;

    public boolean getDbStorage() {
        return dbStorage;
    }
    /**
     * <b>Значение</b>, используется при {@code dbStorage = false},<br>
     * в противном случае сюда дублируется значение из БД.
     */
    protected long value;


    /**
     * <b>Получить дату</b> последнего изменения
     * @return дата последнего изменения
     */
    public Date getDate(){
        // Вытаскиваем из БД
        return new Date();
    }

    private void init(String name, String note, long value, ValueType valueType, Boolean dbStorage){
        this.value = value;
        this.name = name;
        this.note = note;
        this.dbStorage = dbStorage;
        this.valueType = valueType;
        if(dbStorage) this.value = get();
    }
    /**
     * @param name название
     * @param note описание
     * @param dbStorage признак хранения в БД
     * @param value значение
     */
    public Value(String name, String note, long value, ValueType valueType, Boolean dbStorage) {
        super("val_" + name.toLowerCase(), DB_TABLE_FIELDS, dbStorage);
        init(name, note, value, valueType, dbStorage);
    }

    public Value(String name, String note, long value, ValueType valueType) {
        super("val_" + name.toLowerCase(), DB_TABLE_FIELDS);
        init(name, note, value, valueType, true);
    }

    /**
     * <b>Получить значение</b> на указанную дату<br>
     * При этом, если ({@code dbStorage = true}), то значение на дату по честному ищется в БД,
     * в противном случае, значение просто берётся из {@code value}.<br>
     * @param date дата
     * @return значение из на указанную дату
     */
    public long get(Date date) {
        long dbValue;
        try {
            if(!dbStorage) return value;
            if(!Database.connected()) throw new Exception("Нет подключения к базе данных");
            PreparedStatement ps = Database.getConnection().prepareStatement(
                    "SELECT value FROM " + getTableName() + " WHERE (moment <= ?) ORDER BY moment DESC LIMIT 1");
            ps.setTimestamp(1, new Timestamp(date.getTime()));
            if (ps.execute()) {
                ResultSet rs = ps.getResultSet();
                //return (rs.next() ? rs.getDouble(0) : Double.NaN);
                if (rs.next())
                    return rs.getBigDecimal(1).longValue();
                else
                    throw new Exception("Таблица значений пуста, впрочем как и все феномены этой жизни...");
            } else
                throw new Exception("Значение не найдено в базе данных");

        } catch (Exception e){
            dbValue = value;
        }
        return dbValue;
    }

    /**
     * <b>Получить значение</b><br>
     * При этом, если ({@code dbStorage = true}), то значение берётся из БД,
     * в противном случае - из {@code value}.<br>
     * @return значение
     */
    public long get() {
        return get(new Date());
    }

    /**
     * <b>Установить</b> значение<br>
     * @param newValue - устанавлевоемое значение
     * @return установленное значение
     */
    synchronized public long set(long newValue) {
        if (dbStorage)
            if (newValue != get()) {
                try {
                    if(!Database.connected()) throw new Exception("Нет подключения к базе данных");
                    PreparedStatement statement = Database.getConnection().prepareStatement(
                            "INSERT INTO " + getTableName() +
                                    " (value) VALUES (?)");
                    statement.setLong(1, newValue);
                    statement.executeUpdate();
                    String stringValue = (valueType != ValueType.BOOLEAN) ? getValueAsString() : Long.toString(newValue);
                    Journal.add(getNote() + " - установлено заначение: " + stringValue);
                } catch (Exception e) {
                    Journal.add("Ошибка записи в таблицу " + getTableName() + e.getMessage(), NoteType.ERROR);
                }
            }
        value = newValue;
        return value;
    }

    /**
     * <b>Сбросить</b> значение на заачение по умолчанию ({@code defaultValue})
     */
    public void reset() {
        value = 0;
    }

    /**
     * <b>Получить название</b><br>
     * @return название
     */
    public String getName(){
        return name;
    }

    /**
     * <b>Получить описание</b><br>
     * @return описание
     */
    public String getNote(){
        return note;
    }




    public static int stringValueSize = 1;
    /**
     */
    public int getStringValueSize(){
        return (stringValueSize < 0) ? getValueAsString().length() : stringValueSize;
    };
    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    public String getValueAsString(){
        return Long.toString(get());
    }

    public void setValueFromString(String value){
        if(value != null)
            set(Long.parseLong(value));
    }

    public DBTable getTable(){
        return this;
    }
}

