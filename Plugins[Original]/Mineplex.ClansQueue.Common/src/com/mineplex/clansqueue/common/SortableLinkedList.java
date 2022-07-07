package com.mineplex.clansqueue.common;

import java.util.Comparator;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class SortableLinkedList<T extends Comparable<T>> extends LinkedList<T>
{
	public void sort()
	{
		sort(Comparator.naturalOrder());
	}
}