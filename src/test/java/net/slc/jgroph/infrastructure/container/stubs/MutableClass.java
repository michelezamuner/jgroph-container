package net.slc.jgroph.infrastructure.container.stubs;

@SuppressWarnings("initialization")
public class MutableClass
{
    private String value;

    public void setValue(final String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}