package net.slc.jgroph.infrastructure.container.stubs;

public class ComplexDeps
{
    private final SimpleDeps d1;
    private final Simple d2;

    public ComplexDeps(final SimpleDeps d1, final Simple d2)
    {
        this.d1 = d1;
        this.d2 = d2;
    }

    public SimpleDeps getD1()
    {
        return d1;
    }

    public Simple getD2()
    {
        return d2;
    }
}