package mineplex.serverdata.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * DataRepository is used to store {@link Data} objects in a central database
 * for real-time fetching/modification.
 * @author Ty
 *
 * @param <T> - the type of {@link Data} object stored in this repository.
 */
public interface DataRepository<T extends Data> 
{
	
	public Collection<T> getElements();

	public T getElement(String dataId);
	
	public Collection<T> getElements(Collection<String> dataIds);

	public Map<String,T> getElementsMap(List<String> dataIds);

	public void addElement(T element, int timeout);
	
	public void addElement(T element);

	public void removeElement(T element);
	
	public void removeElement(String dataId);

	public boolean elementExists(String dataId);

	public int clean();
}
