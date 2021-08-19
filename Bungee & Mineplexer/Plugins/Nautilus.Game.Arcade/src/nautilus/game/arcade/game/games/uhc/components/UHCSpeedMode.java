package nautilus.game.arcade.game.games.uhc.components;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.uhc.UHC;
import nautilus.game.arcade.game.modules.CutCleanModule;

public class UHCSpeedMode implements Listener
{

	// The max Y value that a player can have in order to have night vision.
	public static final long NIGHT_VISION_MAX_Y = 54;

	// The rate that an apple will drop when breaking a leaves block
	public static final double APPLE_DROP_RATE = 0.1;
	
	private static final ItemStack[] PLAYER_ITEMS = {
		new ItemStack(Material.STONE_SWORD),
		new ItemStack(Material.STONE_PICKAXE),
		new ItemStack(Material.STONE_AXE),
		new ItemStack(Material.STONE_SPADE),
		new ItemStack(Material.COOKED_BEEF, 10),
		new ItemStack(Material.WOOD, 32)
	};
	
	private UHC _host;
	
	public UHCSpeedMode(UHC host)
	{
		_host = host;
		
		new CutCleanModule()
		.associateBlockDrop(Material.GOLD_ORE, new ItemBuilder(Material.GOLD_INGOT).build())
		.associateBlockDrop(Material.IRON_ORE, new ItemBuilder(Material.IRON_INGOT).build())
		.associateBlockDrop(Material.GRAVEL, new ItemStack(Material.FLINT))
		.associateMobDrop(Material.RAW_BEEF, new ItemBuilder(Material.COOKED_BEEF).build())
		.associateMobDrop(Material.RAW_CHICKEN, new ItemBuilder(Material.COOKED_CHICKEN).build())
		.associateMobDrop(Material.RAW_FISH, new ItemBuilder(Material.COOKED_FISH).build())
		.associateMobDrop(Material.PORK, new ItemBuilder(Material.GRILLED_PORK).build())
		.associateMobDrop(Material.RABBIT, new ItemBuilder(Material.COOKED_RABBIT).build())
		.associateMobDrop(Material.MUTTON, new ItemBuilder(Material.COOKED_MUTTON).build())
		.register(host);
		
		host.Manager.registerEvents(this);
	}
	
	@EventHandler
	public void start(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}
		
		for (Player player : _host.GetPlayers(true))
		{
			player.getInventory().addItem(PLAYER_ITEMS);
		}
	}
	
	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}
		
		UtilServer.Unregister(this);
	}
	
	@EventHandler
	public void nightVision(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : _host.GetPlayers(true))
		{
			if (player.getLocation().getY() <= NIGHT_VISION_MAX_Y)
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
			}
			else
			{
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
		}
	}

	@EventHandler
	public void appleDrop(BlockBreakEvent event)
	{
		Block block = event.getBlock();

		if (block.getType() != Material.LEAVES)
		{
			return;
		}

		if (Math.random() < APPLE_DROP_RATE)
		{
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.APPLE));
		}
	}
	
}
