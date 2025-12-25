package com.example.pattern.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class MethodDescriptor {

    private final Object bean;
    private final Method method;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MethodDescriptor) obj;
        return Objects.equals(this.bean, that.bean) &&
                Objects.equals(this.method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bean, method);
    }

    @Override
    public String toString() {
        return "MethodDescriptor[" +
                "bean=" + bean + ", " +
                "method=" + method + ']';
    }

}
