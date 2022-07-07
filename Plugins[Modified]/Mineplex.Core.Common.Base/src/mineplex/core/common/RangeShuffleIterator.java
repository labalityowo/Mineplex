package mineplex.core.common;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

/**
 * An Iterator that shuffles and provides integers from a specified range.
 * The shuffle strategy is based on the Fisher-Yates shuffle.
 *
 * @see #nextInts(int)
 * @see #nextInt()
 */
public class RangeShuffleIterator implements PrimitiveIterator.OfInt
{
    private static final Random random = new Random();
    private final IntSet _remaining;
    private int _remainingCount;

    /**
     * Create a RangeShuffleIterator encompassing the specified range (inclusive)
     *
     * @param start The range lower bound, inclusive
     * @param end The range upper bound, inclusive
     */
    public RangeShuffleIterator(int start, int end)
    {
        Preconditions.checkArgument(start <= end);
        _remaining = new IntSet(start, end);
        _remainingCount = end - start + 1;
    }

    /**
     * Provide a specified number of integers in an int array. If the number
     * of elements remaining is fewer than {@code maxAmount}, return all
     * remaining elements.
     *
     * @param maxAmount The number of elements to retrieve
     * @return An array containing the retrieved elements
     */
    public int[] nextInts(int maxAmount)
    {
        int[] ret = new int[Math.min(_remainingCount, maxAmount)];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = nextInt();
        }
        return ret;
    }

    @Override
    public int nextInt()
    {
        if (!hasNext())
        {
            throw new IllegalStateException("No remaining ranges to iterate");
        }

        int selectedPosition = random.nextInt(_remainingCount);

        Iterator<Map.Entry<Integer, Integer>> it = _remaining.ranges().iterator();

        final int selected;
        while (true)
        {
            Map.Entry<Integer, Integer> range = it.next();
            int span = range.getValue() - range.getKey();
            if (span < selectedPosition)
            {
                selectedPosition -= span + 1;

            }
            else
            {
                selected = range.getKey() + selectedPosition;
                break;
            }
        }

        _remaining.remove(selected);
        --_remainingCount;

        return selected;
    }

    @Override
    public boolean hasNext()
    {
        return _remainingCount > 0;
    }

    /**
     * A set of integers. The set is seeded by a single range, and the only
     * supported operation is int removal.
     * <p>
     * This implementation only exists for performance reasons.
     */
    private static class IntSet
    {
        /**
         * A set of ranges representing the remaining integers in this set
         */
        private final NavigableMap<Integer, Integer> ranges = new TreeMap<>();

        /**
         * Create an IntSet containing all numbers from {@code start} to
         * {@code end}, inclusive
         *
         * @param start The range lower bound, inclusive
         * @param end The range upper bound, inclusive
         */
        private IntSet(int start, int end)
        {
            ranges.put(start, end);
        }

        public Set<Map.Entry<Integer, Integer>> ranges()
        {
            return ranges.entrySet();
        }

        /**
         * Remove an integer from this IntSet
         * @param value The integer to remove
         */
        public void remove(int value)
        {
            Map.Entry<Integer,Integer> range = ranges.floorEntry(value);
            if (range == null || range.getValue() < value)
            {
                return;
            }

            int lower = range.getKey();
            int upper = range.getValue();

            if (upper > value)
            {
                reinsert(value + 1, upper);
            }
            reinsert(lower, Math.min(upper, value - 1));
        }

        private void reinsert(int start, int end)
        {
            if (end < start)
            {
                ranges.remove(start);
            }
            else
            {
                ranges.put(start, end);
            }
        }
    }
}