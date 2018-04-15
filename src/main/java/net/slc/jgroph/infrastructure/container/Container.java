package net.slc.jgroph.infrastructure.container;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> getConstructor(final Class<T> type, final Object... args)
    {
        final Constructor<T>[] constructors = (Constructor<T>[])type.getConstructors();

        if (args.length == 0) {
            if (constructors.length > 0) {
                return constructors[0];
            }

            try {
                return type.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new ContainerError("Cannot instantiate " + type + " with no public constructor.", e);
            }
        }

        for (final Constructor<T> constructor : constructors) {
            final Class<T>[] types = (Class<T>[])constructor.getParameterTypes();
            if (types.length != args.length) {
                continue;
            }

            boolean allArgsMatching = true;
            for (int i = 0; i < types.length; i++) {
                if (!getBoxedType(types[i]).isInstance(args[i])) {
                    allArgsMatching = false;
                    break;
                }
            }
            if (allArgsMatching) {
                return constructor;
            }
        }

        final String message = String.format(
                "%s has no constructor with arguments: %s",
                type,
                String.join(", ", Arrays.stream(args)
                        .map(arg -> arg.getClass())
                        .map(Object::toString)
                        .toArray(String[]::new))
        );
        throw new ContainerError(message);
    }

    private Class<?> getBoxedType(final Class<?> type)
    {
        if (type.equals(boolean.class)) {
            return Boolean.class;
        }
        if (type.equals(byte.class)) {
            return Byte.class;
        }
        if (type.equals(char.class)) {
            return Character.class;
        }
        if (type.equals(float.class)) {
            return Float.class;
        }
        if (type.equals(int.class)) {
            return Integer.class;
        }
        if (type.equals(long.class)) {
            return Long.class;
        }
        if (type.equals(short.class)) {
            return Short.class;
        }
        if (type.equals(double.class)) {
            return Double.class;
        }
        return type;
    }
}