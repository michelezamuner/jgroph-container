package net.slc.jgroph.infrastructure.container;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Container
{
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Callback> callbacks = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T make(final Class<T> type, final Object... args)
    {
        if (type == Container.class) {
            return (T)this;
        }

        if (instances.containsKey(type)) {
            return (T)instances.get(type);
        }

        if (callbacks.containsKey(type)) {
            return (T)callbacks.get(type).call(args);
        }

        if (type.isInterface()) {
            throw new ContainerError("Cannot instantiate " + type + " with no object bound.");
        }

        try {
            return createInstance(getConstructor(type, args), args);
        } catch (ReflectiveOperationException e) {
            final String message = e.getMessage() == null
                    ? "An error happened while instantiating " + type
                    : e.getMessage();
            throw new ContainerError(message, e);
        }
    }

    public <T> void bind(final Class<T> type, final @NonNull T instance)
    {
        instances.put(type, instance);
    }

    public <T> void bind(final Class<T> type, final Callback callback)
    {
        callbacks.put(type, callback);
    }

    private <T> T createInstance(final Constructor<T> constructor, final Object... args)
            throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        final Class<?>[] types = constructor.getParameterTypes();

        if (types.length == 0) {
            return constructor.newInstance();
        }

        if (args.length == 0) {
            final Object[] newArgs = Arrays.stream(types).map(type -> this.make(type)).toArray(Object[]::new);
            return constructor.newInstance(newArgs);
        }

        return constructor.newInstance(args);
    }

    private <T> Constructor<T> getConstructor(final Class<T> type, final Object... args)
    {
        if (args.length == 0) {
            return getConstructorWithNoArgs(type);
        }

        return getConstructorProvidedArgs(type, args);
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> getConstructorWithNoArgs(final Class<T> type)
    {
        final Constructor<?>[] constructors = type.getConstructors();

        if (constructors.length == 1) {
            return (Constructor<T>)constructors[0];
        }

        return getDefaultConstructor(type)
                .orElseThrow(() -> new ContainerError("Cannot instantiate " + type + " with no public constructor."));
    }

    private <T> Optional<Constructor<T>> getDefaultConstructor(final Class<T> type)
    {
        try {
            return Optional.ofNullable(type.getConstructor());
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private <T> Constructor<T> getConstructorProvidedArgs(
            final Class<T> type,
            final Object... args
    ) {
        final Class<?>[] types = Arrays.stream(args).map(this::getUnboxedType).toArray(Class[]::new);

        try {
            return type.getConstructor(types);
        } catch (NoSuchMethodException e) {
            final String message = String.format(
                    "%s has no constructor with arguments: %s",
                    type,
                    String.join(", ", Arrays.stream(types).map(Object::toString).toArray(String[]::new))
            );
            throw new ContainerError(message, e);
        }
    }

    private Class<?> getUnboxedType(final Object obj)
    {
        final Class<?> type = obj.getClass();
        if (type.equals(Boolean.class)) {
            return boolean.class;
        }
        if (type.equals(Byte.class)) {
            return byte.class;
        }
        if (type.equals(Character.class)) {
            return char.class;
        }
        if (type.equals(Float.class)) {
            return float.class;
        }
        if (type.equals(Integer.class)) {
            return int.class;
        }
        if (type.equals(Long.class)) {
            return long.class;
        }
        if (type.equals(Short.class)) {
            return short.class;
        }
        if (type.equals(Double.class)) {
            return double.class;
        }
        return type;
    }
}