package mineplex.core.noteblock;

/**
 * Represents a single note to be played
 */
public class Note
{

	private final byte _instrument,  _note;

	public Note(byte instrument, byte note)
	{
		_instrument = instrument;
		_note = note;
	}

	public byte getInstrument()
	{
		return _instrument;
	}

	public byte getNote()
	{
		return _note;
	}
}
