package ru.staffbots.tools.resources;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum ResourceType {

    HTML,
    JS,
    CSS,
    IMG;

    @Override
    public String toString(){
        return super.toString().toLowerCase();
    }

}
