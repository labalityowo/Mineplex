package mineplex.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simply represents that this {@link MiniPlugin} can be reflectively instantiated with no harm
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ReflectivelyCreateMiniPlugin
{
}
