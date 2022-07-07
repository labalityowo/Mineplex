package mineplex.core.common.weight;

public class Weight<T>
{

	private final int _weight;
	public int getWeight() { return _weight; }
	
	private final T _value;
	public T getValue() { return _value; }
	
	public Weight(int weight, T value)
	{
		_weight = weight;
		_value = value;
	}
	
	
}
