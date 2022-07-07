package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class UtilSkull
{
	public static byte getSkullData(Entity entity)
	{
		if (entity == null)
			return 0;
		
		return getSkullData(entity.getType());
	}
	
	public static byte getSkullData(EntityType type)
	{
		if (type == EntityType.SKELETON)
			return 0;
		if (type == EntityType.WITHER)
			return 1;
		if (type == EntityType.ZOMBIE || type == EntityType.GIANT)
			return 2;
		if (type == EntityType.CREEPER)
			return 4;
		return 3;
	}

	public static ItemStack getPlayerHead(String playerName, String itemName, List<String> itemLore)
	{
		boolean displayHead = !playerName.isEmpty();
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 0, displayHead ? (byte) 3 : 0);
		SkullMeta meta = ((SkullMeta) skull.getItemMeta());
		if (displayHead)
			meta.setOwner(playerName);
		meta.setDisplayName(itemName);
		if (itemLore != null)
			meta.setLore(itemLore);
		skull.setItemMeta(meta);
		return skull;
	}

	public static boolean isPlayerHead(byte data)
	{
		return data == 3;
	}

	public static String getPlayerHeadName(Entity entity)
	{	
		return getPlayerHeadName(entity.getType());
	}
	
	public static String getPlayerHeadName(EntityType entity)
	{
		String name = "MHF_Alex";

		// order is important for some of these
		if (entity == EntityType.BLAZE)
			name = "MHF_Blaze";
		else if (entity == EntityType.CAVE_SPIDER)
			name = "MHF_CaveSpider";
		else if (entity == EntityType.SPIDER)
			name = "MHF_Spider";
		else if (entity == EntityType.CHICKEN)
			name = "MHF_Chicken";
		else if (entity == EntityType.MUSHROOM_COW)
			name = "MHF_MushroomCow";
		else if (entity == EntityType.COW)
			name = "MHF_Cow";
		else if (entity == EntityType.CREEPER)
			name = "MHF_Creeper";
		else if (entity == EntityType.ENDERMAN)
			name = "MHF_Enderman";
		else if (entity == EntityType.GHAST)
			name = "MHF_Ghast";
		else if (entity == EntityType.IRON_GOLEM)
			name = "MHF_Golem";
		else if (entity == EntityType.PIG_ZOMBIE)
			name = "MHF_PigZombie";
		else if (entity == EntityType.MAGMA_CUBE)
			name = "MHF_LavaSlime";
		else if (entity == EntityType.SLIME)
			name = "MHF_Slime";
		else if (entity == EntityType.OCELOT)
			name = "MHF_Ocelot";
		else if (entity == EntityType.PIG)
			name = "MHF_Pig";
		else if (entity == EntityType.SHEEP)
			name = "MHF_Sheep";
		else if (entity == EntityType.SQUID)
			name = "MHF_Squid";
		else if (entity == EntityType.PLAYER)
			name = "MHF_Steve";
		else if (entity == EntityType.VILLAGER)
			name = "MHF_Villager";

		return name;
	}
}
