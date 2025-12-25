package com.example.pattern.condition;

import com.example.pattern.common.MethodDescriptor;
import com.example.pattern.common.registry.AbstractMethodDescriptionRegistry;
import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ConditionRegistry extends AbstractMethodDescriptionRegistry {

	public ConditionRegistry(ApplicationContext ctx) {
		Map<String, Condition> conditions = ctx.getBeansOfType(Condition.class);
		conditions.values().forEach(this::registerCondition);
	}

	private void registerCondition(Condition condition) {
		Class<?> clazz = condition.getClass();
		String name = clazz.getSimpleName();
		if (name.endsWith("Condition")) {
			name = name.substring(0, name.length() - "Condition".length());
		}
		name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

		for (Method m : clazz.getMethods()) {
			String key = name + "." + m.getName();
			functions.put(key, new MethodDescriptor(condition, m));
		}
	}
}
