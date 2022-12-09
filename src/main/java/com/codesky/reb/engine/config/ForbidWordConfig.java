package com.codesky.reb.engine.config;

public interface ForbidWordConfig {

    boolean haveForbidWord(String text);

    String filterForbidWord(String text);

}
