package mineplex.game.clans.clans.boxes.extra;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.Pair;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.boxes.BoxManager.BoxType;

public class BuilderBox
{
	private static final Map<Pair<Material, Byte>, ItemStack> REPLACEMENT_ITEMS;
	
	static
	{
		Map<Pair<Material, Byte>, ItemStack> replacements = new HashMap<>();
		replacements.put(Pair.create(Material.STONE, (byte)0), new ItemStack(Material.STAINED_CLAY));
		replacements.put(Pair.create(Material.GLASS, (byte)0), new ItemStack(Material.STAINED_GLASS));
		replacements.put(Pair.create(Material.THIN_GLASS, (byte)0), new ItemStack(Material.STAINED_GLASS_PANE));
		replacements.put(Pair.create(Material.WOOL, (byte)0), new ItemStack(Material.WOOL));
		replacements.put(Pair.create(Material.CARPET, (byte)0), new ItemStack(Material.CARPET));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)0), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)1), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)2), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)3), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)4), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)5), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)6), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)7), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.RED_ROSE, (byte)8), new ItemStack(Material.RED_ROSE));
		replacements.put(Pair.create(Material.COBBLE_WALL, (byte)0), new ItemStack(Material.COBBLE_WALL));
		replacements.put(Pair.create(Material.JACK_O_LANTERN, (byte)0), new ItemStack(Material.GLOWSTONE));
		replacements.put(Pair.create(Material.SMOOTH_BRICK, (byte)0), new ItemStack(Material.SMOOTH_BRICK));
		
		REPLACEMENT_ITEMS = Collections.unmodifiableMap(replacements);
	}
	
	@SuppressWarnings("deprecation")
	private static ItemStack convert(ItemStack old)
	{
		if (old == null)
		{
			return null;
		}
		Pair<Material, Byte> pair = Pair.create(old.getType(), old.getData().getData());
		if (!REPLACEMENT_ITEMS.containsKey(pair))
		{
			return null;
		}
		ItemBuilder after = new ItemBuilder(REPLACEMENT_ITEMS.get(pair));
		if (after.getType() == Material.RED_ROSE)
		{
			after.setData((short)UtilMath.r(9));
		}
		else if (after.getType() == Material.COBBLE_WALL)
		{
			after.setData((short)1);
		}
		else if (after.getType() == Material.GLOWSTONE)
		{
			after.setData((short)0);
		}
		else if (after.getType() == Material.SMOOTH_BRICK)
		{
			after.setData(UtilMath.randomElement(new Short[] {1, 3}));
		}
		else
		{
			after.setData(UtilMath.randomElement(DyeColor.values()).getWoolData());
		}
		after.setAmount(old.getAmount());
		
		return after.build();
	}
	
	public static void open(Player player)
	{
		boolean used = false;
		for (int i = 0; i < player.getInventory().getSize(); i++)
		{
			ItemStack converted = convert(player.getInventory().getItem(i));
			if (converted == null)
			{
				continue;
			}
			used = true;
			player.getInventory().setItem(i, converted);
		}
		
		if (used)
		{
			ClansManager.getInstance().getInventoryManager().addItemToInventory(player, BoxType.BUILDER_BOX.getItemName(), -1);
			UtilPlayer.message(player, F.main("Builder's Box", "You have redeemed your box contents!"));
		}
	}
}