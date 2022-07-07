package com.mineplex.clansqueue.common;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class EnclosedInteger
{
	private int _value;
	
	public EnclosedInteger(int value)
	{
		_value = value;
	}
	
	public EnclosedInteger()
	{
		this(0);
	}
	
	public int get()
	{
		return _value;
	}
	
	public int getAndIncrement()
	{
		return _value++;
	}
	
	public int incrementAndGet()
	{
		return ++_value;
	}
	
	public void set(int newValue)
	{
		_value = newValue;
	}
}