package mineplex.core.noteblock;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about the NBS Format was taken from
 * http://www.stuffbydavid.com/mcnbs/format
 */
public class NBSReader
{

	public static NoteSong loadSong(String fileName) throws FileNotFoundException
	{
		return loadSong(new DataInputStream(new FileInputStream(new File(fileName))), fileName);
	}

	public static NoteSong loadSong(DataInputStream stream, String defaultName)
	{
		try
		{
			// Header Information
			short length = readShort(stream);
			short height = readShort(stream);
			String name = readString(stream);
			String author = readString(stream);
			String originalAuthor = readString(stream);
			String description = readString(stream);
			short tempo = readShort(stream); // Tempo multiplied by 1000
			boolean autosave = stream.readBoolean();
			byte autosaveDuration = stream.readByte();
			byte timeSignature = stream.readByte();
			int minutesSpent = readInt(stream);
			int leftClicks = readInt(stream);
			int rightClicks = readInt(stream);
			int blocksAdded = readInt(stream);
			int blocksRemoved = readInt(stream);
			String midiFileName = readString(stream);

			if (name.length() == 0 && defaultName != null)
			{
				name = defaultName;
			}

			Map<Integer, NoteLayer> layerMap = new HashMap<>();

			// Note Block Information
			int tick = -1;
			int jumps;

			while (true)
			{
				jumps = readShort(stream);

				if (jumps == 0)
				{
					break;
				}

				tick += jumps;
				int layer = -1;

				while (true)
				{
					jumps = readShort(stream);

					if (jumps == 0)
					{
						break;
					}

					layer += jumps;
					byte instrument = stream.readByte();
					byte key = stream.readByte();

					Note note = new Note(instrument, key);
					NoteLayer noteLayer = layerMap.get(layer);

					if (noteLayer == null)
					{
						noteLayer = new NoteLayer();
						layerMap.put(layer, noteLayer);
					}

					noteLayer.setNote(tick, note);
				}
			}

			// Layer Information
			for (int i = 0; i < height; i++)
			{
				NoteLayer layer = layerMap.get(i);
				if (layer != null)
				{
					layer.setName(readString(stream));
					layer.setVolume(stream.readByte());
				}
			}

			System.out.println("[NBSReader] Successfully loaded song " + name + " with tempo " + tempo);
			return new NoteSong(length, height, name, tempo, timeSignature, layerMap);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private static int readInt(DataInputStream stream) throws IOException
	{
		// For some reason the bytes are in reverse order from stream.readInt()
		int ch1 = stream.read();
		int ch2 = stream.read();
		int ch3 = stream.read();
		int ch4 = stream.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
	}

	private static short readShort(DataInputStream stream) throws IOException
	{
		// For some reason the bytes are in reverse order from stream.readShort()
		int ch1 = stream.read();
		int ch2 = stream.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short)((ch2 << 8) + ch1);
	}

	private static String readString(DataInputStream stream) throws IOException
	{
		int length = readInt(stream);
		char[] string = new char[length];
		for (int i = 0; i < length; i++)
		{
			string[i] = (char) stream.readByte();
		}
		return new String(string);
	}
}
