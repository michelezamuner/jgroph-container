package net.slc.jgroph.infrastructure.container.stubs;

public class MultipleCtors
{
    private final String value;

    public MultipleCtors()
    {
        value = "";
    }

    public MultipleCtors(final Simple simple)
    {
        value = simple.getValue();
    }

    public MultipleCtors(final String s, final int i)
    {
        value = s;
    }

    public MultipleCtors(final StubInterface i)
    {
        value = "";
    }

    public String getValue()
    {
        return value;
    }
}