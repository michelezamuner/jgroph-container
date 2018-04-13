package net.slc.jgroph.infrastructure.container;

import org.eclipse.jdt.annotation.NonNull;

import javax.annotation.Nullable;

public class Service
{
    private final Container container;

    Service(@Nullable final Container container)
    {
        this.container = container == null ? new Container() : container;
    }

    public <@NonNull T> T perform(final Class<@NonNull T> nonNullClass)
    {
        return container.make(nonNullClass);
    }
}