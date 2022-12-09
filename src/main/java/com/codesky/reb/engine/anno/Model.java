package com.codesky.reb.engine.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Model {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
