package ru.staffbots.tools.values;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.DBValue;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.levers.LeverMode;
import ru.staffbots.webserver.WebServer;
import ru.staffbots.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * <b>Контейнер значения</b> содержит именованное значение типа {@code Double}.<br>
 * Его дочерние классы предоставляют интерфейс для работы со значениями других типов данных:<br>
 *  - {@code BooleanValue} для значений типа {@code Boolean}<br>
 *  - {@code DoubleValue} для значений типа {@code Double}<br>
 *  - {@code DateValue} для значений типа {@code Date}<br>
 *  - {@code LongValue} для значений типа {@code Long}<br>
 *  - {@code VoidValue} для внутренних нужд, а именно - для группировки рычагов управлкния в {@link WebServer}.<br>
 * Однако, вне зависимости от типа предоставляемого значения, все перечисленные дочерние классы для хранения используют тип Long
 * Благодоря этому, Value и все его дочерние классы имееют унифицированный интерфейс
 * и единый тип значений - {@code long}, что значительно облегчает работу с БД.
 */
abstract public class Value extends DBTable {

    private static final String DB_TABLE_FIELDS = "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), value BIGINT";

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

    private void init(String name, String note, ValueMode valueMode, ValueType valueType, long value){
        this.value = value;
        this.name = (name == null ? "" : name);
        this.note = (note == null ? "" : note);
        this.valueMode = valueMode;
        this.valueType = valueType;
    }

    /**
     * @param name название
     * @param note описание
     * @param value значение
     */
    public Value(String name, String note, ValueMode valueMode, ValueType valueType, long value) {
        super("val_" + name.toLowerCase(), DB_TABLE_FIELDS, (valueMode == ValueMode.STORABLE));
        init(name, note, valueMode, valueType, value);
    }

    public Value(String name, String note, ValueType valueType, long value) {
        super("val_" + name.toLowerCase(), DB_TABLE_FIELDS);
        init(name, note, valueMode, valueType, value);
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

    public int getStringValueSize(){
        String valueString = toString();
        return (valueString == null) ? 0 : valueString.length();
    };

    public DBTable getTable(){
        return this;
    }

    public ArrayList<DBValue> getDataSet(Period period){
        ArrayList<DBValue> dbValues = new ArrayList<>();
        try {
            String condition = (period.getFromDate() == null) ? null : " (? <= moment) ";
            if (period.getToDate() != null)
                condition = ((condition == null) ? "" : condition + "AND") + " (moment <= ?) ";
            condition = ((condition == null) ? "" : " WHERE " + condition);

            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "SELECT moment, value FROM " + getTableName()
                            + condition + " ORDER BY moment ASC");

            if (period.getFromDate() != null)
                statement.setTimestamp(1,
                    new Timestamp(period.getFromDate().getTime()));

            if (period.getToDate() != null)
                statement.setTimestamp((period.getFromDate() == null) ? 1 : 2,
                    new Timestamp(period.getToDate().getTime()));

            if(statement.execute()) {
                ResultSet resultSet = statement.getResultSet();
                String previousValue = null;
                while (resultSet.next()) {
                    if (getValueType() == ValueType.BOOLEAN)
                        if (previousValue !=null)
                            dbValues.add(new DBValue(
                                    new Date(resultSet.getTimestamp(1).getTime()),
                                    previousValue));
                    previousValue = toValueString(resultSet.getBigDecimal(2).longValue());
                    dbValues.add(new DBValue(
                            new Date(resultSet.getTimestamp(1).getTime()),
                            previousValue));
                }
            }

        } catch (SQLException exception) {
            Journal.add("Value " + getName() + ": " + exception.getMessage(), NoteType.ERROR);
        }



        return dbValues;
    }

    // Возможено ли построение графика
    public boolean isPlotPossible(){
        return isStorable() && (
            (valueType == ValueType.BOOLEAN) ||
            (valueType == ValueType.LONG) ||
            (valueType == ValueType.DOUBLE));
    }

    public void setRandom(Period period){
        double average = Math.random() * 30;
        double dispersion = Math.random() * 10;
        long count = 35;
        setRandom(period, count, average, dispersion);
    }

    public void setRandom(Period period, long count, double average, double dispersion){
        if (!isPlotPossible()) return;
        eraseTable();
        //long timePeriod = period.getDuration();
        long moment = period.getFromDate().getTime();
        double randomValue;
        int i = 0;
        while (moment < period.getToDate().getTime()) {
            moment += (Math.random() + 0.5) * period.getDuration() / count;
            randomValue = average + (Math.random() - 0.5) * dispersion;
            long newValue = 0;
            if (valueType == ValueType.DOUBLE)
                newValue = Double.doubleToLongBits(randomValue);
            if (valueType == ValueType.BOOLEAN)
                newValue = Math.round(Math.random());
            if (valueType == ValueType.LONG)
                newValue = Math.round(randomValue);
            set(new Date(moment), newValue);
            i++;
        }
        Journal.add(name + ". Всего добавлено: " + i, NoteType.ERROR);
    }

    protected LeverMode leverMode = LeverMode.CHANGEABLE;

    // Является ли это параметром, который изменяется на закладке "Управление"
    public boolean isChangeable(){
        return (leverMode == LeverMode.CHANGEABLE);
    }

    public LeverMode getLeverMode() {
        return leverMode;
    }

    protected ValueMode valueMode = ValueMode.STORABLE;

    // Сохраняется ли история изменения в БД
    public boolean isStorable(){
        return (valueMode == ValueMode.STORABLE);
    }

    public ValueMode getValueMode() {
        return valueMode;
    }

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    /**
     * <b>Установить</b> значение<br>
     * @param newValue - устанавлевоемое значение
     * @return установленное значение
     */
    public long set(long newValue) {

        boolean allow;
        try {
            allow = (newValue != tryGet(new Date()));
        } catch (Exception exception) {
            allow = true;
        }
        boolean valueChanged = false;
        if (isStorable() && allow)
            try {
                if (Database.disconnected()) throw new Exception("Нет подключения к базе данных");
                PreparedStatement statement = Database.getConnection().prepareStatement(
                        "INSERT INTO " + getTableName() +
                                " (value) VALUES (?)");
                statement.setLong(1, newValue);
                statement.executeUpdate();
                valueChanged = true;
            } catch (Exception exception) {
                Journal.add("Ошибка записи в таблицу " + getTableName() + exception.getMessage(), NoteType.ERROR);
            }
        value = newValue;
        if (valueChanged) Journal.add(getNote() + " - установлено значение: " + toValueString(value));
        return value;
    }

    public long set(Date moment, long newValue) {
        boolean allow;
        try {
            allow = (newValue != tryGet(moment));
        } catch (Exception exception) {
            allow = true;
        }
        boolean valueChanged = false;
        if (isStorable() && allow)
            try {
                if (Database.disconnected()) throw new Exception("Нет подключения к базе данных");
                PreparedStatement statement = Database.getConnection().prepareStatement(
                        "INSERT INTO " + getTableName() +
                                " (moment, value) VALUES (?, ?)");
                statement.setTimestamp(1, new Timestamp(moment.getTime()));
                statement.setLong(2, newValue);
                statement.executeUpdate();
                valueChanged = true;
            } catch (Exception e) {
                Journal.add("Ошибка записи в таблицу " + getTableName() + e.getMessage(), NoteType.ERROR);
            }
        value = newValue;
        if (valueChanged)
            Journal.add(getNote() + " - установлено значение: " + toValueString(value)
                    + " на дату " + DateValue.toString(moment, Journal.DATE_FORMAT));
        return value;
    }

    /**
     * <b>Получить значение</b><br>
     * @return значение
     */
    public long get() {
        return get(new Date());
    }

    public long get(Date date) {
        try {
            return tryGet(date);
        } catch (Exception exception){
            return value;
        }
    }

    /**
     * <b>Получить значение</b> на указанную дату<br>
     */
    private long tryGet(Date date) throws Exception{
        if(!isStorable()) return value;
        if(Database.disconnected()) throw new Exception("Нет подключения к базе данных");
        PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT value FROM " + getTableName() + " WHERE (moment <= ?) ORDER BY moment DESC LIMIT 1");
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        if (ps.execute()) {
            ResultSet rs = ps.getResultSet();
            if (rs.next())
                return rs.getBigDecimal(1).longValue();
            else
                throw new Exception("Таблица значений пуста, впрочем как и все феномены этой жизни...");
        } else
            throw new Exception("Значение не найдено в базе данных");
    }

    /**
     * <b>Сбросить</b> значение на заачение по умолчанию ({@code defaultValue})
     */
    abstract public void reset();

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/

    /**
     * <b>Получить значение для отображения</b><br>
     */
    @Override
    // Используется для вывода значения по умолчанию
    public String toString(){
        return toValueString();
    }

    // Используется для ввода значения
    public String toHtmlString(){
        return toString();
    }

    // Используется для вывода значения
    public String toViewString(){
        return toString();
    }

    // Используется для выгрузки графиков
    public String toValueString(){
        return toValueString(get());
    }

    // Используется для выгрузки графиков
    public String toValueString(long value){
        return Long.toString(value);
    }

    // Устанавливает значение из строки value
    // Переопределён для каждого (дочернего) типа заначений
    public abstract void setFromString(String value);

    public Value toValue(){
        return this;
    }

}

