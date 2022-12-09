package com.codesky.reb.engine.model;

public interface ForbidWordModel {

    Boolean isForbidWord(String text);

    String processInput(String text);

    String processTrim(String text);

    String filterForbidWord(String text);
}
