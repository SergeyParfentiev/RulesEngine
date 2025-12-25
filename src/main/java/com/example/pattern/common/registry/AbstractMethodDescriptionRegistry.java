package com.example.pattern.common.registry;

import com.example.pattern.common.MethodDescriptor;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMethodDescriptionRegistry implements MethodDescriptionRegistry {

	protected final Map<String, MethodDescriptor> functions = new HashMap<>();

	@Override
	public MethodDescriptor resolve(String bean, String method) {
		return functions.get(bean + "." + method);
	}
}
