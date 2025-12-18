package com.example.rule.common.registry;

import com.example.rule.common.MethodDescriptor;

public interface MethodDescriptionRegistry {

	MethodDescriptor resolve(String bean, String method);
}
