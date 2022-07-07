package mineplex.core.common.util;

import java.io.File;
import java.io.FileInputStream;

import org.bukkit.inventory.ItemStack;

import com.java.sk89q.jnbt.NBTInputStream;
import com.java.sk89q.jnbt.NamedTag;

public class UtilOfflinePlayer
{
	public static ItemStack loadOfflineInventory(File file)
	{
		try (NBTInputStream stream = new NBTInputStream(new FileInputStream(file)))
		{
			NamedTag tag = stream.readNamedTag();
			
			System.out.println(tag);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
   
		return null;
	}
}
