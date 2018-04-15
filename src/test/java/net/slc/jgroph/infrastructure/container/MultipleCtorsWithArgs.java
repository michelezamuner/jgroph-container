package net.slc.jgroph.infrastructure.container;

public class MultipleCtorsWithArgs
{
    private String value;

    public MultipleCtorsWithArgs(final SimpleDouble arg)
    {
        value = "first";
    }

    public MultipleCtorsWithArgs(final SimpleDouble first, final SimpleDouble second)
    {
        value = "second";
    }

    public String getValue()
    {
        return value;
    }
}