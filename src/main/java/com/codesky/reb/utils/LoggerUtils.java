package com.codesky.reb.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoggerUtils {
    private static final char SEPARATE = '|';

    public LoggerUtils() {
    }

    /**
     *
     * @param loggerClass           配置文件类
     * @param configClass           配置Bean类
     * @param id                    配置文件唯一标识字段
     * @param colName               配置错误的列名
     * @param desc                  错误详情
     * @param value                 字段值
     */
    public static void configError(Class<?> loggerClass, Class<?> configClass, Object id, String colName, String desc, Object value) {
        String configClassName = configClass.getSimpleName();
        configClassName = configClassName.substring(0,1).toLowerCase() + configClassName.substring(1, configClassName.length() - 4) + ".yml";
        String line = "【配置错误】" + configClassName + '|' + id + '|' + colName + '|' + desc + '|' + value;
        System.out.println(line);
    }

    public static void configError(Class<?> loggerClass, String fileName, Object id, String desc, Object value) {
        fileName = fileName + ".properties";
        String line = "【配置错误】常量配置|" + fileName + '|' + id + '|' + desc + '|' + value;
        System.out.println(line);
    }

    public static void debug(Class<?> loggerClass, Object... args) {
        Logger logger = LoggerFactory.getLogger(loggerClass);
        if (logger.isDebugEnabled()) {
            String line = getLogText(args);
            logger.debug(line);
        }

    }

    public static void log(Class<?> loggerClass, Object... args) {
        Logger logger = LoggerFactory.getLogger(loggerClass);
        if (logger.isInfoEnabled()) {
            String line = getLogText(args);
            logger.info(line);
        }

    }

    public static void error(Class<?> loggerClass, String functionName, Throwable e, Object... args) {
        Logger logger = LoggerFactory.getLogger(loggerClass);
        if (logger.isErrorEnabled()) {
            String line = getLogText(DateFormatUtils.format(System.currentTimeMillis(), "yy-MM-dd HH:mm:ss"), "AldmdException", loggerClass.getSimpleName(), functionName, args, e.getMessage());
            if (e instanceof InvocationTargetException) {
                InvocationTargetException e2 = (InvocationTargetException)e;
                logger.error(line, e2.getTargetException());
                e2.getTargetException().printStackTrace();
            } else {
                logger.error(line, e.getCause());
                e.printStackTrace();
            }
        }

    }

    private static String getValueString(Object value) {
        if (value == null) {
            return "";
        } else {
            return value instanceof Class ? ((Class)value).getName() : value.toString();
        }
    }

    public static String getLogText(Object... args) {
        StringBuilder line = new StringBuilder();
        if (args != null && args.length > 0) {
            for(int i = 0; i < args.length; ++i) {
                Object arg = args[i];
                if (arg == null) {
                    line.append("");
                } else if (arg.getClass().isArray()) {
                    for(int j = 0; j < Array.getLength(arg); ++j) {
                        Object value = Array.get(arg, j);
                        line.append(getValueString(value));
                        if (j < Array.getLength(arg) - 1) {
                            line.append('|');
                        }
                    }
                } else {
                    int index;
                    Object ob;
                    Iterator iterator;
                    if (arg instanceof Map) {
                        Map<?, ?> map = (Map)arg;
                        index = 0;

                        for(iterator = map.keySet().iterator(); iterator.hasNext(); ++index) {
                            ob = iterator.next();
                            if (ob != null) {
                                line.append(ob);
                            }

                            line.append(":");
                            Object value = map.get(ob);
                            line.append(getValueString(value));
                            if (index < map.size() - 1) {
                                line.append('|');
                            }
                        }
                    } else if (arg instanceof Collection) {
                        Collection<?> c = (Collection)arg;
                        index = 0;

                        for(iterator = c.iterator(); iterator.hasNext(); ++index) {
                            ob = iterator.next();
                            line.append(getValueString(ob));
                            if (index < c.size() - 1) {
                                line.append('|');
                            }
                        }
                    } else {
                        line.append(getValueString(args[i]));
                    }
                }

                if (i < args.length - 1) {
                    line.append('|');
                }
            }
        }

        return line.toString();
    }
}
