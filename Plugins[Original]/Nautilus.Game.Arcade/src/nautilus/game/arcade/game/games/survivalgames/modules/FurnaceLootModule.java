package nautilus.game.arcade.game.games.survivalgames.modules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;

import nautilus.game.arcade.events.ChestRefillEvent;
import nautilus.game.arcade.game.modules.Module;

public class FurnaceLootModule extends Module
{

	private final Set<Block> _lootedBlocks;
	private final List<ItemStack> _rawLoot = Arrays.asList
			(
					new ItemStack(Material.RAW_BEEF),
					new ItemStack(Material.RAW_CHICKEN),
					new ItemStack(Material.RAW_FISH),
					new ItemStack(Material.PORK),
					new ItemStack(Material.POTATO_ITEM)
			);
	private final List<ItemStack> _cookedLoot = Arrays.asList
			(
					new ItemStack(Material.COOKED_BEEF),
					new ItemStack(Material.COOKED_CHICKEN),
					new ItemStack(Material.COOKED_FISH),
					new ItemStack(Material.GRILLED_PORK),
					new ItemStack(Material.BAKED_POTATO),
					new ItemStack(Material.BAKED_POTATO),
					new ItemStack(Material.IRON_INGOT)
			);

	public FurnaceLootModule()
	{
		_lootedBlocks = new HashSet<>();
	}

	@EventHandler
	public void furnaceInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK) || !getGame().IsLive())
		{
			return;
		}

		Block block = event.getClickedBlock();

		if (block.getType() != Material.FURNACE || !_lootedBlocks.add(block))
		{
			return;
		}

		Furnace furnace = ((Furnace) block.getState());

		if (furnace.getCookTime() != 0)
		{
			return;
		}

		FurnaceInventory inventory = furnace.getInventory();
		int random = UtilMath.r(6);

		if (random == 0)
		{
			inventory.setFuel(new ItemStack(Material.STICK, UtilMath.r(2) + 1));
		}
		else if (random <= 3)
		{
			inventory.setSmelting(UtilAlg.Random(_rawLoot));
		}
		else
		{
			inventory.setResult(UtilAlg.Random(_cookedLoot));
		}
	}

	@EventHandler
	public void chestRefill(ChestRefillEvent event)
	{
		_lootedBlocks.clear();
	}
}
