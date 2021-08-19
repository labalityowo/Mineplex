package mineplex.core.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UtilFile
{
	public static String read(File file)
	{
		try
		{
			return new String(readBytes(file));
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(File file) throws IOException
	{
		FileInputStream stream = new FileInputStream(file);
		
		byte[] bytes = new byte[stream.available() /* estimated bytes available */];
		
		int pointer = 0;
		while (true)
		{
			int read = stream.read();
			
			if (read == -1)
			{
				break;
			}
			
			bytes = UtilCollections.ensureSize(bytes, bytes.length + 1);
			
			bytes[pointer] = (byte) read;
			
			++pointer;
		}
		
		stream.close();
		
		return bytes;
	}
}
