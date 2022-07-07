package mineplex.core.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class UtilCollections
{
	public static Random Random = new Random();
	
	@SafeVarargs
	public static <E> NautArrayList<E> newNautList(E... elements)
	{
		return new NautArrayList<E>(elements);
	}
	
	public static <E> NautArrayList<E> newNautList()
	{
		return new NautArrayList<E>();
	}
	
	public static <K, V> NautHashMap<K, V> newNautMap(K[] keys, V[] values)
	{
		return new NautHashMap<K, V>(keys, values);
	}
	
	public static <K, V> NautHashMap<K, V> newNautMap()
	{
		return new NautHashMap<K, V>();
	}
	
	public static <T> T getLast(List<T> list)
	{
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}
	
	public static <T> T getFirst(List<T> list)
	{
		return list.isEmpty() ? null : list.get(0);
	}
	
	public static <T> T getLast(NautArrayList<T> list)
	{
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}
	
	public static <T> T getFirst(NautArrayList<T> list)
	{
		return list.isEmpty() ? null : list.get(0);
	}

	public static <E> void forEach(E[] elements, Function<E, E> filter, Consumer<E> consumer)
	{
		for (int i = 0; i < elements.length; i++)
		{
			consumer.accept(filter.apply(elements[i]));
		}
	}
	
	public static <E> void forEach(E[] elements, Consumer<E> consumer)
	{
		for (int i = 0; i < elements.length; i++)
		{
			consumer.accept(elements[i]);
		}
	}
	
	public static <E> void forEach(E[] elements, BiConsumer<Integer, E> consumer)
	{
		for (int i = 0; i < elements.length; i++)
		{
			consumer.accept(i, elements[i]);
		}
	}

	public static <E> void addAll(E[] elements, Collection<E> collection)
	{
		forEach(elements, collection::add);
	}
	
	public static void loop(int min, int max, Consumer<Integer> consumer)
	{
		for (int i = min; i < max; i++)
		{
			consumer.accept(i);
		}
	}
	
	public static byte[] ensureSize(byte[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}
	
	public static boolean[] ensureSize(boolean[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}
	
	public static int[] ensureSize(int[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}
	
	public static long[] ensureSize(long[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}

	public static short[] ensureSize(short[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}

	public static char[] ensureSize(char[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}
	
	public static float[] ensureSize(float[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}

	public static double[] ensureSize(double[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}

	public static <T> T[] ensureSize(T[] array, int size)
	{
		if (array.length <= size)
		{
			return array;
		}
		
		return Arrays.copyOf(array, size);
	}
	
	public static byte random(byte[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static short random(short[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static char random(char[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static boolean random(boolean[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static int random(int[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static long random(long[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static double random(double[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static float random(float[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static <T> T random(T[] array)
	{
		return array[Random.nextInt(array.length)];
	}
	
	public static <T> List<? extends T> toList(T[] array)
	{
		return Lists.newArrayList(array);
	}

	public static <T1, T2> boolean equal(T1[] array1, T2[] array2)
	{
		return Arrays.equals(array1, array2);
	}

	public static <X> void ForEach(List<X> list, Consumer<X> consumer)
	{
		Iterator<X> iterator = list.iterator();
		
		while (iterator.hasNext())
		{
			consumer.accept(iterator.next());
		}
	}

	@SafeVarargs
	public static <X> List<? extends X> newList(X... elements)
	{
		return toList(elements);
	}

	public static <X> String combine(X[] data, String delimiter)
	{
		StringBuilder total = new StringBuilder();
		
		int loops = 0;
		
		for (X x : data)
		{
			if (delimiter != null && loops != 0)
			{
				total.append(delimiter);
			}
			
			total.append(x.toString());
			
			loops++;
		}
		
		return total.toString();
	}

	public static <T> List<T> unboxPresent(Collection<Optional<T>> optionalList)
	{
		return optionalList.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}
}