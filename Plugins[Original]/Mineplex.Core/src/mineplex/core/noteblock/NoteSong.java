package mineplex.core.noteblock;

import java.util.Map;

public class NoteSong
{

	// Song Data
	private final short _length, _height;
	private final String _name;
	private final short _tempo;
	private final byte _timeSignature;

	// Layer Data
	private final Map<Integer, NoteLayer> _layerMap;

	public NoteSong(short length, short height, String name, short tempo, byte timeSignature, Map<Integer, NoteLayer> layerMap)
	{
		_length = length;
		_height = height;
		_name = name;
		_tempo = tempo;
		_timeSignature = timeSignature;
		_layerMap = layerMap;
	}

	public short getLength()
	{
		return _length;
	}

	public short getHeight()
	{
		return _height;
	}

	public String getName()
	{
		return _name;
	}

	public short getTempo()
	{
		return _tempo;
	}

	public byte getTimeSignature()
	{
		return _timeSignature;
	}

	public Map<Integer, NoteLayer> getLayerMap()
	{
		return _layerMap;
	}
}
