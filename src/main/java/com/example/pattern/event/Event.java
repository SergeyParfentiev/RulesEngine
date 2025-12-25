package com.example.pattern.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Indexed
@Component
public @interface Event {

	String value() default "";
}
