package ru.staffbots.tools.values;

public enum ValueType {

    LONG (true),
    DOUBLE (true),
    BOOLEAN (false),
    DATE (true),
    LIST (true),
    VOID (false);

    private boolean sizeble;

    ValueType(boolean sizeble){
        this.sizeble = sizeble;
    }

    public boolean isSizeble(){
        return sizeble;
    }
}
