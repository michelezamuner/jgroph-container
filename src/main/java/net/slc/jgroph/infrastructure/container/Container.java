package net.slc.jgroph.infrastructure.container;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Container
{
    private final Map<Class<@NonNull ?>, Object> instances = new HashMap<>();
    private final Map<Class<@NonNull ?>, Callback> callbacks = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <@NonNull T> T make(final Class<@NonNull T> type, final Object... args)
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
            return createInstance(getConstructor(type), args);
        } catch (ReflectiveOperationException e) {
            throw new ContainerError(e.getMessage(), e);
        }
    }

    public <@NonNull T> void bind(final Class<@NonNull T> type, final @NonNull T instance)
    {
        instances.put(type, instance);
    }

    public <@NonNull T> void bind(final Class<@NonNull T> type, final Callback callback)
    {
        callbacks.put(type, callback);
    }

    @SuppressWarnings("unchecked")
    private <@NonNull T> Constructor<@NonNull T> getConstructor(final Class<@NonNull T> type)
    {
        final Constructor[] constructors = type.getConstructors();

        if (constructors.length > 1) {
            throw new ContainerError("Cannot instantiate " + type + " with multiple constructors.");
        }

        if (constructors.length == 1) {
            return (Constructor<@NonNull T>)constructors[0];
        }

        return getDefaultConstructor(type)
                .orElseThrow(() -> new ContainerError("Cannot instantiate " + type + " with no public constructor."));
    }

    @SuppressWarnings("unchecked")
    private <@NonNull T> T createInstance(final Constructor<@NonNull T> constructor, final Object... args)
            throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        final Class[] params = constructor.getParameterTypes();

        if (params.length == 0) {
            return constructor.newInstance();
        }

        if (args.length == 0) {
            final Object[] actual = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                actual[i] = make(params[i]);
            }
            return constructor.newInstance(actual);
        }

        if (params.length != args.length) {
            final Class type = constructor.getDeclaringClass();
            throw new ContainerError("Cannot instantiate " + type + " with partial explicit arguments.");
        }

        return constructor.newInstance(args);
    }

    private <@NonNull T> Optional<Constructor<@NonNull T>> getDefaultConstructor(final Class<@NonNull T> type)
    {
        try {
            return Optional.ofNullable(type.getConstructor());
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}