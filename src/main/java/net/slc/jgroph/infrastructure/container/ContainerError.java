package net.slc.jgroph.infrastructure.container;

public class ContainerError extends Error
{
    private static final long serialVersionUID = -4418926216325118592L;

    public ContainerError(final String message)
    {
        super(message);
    }

    public ContainerError(final String message, final Throwable previous)
    {
        super(message, previous);
    }
}