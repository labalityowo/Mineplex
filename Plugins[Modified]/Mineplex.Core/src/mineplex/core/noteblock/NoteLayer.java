package mineplex.core.noteblock;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a layer of notes in Note Block Studio
 */
public class NoteLayer
{

	private final Map<Integer, Note> _noteMap; // Notes indexed by ticks
	private int _volume; // Volume as a percentage 1-100
	private String _name;

	NoteLayer()
	{
		_noteMap = new HashMap<>();
		_volume = 100;
		_name = "";
	}

	public int getVolume()
	{
		return _volume;
	}

	public void setVolume(int volume)
	{
		_volume = volume;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setNote(int ticks, Note note)
	{
		_noteMap.put(ticks, note);
	}

	public Note getNote(int ticks)
	{
		return _noteMap.get(ticks);
	}

}
