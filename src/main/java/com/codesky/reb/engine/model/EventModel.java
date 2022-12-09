package com.codesky.reb.engine.model;

public interface EventModel {

    <T> void sendEvent(Class<T> clazz, String methodName, Object... params);
}
