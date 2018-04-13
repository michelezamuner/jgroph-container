package net.slc.jgroph.infrastructure.container;

@SuppressWarnings("initialization")
public class ClassWithValue
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