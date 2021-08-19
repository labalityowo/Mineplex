package mineplex.core.lifetimes;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PhasedLifetimeTest
{
	PhasedLifetime<Phase> _lifetime = new PhasedLifetime<>();
	List<String> _events = new ArrayList<>();
	@Test
	public void testTwoPhaseComponent()
	{
		Assert.assertFalse(_lifetime.isActive());
		_lifetime.register(new LoggingComponent(_events, "component"), Arrays.asList(Phase.A, Phase.B));
		_lifetime.start(Phase.A);
		Assert.assertTrue(_lifetime.isActive());
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A"), _events);
		_lifetime.setPhase(Phase.B);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B"), _events);
		_lifetime.setPhase(Phase.C);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B", "component setPhase C", "component deactivated"), _events);
		_lifetime.end();
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B", "component setPhase C", "component deactivated"), _events);
		Assert.assertFalse(_lifetime.isActive());
	}
	@Test
	public void testGlobalComponent()
	{
		_lifetime.register(new LoggingComponent(_events, "component"));
		_lifetime.start(Phase.A);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A"), _events);
		_lifetime.setPhase(Phase.B);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B"), _events);
		_lifetime.end();
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B", "component deactivated"), _events);
	}

	@Test
	public void testLateRegistration()
	{
		_lifetime.start(Phase.A);
		_lifetime.register(new LoggingComponent(_events, "component"), Arrays.asList(Phase.A, Phase.B));
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A"), _events);
		_lifetime.setPhase(Phase.B);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B"), _events);
		_lifetime.end();
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase A", "component setPhase B", "component deactivated"), _events);
	}
	@Test
	public void testSinglePhase()
	{
		_lifetime.register(new LoggingComponent(_events, "component"), Collections.singletonList(Phase.B));
		_lifetime.start(Phase.A);
		Assert.assertEquals(Collections.emptyList(), _events);
		_lifetime.setPhase(Phase.B);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase B"), _events);
		_lifetime.setPhase(Phase.C);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase B", "component setPhase C", "component deactivated"), _events);
		_lifetime.end();
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase B", "component setPhase C", "component deactivated"), _events);
	}
	@Test
	public void testComponentLifetimes()
	{
		_lifetime.register(new LoggingComponent(_events, "component"), Collections.singletonList(Phase.B)).register(new LoggingComponent(_events, "child"));
		_lifetime.start(Phase.A);
		Assert.assertEquals(Collections.emptyList(), _events);
		_lifetime.setPhase(Phase.B);
		Assert.assertEquals(Arrays.asList("component activated", "child activated", "component setPhase B"), _events);
		_lifetime.setPhase(Phase.C);
		Assert.assertEquals(Arrays.asList("component activated", "child activated","component setPhase B",  "component setPhase C", "child deactivated", "component deactivated"), _events);
		_lifetime.end();
		Assert.assertEquals(Arrays.asList("component activated", "child activated", "component setPhase B", "component setPhase C", "child deactivated", "component deactivated"), _events);
	}
	@Test
	public void testEarlyShutdown()
	{
		_lifetime.register(new LoggingComponent(_events, "component"), Arrays.asList(Phase.B, Phase.C));
		_lifetime.start(Phase.A);
		Assert.assertEquals(Collections.emptyList(), _events);
		_lifetime.setPhase(Phase.B);
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase B"), _events);
		_lifetime.end();
		Assert.assertEquals(Arrays.asList("component activated", "component setPhase B", "component deactivated"), _events);

	}
	enum Phase
	{
		A,
		B,
		C,
		;
	}
}
