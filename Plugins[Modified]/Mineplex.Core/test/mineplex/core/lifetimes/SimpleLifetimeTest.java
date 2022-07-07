package mineplex.core.lifetimes;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleLifetimeTest
{
	private final SimpleLifetime _lifetime = new SimpleLifetime();
	private final List<String> _events = new ArrayList<>();
	@Test
	public void testAddition()
	{
		_lifetime.register(new LoggingComponent(_events,"a"));
		_lifetime.start();
		_lifetime.end();
		Assert.assertEquals(_events, Arrays.asList("a activated", "a deactivated"));
	}
	@Test
	public void testLateAddition()
	{
		_lifetime.start();
		_lifetime.register(new LoggingComponent(_events,"a"));
		_lifetime.end();
		Assert.assertEquals(_events, Arrays.asList("a activated", "a deactivated"));
	}
	@Test
	public void testActivationOrder()
	{
		_lifetime.register(new LoggingComponent(_events,"a"));
		_lifetime.register(new LoggingComponent(_events,"b"));
		_lifetime.start();
		_lifetime.end();
		Assert.assertEquals(_events, Arrays.asList("a activated", "b activated", "b deactivated", "a deactivated"));
	}


}
