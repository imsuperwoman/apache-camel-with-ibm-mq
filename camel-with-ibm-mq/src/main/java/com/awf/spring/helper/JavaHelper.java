package com.awf.spring.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class JavaHelper extends AbstractHelper {

	public <T> T newInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		T obj = clazz.newInstance();
		return obj;
	}

	public <T> T cast(Class<T> clazz, Object obj) {
		return (T) obj;
	}

	// http://www.asgteach.com/blog/?p=559
	public List<Method> findGetters(Class<?> clazz) {
		List<Method> list = Lists.newArrayList();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (isGetter(method)) {
				list.add(method);
			}
		}
		return list;
	}

	public boolean isGetter(Method method) {
		if (Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0) {
			if (method.getName().matches("^get[A-Z].*")
					&& !method.getReturnType().equals(void.class)) {
				return true;
			}
			if (method.getName().matches("^is[A-Z].*")
					&& method.getReturnType().equals(boolean.class)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSetter(Method method) {
		return Modifier.isPublic(method.getModifiers())
				&& method.getReturnType().equals(void.class)
				&& method.getParameterTypes().length == 1
				&& method.getName().matches("^set[A-Z].*");
	}

	public Object invokeStaticMethod(Class<?> clazz, String methodName, Object[] args,
			Class<?>[] parameterTypes) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		return method.invoke(null, args);
	}

}
