package mineplex.core.common.util;

import java.util.function.Predicate;

public class UtilLambda
{
	/**
	 * This will return a {@link Predicate} which will return true <b>if and only if</b> all of the supplied Predicates
	 * return true
	 * @param predicates The Predicates to test against
	 * @return The resulting criterion
	 */
	@SafeVarargs
	public static <T> Predicate<T> and(Predicate<T>... predicates)
	{
		return t ->
		{
			for (Predicate<T> predicate : predicates)
			{
				if (!predicate.test(t))
				{
					return false;
				}
			}
			return true;
		};
	}

	/**
	 * This will return a {@link Predicate} which will return true <b>if and only if</b> one of the the supplied Predicates
	 * return true
	 * @param predicates The Predicates to test against
	 * @return The resulting criterion
	 */
	@SafeVarargs
	public static <T> Predicate<T> or(Predicate<T>... predicates)
	{
		return t ->
		{
			for (Predicate<T> predicate : predicates)
			{
				if (predicate.test(t))
				{
					return true;
				}
			}
			return false;
		};
	}

	public static <T> Predicate<T> not(Predicate<T> predicate)
	{
		return t -> !predicate.test(t);
	}
}
