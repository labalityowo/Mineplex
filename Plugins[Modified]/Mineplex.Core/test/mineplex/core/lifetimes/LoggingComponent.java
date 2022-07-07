package mineplex.core.lifetimes;

import java.util.List;

public class LoggingComponent implements PhasedComponent {
	private final List<String> _events;
	private final String _name;

	public LoggingComponent(List<String> events, String name)
	{
		_events = events;
		this._name = name;
	}

	@Override
	public void activate()
	{
		_events.add(this._name + " activated");
	}

	@Override
	public void deactivate()
	{
		_events.add(this._name + " deactivated");
	}

	@Override
	public void setPhase(Object phase)
	{
		_events.add(this._name + " setPhase " + phase);
	}
}