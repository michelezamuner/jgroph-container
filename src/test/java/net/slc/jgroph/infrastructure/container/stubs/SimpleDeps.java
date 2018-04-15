package net.slc.jgroph.infrastructure.container.stubs;

public class SimpleDeps
{
    private final Simple d1;
    private final Simple d2;

    public SimpleDeps(Simple d1, Simple d2)
    {
        this.d1 = d1;
        this.d2 = d2;
    }

    public Simple getD1()
    {
        return d1;
    }

    public Simple getD2()
    {
        return d2;
    }
}