package com.example.rule.common;

import java.lang.reflect.Method;

public record MethodDescriptor(Object bean, Method method) {
}
