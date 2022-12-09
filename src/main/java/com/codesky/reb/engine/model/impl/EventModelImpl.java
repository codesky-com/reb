package com.codesky.reb.engine.model.impl;


import com.codesky.reb.engine.model.EventModel;
import com.codesky.reb.utils.LoggerUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

@Component
public class EventModelImpl implements EventModel, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> void sendEvent(Class<T> clazz, String methodName, Object... params) {
        Map<String, T> beans = this.applicationContext.getBeansOfType(clazz);
        Iterator iterator = beans.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, T> entry = (Map.Entry)iterator.next();
            try {
                long t = System.currentTimeMillis();
                if (null == params) {
                    params = ArrayUtils.EMPTY_OBJECT_ARRAY;
                }

                int arguments = params.length;
                Class[] paramClass = new Class[arguments];
                for (int i = 0; i < arguments; i++) {
                    paramClass[i] = params[i].getClass();
                }

                Method method = ReflectionUtils.findMethod(entry.getValue().getClass(), methodName, paramClass);
                method.invoke(entry.getValue(),params);
                t = System.currentTimeMillis() - t;
                if (t > 100L) {
                    LoggerUtils.log(this.getClass(), new Object[]{"sendEvent", "slow", entry.getKey(), methodName, t});
                }

            } catch (Exception e) {
                LoggerUtils.error(this.getClass(), "sendEvent", e, new Object[]{entry.getKey(), clazz.getName(), entry.getValue().getClass().getName(), methodName, params});
            }
        }
    }
}
