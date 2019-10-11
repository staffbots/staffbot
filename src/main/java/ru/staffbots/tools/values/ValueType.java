package ru.staffbots.tools.values;


public enum ValueType {

    LONG (true),
    DOUBLE (true),
    BOOLEAN (false),
    DATE (true),
    LIST (true),
    VOID (false);

    //true - значение в строковом представлении имеет переменную длинну
    //false - значение в строковом представлении имеет фиксированную длинну
    private boolean sizeble;

    ValueType(boolean sizeble){
        this.sizeble = sizeble;
    }

    public boolean isSizeble(){
        return sizeble;
    }

}
