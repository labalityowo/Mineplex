package mineplex.bungee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

import org.apache.commons.codec.digest.DigestUtils;

public class FileUpdater implements Runnable
{
	private Plugin _plugin;
	private HashMap<String, String> _jarMd5Map = new HashMap<String, String>();
		
	private boolean _needUpdate;
	private boolean _enabled = true;
	private int _timeTilRestart = 5;
	
	public FileUpdater(Plugin plugin)
	{
		_plugin = plugin;
		
		getPluginMd5s();
		
		if (new File("IgnoreUpdates.dat").exists())
			_enabled = false;
		
		_plugin.getProxy().getScheduler().schedule(_plugin, this, 2L, 2L, TimeUnit.MINUTES);
	}
	
	public void checkForNewFiles()
	{
		if (_needUpdate || !_enabled)
			return;
		
		boolean windows = System.getProperty("os.name").startsWith("Windows");
		
		File updateDir = new File((windows ? "C:" : File.separator + "home" + File.separator + "mineplex") + File.separator + "update");
		
		updateDir.mkdirs();
	    
	    FilenameFilter statsFilter = new FilenameFilter() 
	    {
            public boolean accept(File paramFile, String paramString) 
            {
                if (paramString.endsWith("jar"))
                {
                    return true;
                }
                
                return false;
            }
	    };
	    
	    for (File f : updateDir.listFiles(statsFilter))
	    {
	    	FileInputStream fis = null;
	    	
	    	try
	    	{
	    		if (_jarMd5Map.containsKey(f.getName()))
	    		{
	    			fis = new FileInputStream(f);
	    			String md5 = DigestUtils.md5Hex(fis);
	    			
	    			if (!md5.equals(_jarMd5Map.get(f.getName())))
	    			{
	    				System.out.println(f.getName() + " old jar : " + _jarMd5Map.get(f.getName()));
	    				System.out.println(f.getName() + " new jar : " + md5);
	    				_needUpdate = true;
	    			}
	    		}
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}
	    	finally
	    	{
	    		if (fis != null)
	    		{
					try
					{
						fis.close();
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}		
	    		}
	    	}
	    }
	}
	
	private void getPluginMd5s()
	{
		File pluginDir = new File("plugins");
		
		pluginDir.mkdirs();
	    
	    FilenameFilter statsFilter = new FilenameFilter() 
	    {
            public boolean accept(File paramFile, String paramString) 
            {
                if (paramString.endsWith("jar"))
                {
                    return true;
                }
                
                return false;
            }
	    };
	    
	    for (File f : pluginDir.listFiles(statsFilter))
	    {
	    	FileInputStream fis = null;
	    	
	    	try
	    	{
	    		fis = new FileInputStream(f);
	    		_jarMd5Map.put(f.getName(), DigestUtils.md5Hex(fis));
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}
	    	finally
	    	{
	    		if (fis != null)
	    		{
					try
					{
						fis.close();
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}		
	    		}
	    	}
	    }
	}

	@Override
	public void run() 
	{
		checkForNewFiles();
		
		if (_needUpdate)
		{
			BungeeCord.getInstance().broadcast(ChatColor.RED + "Connection Node" + ChatColor.DARK_GRAY + ">" + ChatColor.YELLOW + "This connection node will be restarting in " + _timeTilRestart + " minutes.");
		}
		else
		{
			return;
		}
		
		_timeTilRestart -= 2;
		
		if (_timeTilRestart < 0 || !_enabled)
		{
			BungeeCord.getInstance().stop();
		}
	}
}
