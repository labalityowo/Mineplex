package mineplex.core.noteblock;

import org.bukkit.Sound;

/**
 * See http://minecraft.gamepedia.com/Note_Block for information about pitches
 */
public class UtilNote
{

	private static final double[] PITCH = { 0.5, 0.53, 0.56, 0.6, 0.63, 0.67, 0.7, 0.76, 0.8, 0.84, 0.9, 0.94, 1.0,
		1.06, 1.12, 1.18, 1.26, 1.34, 1.42, 1.5, 1.6, 1.68, 1.78, 1.88, 2.0 };

	public static Sound getInstrumentSound(byte instrument)
	{
		switch (instrument)
		{
			case 0:
				return Sound.NOTE_PIANO;
			case 1:
				return Sound.NOTE_BASS_GUITAR;
			case 2:
				return Sound.NOTE_BASS_DRUM;
			case 3:
				return Sound.NOTE_SNARE_DRUM;
			case 4:
				return Sound.NOTE_STICKS;
			default:
				return Sound.NOTE_PIANO;
		}
	}

	public static double getPitch(int note)
	{
		return note >= 0 && note < PITCH.length ? PITCH[note] : 0;
	}

}
