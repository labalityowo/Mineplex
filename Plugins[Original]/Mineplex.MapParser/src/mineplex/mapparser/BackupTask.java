package mineplex.mapparser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.ZipUtil;

/**
 * Created by shaun on 14-09-23.
 */
public class BackupTask implements Runnable
{
	private final String _worldName;
	private final Callback<Boolean> _callback;
	private final JavaPlugin _plugin;

	public BackupTask(JavaPlugin plugin, String worldName, Callback<Boolean> callback)
	{
		_plugin = plugin;
		_worldName = worldName;
		_callback = callback;

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this);
	}

	@Override
	public void run()
	{
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		List<String> fileList = new ArrayList<String>();
		List<String> folderList = new ArrayList<String>();
		String dest = "backup" + _worldName.substring(3) + "/" + format.format(date) + ".zip";
		File srcFile = new File(_worldName);

		// Create backup folder if it doesnt already exist
		File folder = new File(dest.substring(0, dest.lastIndexOf('/')));
		if (!folder.exists())
			folder.mkdirs();

		// Get all files to backup
		for (File file : srcFile.listFiles())
		{
			if (file.isFile())
				fileList.add(_worldName + File.separator + file.getName());
			else if (file.isDirectory())
				folderList.add(_worldName + File.separator + file.getName());
		}

		// Delete old folders if more than 20 backups exist
		if (folder.listFiles().length > 20)
		{
			File[] files = folder.listFiles();
			File oldestFile = files[0];

			for (int i = 1; i < files.length; i++)
			{
				File file = files[i];
				if (file.getName().endsWith(".zip") && file.lastModified() < oldestFile.lastModified())
				{
					oldestFile = file;
				}
			}

			System.out.println("Deleting oldest file: " + oldestFile.getName());
			oldestFile.delete();
		}


		ZipUtil.ZipFolders(srcFile.getAbsolutePath(), dest, folderList, fileList);

		if (_callback != null)
		{
			_plugin.getServer().getScheduler().runTask(_plugin, new Runnable()
			{
				@Override
				public void run()
				{
					_callback.run(true);
				}
			});
		}
	}
}
