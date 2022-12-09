package com.codesky.reb.engine.model.impl;

import com.codesky.reb.Service;
import com.codesky.reb.engine.anno.Checker;
import com.codesky.reb.engine.anno.Model;
import com.codesky.reb.engine.interfaces.GameConfig;
import com.codesky.reb.engine.model.GameConfigModel;
import com.codesky.reb.utils.LoggerUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Model("gameConfigModel")
public class GameConfigModelImpl implements GameConfigModel, ApplicationContextAware, Service {

    private ApplicationContext applicationContext;
    private Map<Class<?>,Object> gameCheckers;

    @Override
    public <T> T getChecker(Class<T> checkClass) {
        return (T)this.gameCheckers.get(checkClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Map<String, Object> beanMap = this.applicationContext.getBeansWithAnnotation(Checker.class);
        Iterator<Object> iterator = beanMap.values().iterator();

        this.gameCheckers = new HashMap<>(beanMap.values().size());
        while (iterator.hasNext()) {
            Object bean = iterator.next();
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (anInterface.getAnnotation(Checker.class) != null) {
                    this.gameCheckers.put(anInterface, bean);
                    LoggerUtils.debug(this.getClass(), "FindChecker", anInterface.getName());
                }
            }
        }

        LoggerUtils.log(this.getClass(), "【FindChecker】", this.gameCheckers.keySet());
    }

    public void initAndCheck() {
        Map<String, GameConfig> configMap = this.applicationContext.getBeansOfType(GameConfig.class);
        GameConfigModel checker = this;

        Iterator<Map.Entry<String, GameConfig>> iteratorInit = configMap.entrySet().iterator();
        while (iteratorInit.hasNext()) {
            Map.Entry entry = iteratorInit.next();
            GameConfig gameConfig = (GameConfig)entry.getValue();
            try {
                gameConfig.init();
            } catch (Exception e) {
                LoggerUtils.error(this.getClass(), "initAndCheck", e, "init", entry.getKey(), gameConfig.getClass());
            }
        }
        LoggerUtils.log(this.getClass(), "GameConfigModel", "finish_init_config");

        Iterator<Map.Entry<String, GameConfig>> iteratorCheck = configMap.entrySet().iterator();
        while (iteratorCheck.hasNext()) {
            Map.Entry entry = iteratorCheck.next();
            GameConfig gameConfig = (GameConfig)entry.getValue();
            try {
                gameConfig.check(checker);
                LoggerUtils.debug(this.getClass(), "check_config", entry.getKey(), ((GameConfig)entry.getValue()).getClass().getSimpleName());
            } catch (Exception e) {
                LoggerUtils.error(this.getClass(), "initAndCheck", e, "check",entry.getKey(),gameConfig.getClass());
            }
        }

        LoggerUtils.log(this.getClass(),"GameConfigModel", "finish_check_config");
    }


    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void start() {
        this.initAndCheck();
        LoggerUtils.log(this.getClass(),"service.gameConfigModel.start");
    }

    @Override
    public void stop() {}

    @Override
    public void tick(long ms) {

    }
}
