package mineplex.core.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
	private static void getFileList(List<String> fileList, String sourceFolder, File node)
	{
		// add file only
		if (node.isFile())
		{
			fileList.add(generateZipEntry(sourceFolder, node.getAbsoluteFile().toString()));
		}

		if (node.isDirectory())
		{
			String[] subNote = node.list();
			for (String filename : subNote)
			{
				getFileList(fileList, sourceFolder, new File(node, filename));
			}
		}
	}

	private static String generateZipEntry(String sourceFolder, String file)
	{
		System.out.println(sourceFolder + " " + file);
		return file.substring(sourceFolder.length() + 1, file.length());
	}

	public static void ZipFolders(String sourceFolder, String zipFilename, List<String> folders, List<String> files)
	{
		ZipOutputStream zipOutputStream = null;
		FileOutputStream fileOutputStream = null;
		FileInputStream fileInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		
		List<String> fileList = new ArrayList<String>();
		byte[] buffer = new byte[2048];

		try
		{			
			fileOutputStream = new FileOutputStream(zipFilename);

			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			zipOutputStream = new ZipOutputStream(bufferedOutputStream);
			ZipEntry entry;

			for (String file : files)
			{
				fileList.add(generateZipEntry(sourceFolder, new File(file).getAbsoluteFile().toString()));
			}
			
			for (String folder : folders)
			{
				getFileList(fileList, sourceFolder, new File(folder));
			}
			
			for (String file : fileList)
			{
				entry = new ZipEntry(file);
				zipOutputStream.putNextEntry(entry);

				fileInputStream = new FileInputStream(sourceFolder + File.separator + file);

				int len;
				while ((len = fileInputStream.read(buffer)) > 0)
				{
					zipOutputStream.write(buffer, 0, len);
				}

				fileInputStream.close();
			}

			zipOutputStream.flush();
			zipOutputStream.close();

			bufferedOutputStream.flush();
			bufferedOutputStream.close();
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

			if (zipOutputStream != null)
			{
				try
				{
					zipOutputStream.close();
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

	public static void UnzipToDirectory(String zipFilePath, String outputDirectory)
	{
		FileInputStream fileInputStream = null;
		ZipInputStream zipInputStream = null;
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		BufferedInputStream bufferedInputStream = null;

		try
		{
			fileInputStream = new FileInputStream(zipFilePath);
			bufferedInputStream = new BufferedInputStream(fileInputStream);
			zipInputStream = new ZipInputStream(bufferedInputStream);
			ZipEntry entry;

			while ((entry = zipInputStream.getNextEntry()) != null)
			{
				int size;
				byte[] buffer = new byte[2048];

				String fileName = outputDirectory + File.separator + entry.getName();

				if (fileName.endsWith("/"))
				{
					new File(fileName).mkdirs();
					continue;
				}

				fileOutputStream = new FileOutputStream(fileName);
				bufferedOutputStream = new BufferedOutputStream(fileOutputStream, buffer.length);

				while ((size = zipInputStream.read(buffer, 0, buffer.length)) != -1)
				{
					bufferedOutputStream.write(buffer, 0, size);
				}

				bufferedOutputStream.flush();
				bufferedOutputStream.close();
				fileOutputStream.flush();
				fileOutputStream.close();
			}

			zipInputStream.close();
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

			if (zipInputStream != null)
			{
				try
				{
					zipInputStream.close();
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
