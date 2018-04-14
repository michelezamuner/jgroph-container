package net.slc.jgroph.infrastructure.container;

class MultipleConstructorsDouble
{
    private final String value;

    public MultipleConstructorsDouble()
    {
        value = "";
    }

    public MultipleConstructorsDouble(final SimpleDouble simple)
    {
        value = simple.getValue();
    }

    public MultipleConstructorsDouble(final String s, final int i)
    {
        value = s;
    }

    public MultipleConstructorsDouble(final String s, final Float i)
    {
        value = "";
    }

    public String getValue()
    {
        return value;
    }
}