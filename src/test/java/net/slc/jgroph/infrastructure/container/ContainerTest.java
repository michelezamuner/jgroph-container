package net.slc.jgroph.infrastructure.container;

import net.slc.jgroph.infrastructure.container.stubs.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings("initialization")
public class ContainerTest
{
    private Container container;
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp()
    {
        container = new Container();
    }

    @Test
    public void instantiatesClassWithNoDependencies()
    {
        final Simple obj = container.make(Simple.class);
        assertNotNull(obj);
        assertSame("Simple", obj.getValue());
    }

    @Test
    public void instantiatesClassWithPublicDefaultConstructor()
    {
        final PublicDefaultCtor obj = container.make(PublicDefaultCtor.class);
        assertNotNull(obj);
    }

    @Test
    public void cannotInstantiateClassWithNonPublicDefaultConstructor()
    {
        class DefCtor {}
        exception.expect(ContainerError.class);
        exception.expectMessage("Cannot instantiate " + DefCtor.class + " with no public constructor.");
        container.make(DefCtor.class);
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndNoArgsUsingFirstConstructorFound()
    {
        final MultipleCtorsWithArgs obj = container.make(MultipleCtorsWithArgs.class);
        assertNotNull(obj);
        assertSame("first", obj.getValue());
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndArgs()
    {
        final MultipleCtors withDeps = container.make(MultipleCtors.class, new Simple());
        assertNotNull(withDeps);
        assertSame("Simple", withDeps.getValue());

        final MultipleCtors withScalars = container.make(MultipleCtors.class, "s", 1);
        assertNotNull(withScalars);
        assertSame("s", withScalars.getValue());
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndSubclassesArgs()
    {
        class Subclassed extends Simple {
            @Override
            public String getValue()
            {
                return "subclassed";
            }
        }

        final MultipleCtors obj = container.make(MultipleCtors.class, new Subclassed());
        assertNotNull(obj);
        assertSame("subclassed", obj.getValue());
    }

    @Test
    public void cannotInstantiateInnerClassesWithArgs()
    {
        class WithDep
        {
            public WithDep(final StubInterface i) {}
        }

        exception.expect(ContainerError.class);
        exception.expectMessage(String.format(
                "%s has no constructor with arguments: %s",
                WithDep.class,
                ContainerTest.class + "$1"
        ));

        container.make(WithDep.class, new StubInterface() {});
    }

    @Test
    public void cannotInstantiateClassWithMultipleConstructorsWithWrongArguments()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage(String.format(
                "%s has no constructor with arguments: %s",
                MultipleCtors.class,
                Integer.class + ", " + Integer.class + ", " + Integer.class
        ));
        container.make(MultipleCtors.class, 1, 2, 3);
    }

    @Test
    public void cannotInstantiateClassesWithPartialExplicitArgs()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage(String.format(
                "%s has no constructor with arguments: %s",
                SimpleDeps.class,
                Simple.class
        ));
        container.make(SimpleDeps.class, new Simple());
    }

    @Test
    public void instantiatesClassWithSimpleDependenciesWithExplicitArgs()
    {
        final Simple d1 = new Simple();
        final Simple d2 = new Simple();

        final SimpleDeps obj = container.make(SimpleDeps.class, d1, d2);

        assertSame(d1, obj.getD1());
        assertSame(d2, obj.getD2());
    }

    @Test
    public void instantiatesClassWithSimpleDependenciesWithImplicitArgs()
    {
        final SimpleDeps obj = container.make(SimpleDeps.class);
        assertEquals("Simple", obj.getD1().getValue());
        assertEquals("Simple", obj.getD2().getValue());
    }

    @Test
    public void instantiatesClassWithComplexDependenciesWithExplicitArgs()
    {
        final Simple d11 = new Simple();
        final Simple d12 = new Simple();
        final Simple d2 = new Simple();

        final ComplexDeps obj = container.make(ComplexDeps.class, new SimpleDeps(d11, d12), d2);

        assertSame(d11, obj.getD1().getD1());
        assertSame(d12, obj.getD1().getD2());
        assertSame(d2, obj.getD2());
    }

    @Test
    public void instantiatesClassWithComplexDependenciesWithImplicitArgs()
    {
        final ComplexDeps obj = container.make(ComplexDeps.class);
        assertEquals("Simple", obj.getD1().getD1().getValue());
        assertEquals("Simple", obj.getD1().getD2().getValue());
        assertEquals("Simple", obj.getD2().getValue());
    }

    @Test
    public void instantiatesClassWithBoundObject()
    {
        final Simple bound = new Simple();
        container.bind(Simple.class, bound);

        final Simple obj = container.make(Simple.class);
        assertSame(bound, obj);
    }

    @Test
    public void cannotInstantiateInterfaceIfNoObjectIsBound()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage("Cannot instantiate " + StubInterface.class + " with no object bound.");
        container.make(StubInterface.class);
    }

    @Test
    public void instantiatesInterfaceWithBoundObject()
    {
        final StubInterface bound = mock(StubInterface.class);
        container.bind(StubInterface.class, bound);

        final StubInterface obj = container.make(StubInterface.class);
        assertSame(bound, obj);
    }

    @Test
    public void instantiatingContainerWillAlwaysProduceTheSameContainerObject()
    {
        final Container obj = container.make(Container.class);
        assertSame(container, obj);
    }

    @Test
    public void instantiatesClassFromCallback()
    {
        container.bind(MutableClass.class, (Callback)(Object ...args) -> {
            final MutableClass obj = new MutableClass();
            obj.setValue((String)args[0]);
            return obj;
        });
        final MutableClass obj = container.make(MutableClass.class, "My Value");
        assertSame("My Value", obj.getValue());
    }
}