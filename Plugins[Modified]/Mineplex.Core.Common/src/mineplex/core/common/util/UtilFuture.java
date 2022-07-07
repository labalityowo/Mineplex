package mineplex.core.common.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class UtilFuture
{
	/**
	 * Returns a {@link CompletableFuture} which will complete when supplied futures have completed.
	 * This is a workaround for {@link CompletableFuture#anyOf(CompletableFuture[])} returning void.
	 *
	 * @param futures the futures to wait to complete
	 * @param <T> the type of item(s)
	 * @return a future which will complete when all supplied futures have completed
	 */
	public static <T> CompletableFuture<List<T>> sequence(Collection<CompletableFuture<T>> futures)
	{
		return sequence(futures, Collectors.toList());
	}

	/**
	 * Returns a {@link CompletableFuture} which will complete when supplied futures have completed.
	 * This is a workaround for {@link CompletableFuture#anyOf(CompletableFuture[])} returning void.
	 *
	 * @param futures the futures to wait to complete
	 * @param <R> the collection type
	 * @param <T> the type of the collection's items
	 * @return a future which will complete when all supplied futures have completed
	 */
	public static <R, T> CompletableFuture<R> sequence(Collection<CompletableFuture<T>> futures, Collector<T, ?, R> collector)
	{
		CompletableFuture<Void> futuresCompletedFuture =
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));

		return futuresCompletedFuture.thenApply(v ->
				futures.stream().map(CompletableFuture::join).collect(collector));
	}

	/**
	 * Returns a {@link CompletableFuture} containing a {@link Multimap} with future values unwrapped.
	 *
	 * @param multimap the multimap to transform the values of
	 * @param <M> the multimap type, values must be a {@link Collection} of {@link CompletableFuture} object.
	 * @param <K> the multimap key type
	 * @param <V> the future return type
	 * @return a future which returns a multimap with unwrapped values
	 */
	public static <M extends Multimap<K, CompletableFuture<V>>, K, V> CompletableFuture<Multimap<K, V>> sequenceValues(M multimap)
	{
		CompletableFuture<Map<K, Collection<V>>> sequenced = sequenceValues(multimap.asMap());

		return sequenced.thenApply(map ->
		{
			Multimap<K, V> sequencedMultiMap = HashMultimap.create();
			map.forEach(sequencedMultiMap::putAll);
			return sequencedMultiMap;
		});
	}

	/**
	 * Returns a {@link CompletableFuture} containing a {@link Map} with future values unwrapped.
	 *
	 * @param map the map to transform the values of
	 * @param <M> the map type, values must be a {@link Collection} of {@link CompletableFuture} object.
	 * @param <K> the map key type
	 * @param <V> the future return type
	 * @return a future which returns a map with unwrapped values
	 */
	public static <M extends Map<K, Collection<CompletableFuture<V>>>, K, V> CompletableFuture<Map<K, Collection<V>>> sequenceValues(M map)
	{
		Map<K, CompletableFuture<List<V>>> seqValues =
				map.keySet().stream().collect(Collectors.toMap(Function.identity(), key -> sequence(map.get(key))));

		Collection<CompletableFuture<List<V>>> values = seqValues.values();

		CompletableFuture<Void> futuresCompleted =
				CompletableFuture.allOf(values.toArray(new CompletableFuture[values.size()]));

		return futuresCompleted.thenApply(v ->
				(Map<K, Collection<V>>) seqValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (Collection<V>) entry.getValue().join())));
	}

	/**
	 * Filters a list using functions which return a {@link CompletableFuture}.
	 * The returned future is marked as complete once all items have been filtered.
	 *
	 * @param list the list of elements to filter
	 * @param futureFunction a function which returns a future containing whether an element should be filtered
	 * @param <T> the type of element
	 * @return a collection containing the filtered elements
	 */
	public static <T> CompletableFuture<List<T>> filter(List<T> list, Function<T, CompletableFuture<Boolean>> futureFunction)
	{
		return filter(list, futureFunction, Collectors.toList());
	}

	/**
	 * Filters a list using functions which return a {@link CompletableFuture}.
	 * The returned future is marked as complete once all items have been filtered.
	 *
	 * @param list the list of elements to filter
	 * @param futureFunction a function which returns a future containing whether an element should be filtered
	 * @param collector the collector used to collect the results
	 * @param <T> the type of element
	 * @param <R> the returned collection type
	 * @return a collection containing the filtered elements
	 */
	public static <T, R> CompletableFuture<R> filter(List<T> list, Function<T, CompletableFuture<Boolean>> futureFunction, Collector<T, ?, R> collector)
	{
		Map<T, CompletableFuture<Boolean>> elementFutureMap = list.stream()
				.collect(Collectors.toMap(
						Function.identity(),
						futureFunction,
						(u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
						LinkedHashMap::new));
		Collection<CompletableFuture<Boolean>> futures = elementFutureMap.values();

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenApply(aVoid ->
				elementFutureMap.entrySet().stream()
						.filter(entry -> entry.getValue().join()) // this doesn't block as all futures have completed
						.map(Map.Entry::getKey)
						.collect(collector));
	}
}
