package mineplex.core.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil
{
	public static void DeleteFolder(File folder) 
	{
		if (!folder.exists())
		{
			System.out.println("Delete target does not exist: " + folder);
			return;
		}
			
		
	    File[] files = folder.listFiles();
	    
	    if(files != null)
	    {
	        for(File f: files)
	        {
	            if(f.isDirectory())
	            	DeleteFolder(f);
	            else 
	            {
	            	f.delete();
	            }
	        }
	    }
	    
	   folder.delete();
	}
	
	public static void CopyToDirectory(File file, String outputDirectory)
	{
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		BufferedInputStream bufferedInputStream = null;
		
		try
		{
			fileInputStream = new FileInputStream(file);
			bufferedInputStream = new BufferedInputStream(fileInputStream);
			
			int size;
			byte[] buffer = new byte[2048];

			fileOutputStream = new FileOutputStream(outputDirectory + "\\" + file.getName());
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream, buffer.length);

			while ((size = bufferedInputStream.read(buffer, 0, buffer.length)) != -1)
			{
				bufferedOutputStream.write(buffer, 0, size);
			}
			
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			fileOutputStream.flush();
			fileOutputStream.close();
	
			bufferedInputStream.close();
			fileInputStream.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			
			if (fileInputStream != null)
			{
				try 
				{
					fileInputStream.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
			
			if (bufferedInputStream != null)
			{
				try 
				{
					bufferedInputStream.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
			
			if (fileOutputStream != null)
			{
				try 
				{
					fileOutputStream.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
			
			if (bufferedOutputStream != null)
			{
				try 
				{
					bufferedOutputStream.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
	}
}
