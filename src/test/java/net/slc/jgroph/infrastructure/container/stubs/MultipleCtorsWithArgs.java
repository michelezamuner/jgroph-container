package net.slc.jgroph.infrastructure.container.stubs;

public class MultipleCtorsWithArgs
{
    private String value;

    public MultipleCtorsWithArgs(final Simple arg)
    {
        value = "first";
    }

    public MultipleCtorsWithArgs(final Simple first, final Simple second)
    {
        value = "second";
    }

    public String getValue()
    {
        return value;
    }
}