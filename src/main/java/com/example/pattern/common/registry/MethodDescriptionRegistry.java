package com.example.pattern.common.registry;

import com.example.pattern.common.MethodDescriptor;

public interface MethodDescriptionRegistry {

	MethodDescriptor resolve(String bean, String method);
}
