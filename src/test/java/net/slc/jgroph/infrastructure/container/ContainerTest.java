package net.slc.jgroph.infrastructure.container;

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
        final SimpleDouble object = container.make(SimpleDouble.class);
        assertNotNull(object);
    }

    @Test
    public void instantiatesClassWithPublicDefaultConstructor()
    {
        final DefaultConstructorPublicDouble object = container.make(DefaultConstructorPublicDouble.class);
        assertNotNull(object);
    }

    @Test
    public void cannotInstantiateClassWithNonPublicDefaultConstructor()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage(
                "Cannot instantiate " + DefaultConstructorDouble.class + " with no public constructor."
        );
        container.make(DefaultConstructorDouble.class);
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndNoArgsUsingNoArgsConstructor()
    {
        final MultipleConstructorsDouble object = container.make(MultipleConstructorsDouble.class);
        assertNotNull(object);
        assertSame("", object.getValue());
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndNoArgsUsingFirstConstructorWithArgsFound()
    {
        final MultipleCtorsWithArgs obj = container.make(MultipleCtorsWithArgs.class);
        assertNotNull(obj);
        assertSame("first", obj.getValue());
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndArgs()
    {
        final MultipleConstructorsDouble withDeps =
                container.make(MultipleConstructorsDouble.class, new SimpleDouble());
        assertNotNull(withDeps);
        assertSame("SimpleDouble", withDeps.getValue());

        final MultipleConstructorsDouble withScalars = container.make(MultipleConstructorsDouble.class, "s", 1);
        assertNotNull(withScalars);
        assertSame("s", withScalars.getValue());
    }

    @Test
    public void instantiatesClassWithMultipleConstructorsAndSubclassesArgs()
    {
        class Subclassed extends SimpleDouble {
            @Override
            public String getValue()
            {
                return "subclassed";
            }
        }
        final MultipleConstructorsDouble object =
                container.make(MultipleConstructorsDouble.class, new Subclassed());
        assertNotNull(object);
        assertSame("subclassed", object.getValue());
    }

    @Test
    public void cannotInstantiateClassWithMultipleConstructorsWithWrongArguments()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage(String.format(
                "%s has no constructor with arguments: %s",
                MultipleConstructorsDouble.class,
                Integer.class + ", " + Integer.class + ", " + Integer.class
        ));
        container.make(MultipleConstructorsDouble.class, 1, 2, 3);
    }

    @Test
    public void cannotInstantiateClassesWithPartialExplicitArgs()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage(String.format(
                "%s has no constructor with arguments: %s",
                SimpleDependenciesDouble.class,
                SimpleDouble.class
        ));
        container.make(SimpleDependenciesDouble.class, new SimpleDouble());
    }

    @Test
    public void instantiatesClassWithSimpleDependenciesWithExplicitArgs()
    {
        final SimpleDouble d1 = new SimpleDouble();
        final SimpleDouble d2 = new SimpleDouble();

        final SimpleDependenciesDouble object = container.make(SimpleDependenciesDouble.class, d1, d2);

        assertSame(d1, object.getD1());
        assertSame(d2, object.getD2());
    }

    @Test
    public void instantiatesClassWithSimpleDependenciesWithImplicitArgs()
    {
        final SimpleDependenciesDouble object = container.make(SimpleDependenciesDouble.class);
        assertEquals("SimpleDouble", object.getD1().getValue());
        assertEquals("SimpleDouble", object.getD2().getValue());
    }

    @Test
    public void instantiatesClassWithComplexDependenciesWithExplicitArgs()
    {
        final SimpleDouble d11 = new SimpleDouble();
        final SimpleDouble d12 = new SimpleDouble();
        final SimpleDouble d2 = new SimpleDouble();

        final ComplexDependenciesDouble object = container.make(
                ComplexDependenciesDouble.class,
                new SimpleDependenciesDouble(d11, d12),
                d2
        );

        assertSame(d11, object.getD1().getD1());
        assertSame(d12, object.getD1().getD2());
        assertSame(d2, object.getD2());
    }

    @Test
    public void instantiatesClassWithComplexDependenciesWithImplicitArgs()
    {
        final ComplexDependenciesDouble object = container.make(ComplexDependenciesDouble.class);
        assertEquals("SimpleDouble", object.getD1().getD1().getValue());
        assertEquals("SimpleDouble", object.getD1().getD2().getValue());
        assertEquals("SimpleDouble", object.getD2().getValue());
    }

    @Test
    public void instantiatesClassWithBoundObject()
    {
        final SimpleDouble bound = new SimpleDouble();
        container.bind(SimpleDouble.class, bound);

        final SimpleDouble object = container.make(SimpleDouble.class);
        assertSame(bound, object);
    }

    @Test
    public void cannotInstantiateInterfaceIfNoObjectIsBound()
    {
        exception.expect(ContainerError.class);
        exception.expectMessage("Cannot instantiate " + InterfaceDouble.class + " with no object bound.");
        container.make(InterfaceDouble.class);
    }

    @Test
    public void instantiatesInterfaceWithBoundObject()
    {
        final InterfaceDouble bound = mock(InterfaceDouble.class);
        container.bind(InterfaceDouble.class, bound);

        final InterfaceDouble object = container.make(InterfaceDouble.class);
        assertSame(bound, object);
    }

    @Test
    public void instantiatingContainerWillAlwaysProduceTheSameObject()
    {
        final Container object = container.make(Container.class);
        assertSame(container, object);
    }

    @Test
    public void instantiatesClassFromCallback()
    {
        container.bind(ClassWithValue.class, (Callback)(Object ...args) -> {
            final ClassWithValue object = new ClassWithValue();
            object.setValue((String)args[0]);
            return object;
        });
        final ClassWithValue object = container.make(ClassWithValue.class, "My Value");
        assertSame("My Value", object.getValue());
    }
}