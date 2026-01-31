package com.example.pattern.event;

import com.example.pattern.common.MethodDescriptor;
import com.example.pattern.common.registry.AbstractMethodDescriptionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class PatternEventRegistry extends AbstractMethodDescriptionRegistry {

    public PatternEventRegistry(ApplicationContext ctx) {
        Map<String, Object> events = ctx.getBeansWithAnnotation(PatternEvent.class);
        events.values().forEach(this::registerEvent);
    }

    private void registerEvent(Object event) {
        Class<?> clazz = event.getClass();
        PatternEvent annotation = AnnotationUtils.findAnnotation(clazz, PatternEvent.class);

        if (annotation == null) {
            throw new IllegalStateException(String.format("Class %s is missing @%s annotation",
                    clazz.getName(), PatternEvent.class.getSimpleName()));
        }

        String eventPrefix = annotation.value();

        for (Method method : clazz.getMethods()) {
            String key = eventPrefix + "." + method.getName();
            functions.put(key, new MethodDescriptor(event, method));
        }
    }
}
