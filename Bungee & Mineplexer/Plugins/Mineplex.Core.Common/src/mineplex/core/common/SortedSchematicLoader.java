package mineplex.core.common;

import mineplex.core.common.block.schematic.Schematic;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads schematics based on an ordered input value. A good example of usage would be to load schematics in the
 * ordering of a progress bar.
 *
 * @author Shaun Bennett
 */
public class SortedSchematicLoader<T>
{
	private final TreeMap<T, Schematic> _schematicMap;
	private final Location _pasteLocation;
	private T _currentValue = null;

	public SortedSchematicLoader(Location pasteLocation)
	{
		this(pasteLocation, null);
	}

	public SortedSchematicLoader(Location pasteLocation, Comparator<T> comparator)
	{
		_schematicMap = new TreeMap<>(comparator);
		_pasteLocation = pasteLocation;
	}

	public void addSchematic(T minValue, Schematic schematic)
	{
		_schematicMap.put(minValue, schematic);
	}

	public void update(T value)
	{
		Map.Entry<T, Schematic> entry = _schematicMap.floorEntry(value);

		if (entry != null && !entry.getKey().equals(_currentValue))
		{
			_currentValue = entry.getKey();
			Schematic schematic = entry.getValue();
			entry.getValue().paste(_pasteLocation, false);
		}
	}
}
