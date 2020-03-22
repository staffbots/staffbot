package ru.staffbots.tools.values;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.ValueDataSet;
import ru.staffbots.database.Executor;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.levers.LeverMode;
import ru.staffbots.webserver.WebServer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private static final String staticTableFields =
        "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), value BIGINT";

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

    private void init(String name, ValueMode valueMode, ValueType valueType, long value){
        this.value = value;
        this.name = (name == null ? "" : name);
        this.valueMode = valueMode;
        this.valueType = valueType;
    }

    /**
     * @param name название
     * @param note описание
     * @param value значение
     */
    public Value(String name, String note, ValueMode valueMode, ValueType valueType, long value) {
        super("val_" + name.toLowerCase(), note, staticTableFields, (valueMode == ValueMode.STORABLE));
        init(name, valueMode, valueType, value);
    }

    public Value(String name, String note, ValueType valueType, long value) {
        super("val_" + name.toLowerCase(), note, staticTableFields);
        init(name, valueMode, valueType, value);
    }

    /**
     * <b>Получить название</b><br>
     * @return название
     */
    public String getName(){
        return name;
    }

    public int getStringValueSize(){
        String valueString = toString();
        return (valueString == null) ? 0 : valueString.length();
    };

    public DBTable getTable(){
        return this;
    }

    public ArrayList<ValueDataSet> getDataSet(Period period){
        String condition = (period.getFromDate() == null) ? null : " (? <= moment) ";
        if (period.getToDate() != null)
            condition = ((condition == null) ? "" : condition + "AND") + " (moment <= ?) ";
        condition = ((condition == null) ? "" : " WHERE " + condition);
        String query = "SELECT moment, value FROM " + getTableName() + condition + " ORDER BY moment ASC";
        List<String> parameters = new ArrayList();
        if (period.getFromDate() != null)
            parameters.add(new Timestamp(period.getFromDate().getTime()).toString());
        if (period.getToDate() != null)
            parameters.add(new Timestamp(period.getToDate().getTime()).toString());
        Executor<ArrayList<ValueDataSet>> executor = new Executor();
        return executor.execQuery(
                query,
                (resultSet) -> {
                    ArrayList<ValueDataSet> dbValues = new ArrayList<>();
                    String previousValue = null;
                    while (resultSet.next()) {
                        if (getValueType() == ValueType.BOOLEAN)
                            if (previousValue !=null)
                                dbValues.add(new ValueDataSet(
                                        new Date(resultSet.getTimestamp(1).getTime()),
                                        previousValue));
                        previousValue = toValueString(resultSet.getBigDecimal(2).longValue());
                        dbValues.add(new ValueDataSet(
                                new Date(resultSet.getTimestamp(1).getTime()),
                                previousValue));
                    }
                    return dbValues;
                },
                parameters.stream().toArray(String[]::new));
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
        Journal.add(NoteType.WARNING, "set_random", name, note, Integer.toString(i));
    }

    // Изменяется ли на закладке "Управление"
    protected LeverMode leverMode = LeverMode.CHANGEABLE;

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
        boolean alreadyRecorded;
        try {
            alreadyRecorded = (newValue == tryGet(new Date()));
        } catch (Exception exception) {
            alreadyRecorded = false;
        }
        if (isStorable() && !alreadyRecorded) {
            Executor executor = new Executor(null);
            if (executor.execUpdate(
                    "INSERT INTO " + getTableName() + " (value) VALUES (?)",
                    String.valueOf(newValue)) > 0) {
                value = newValue;
                Journal.add(NoteType.INFORMATION, "set_value", getName(), getNote(), toViewString());
            }
        }
        return value;
    }

    public long set(Date moment, long newValue) {
        boolean alreadyRecorded;
        try {
            alreadyRecorded = (newValue == tryGet(moment));
        } catch (Exception exception) {
            alreadyRecorded = false;
        }
        if (isStorable() && !alreadyRecorded) {
            Executor executor = new Executor("set_value", getName(), getNote(), String.valueOf(newValue));
            if (executor.execUpdate(
                    "INSERT INTO " + getTableName() + " (moment, value) VALUES (?, ?)",
                    new Timestamp(moment.getTime()).toString(),
                    String.valueOf(newValue)) > 0) {
                value = newValue;
                Journal.add(NoteType.INFORMATION, "set_value", getName(), getNote(), toViewString());
            }
        }
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
        Executor<Long> executor = new Executor();
        return executor.execQuery(
                "SELECT value FROM " + getTableName() + " WHERE (moment <= ?) ORDER BY moment DESC LIMIT 1",
                (resultSet) -> {
                    if (resultSet.next())
                        return resultSet.getBigDecimal(1).longValue();
                    else
                        throw new Exception("No value");
                },
                new Timestamp(date.getTime()).toString());
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

