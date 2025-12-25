package com.example.pattern.event;

import com.example.pattern.common.MethodDescriptor;
import com.example.pattern.common.registry.AbstractMethodDescriptionRegistry;
import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class EventRegistry extends AbstractMethodDescriptionRegistry {

	public EventRegistry(ApplicationContext ctx) {
		Map<String, Object> events = ctx.getBeansWithAnnotation(Event.class);
		events.values().forEach(this::registerBean);
	}

	private void registerBean(Object event) {
		Class<?> clazz = event.getClass();
		Event eventAnnotation = AnnotationUtils.findAnnotation(clazz, Event.class);

		if (eventAnnotation == null) {
			throw new IllegalStateException(String.format("Class %s is missing @%s annotation",
				clazz.getName(), Event.class.getSimpleName()));
		}

		String eventPrefix = eventAnnotation.value();

		for (Method method : clazz.getMethods()) {
			String key = eventPrefix + "." + method.getName();
			functions.put(key, new MethodDescriptor(event, method));
		}
	}
}
