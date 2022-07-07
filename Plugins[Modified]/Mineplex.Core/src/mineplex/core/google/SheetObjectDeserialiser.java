package mineplex.core.google;

public interface SheetObjectDeserialiser<T>
{

	public T deserialise(String[] values) throws ArrayIndexOutOfBoundsException;
	
}
