package mineplex.game.nano.game.games.microbattle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.TeamGame;
import mineplex.game.nano.game.components.player.GiveItemComponent;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.game.nano.game.games.microbattle.components.MapCourruptionComponent;
import mineplex.game.nano.game.games.microbattle.components.TeamArmourComponent;

public class MicroBattle extends TeamGame
{

	private static final long BARRIER_TIME = TimeUnit.SECONDS.toMillis(10);

	private final Set<Block> _glass = new HashSet<>();

	public MicroBattle(NanoManager manager)
	{
		super(manager, GameType.MICRO_BATTLE, new String[]
				{
						"Gather some " + C.cGreen + "Blocks" + C.Reset + ".",
						"The " + C.cAqua + "Glass Barrier" + C.Reset + " will disappear in " + C.cRed + "10 seconds" + C.Reset + ".",
						"Be the " + C.cYellow + "Last Team" + C.Reset + " standing!"
				});

		_worldComponent
				.setBlockBreak(true)
				.setBlockPlace(true);

		_playerComponent
				.setItemDropPickup(true)
				.setHunger(true)
				.setItemMovement(true);

		new GiveItemComponent(this)
				.setItems(new ItemStack[]
						{
								new ItemBuilder(Material.IRON_SWORD)
										.setUnbreakable(true)
										.build(),
								new ItemBuilder(Material.STONE_SPADE)
										.setUnbreakable(true)
										.build(),
								new ItemBuilder(Material.STONE_PICKAXE)
										.setUnbreakable(true)
										.build(),
								new ItemBuilder(Material.STONE_AXE)
										.setUnbreakable(true)
										.build(),
								new ItemStack(Material.APPLE, 3)
						});

		new TeamArmourComponent(this);

		new MapCourruptionComponent(this)
				.setRate(8)
				.setEnableAfter(TimeUnit.SECONDS.toMillis(15), () -> announce(F.main(getManager().getName(), "The world begins to corrupt..."), Sound.ENDERDRAGON_GROWL));

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			getTeams().forEach(team ->
			{
				List<Player> alive = team.getAlivePlayers();

				if (alive.isEmpty())
				{
					return;
				}

				scoreboard.write(team.getChatColour() + C.Bold + team.getName());
				scoreboard.write(alive.size() + " Alive");

				scoreboard.writeNewLine();
			});

			scoreboard.draw();
		});
	}

	@Override
	protected void createTeams()
	{
		addTeam(new GameTeam(this, "Red", ChatColor.RED, Color.RED, DyeColor.RED, _mineplexWorld));
		addTeam(new GameTeam(this, "Yellow", ChatColor.YELLOW, Color.YELLOW, DyeColor.YELLOW, _mineplexWorld));
		addTeam(new GameTeam(this, "Green", ChatColor.GREEN, Color.LIME, DyeColor.LIME, _mineplexWorld));
		addTeam(new GameTeam(this, "Blue", ChatColor.AQUA, Color.AQUA, DyeColor.LIGHT_BLUE, _mineplexWorld));
	}

	@Override
	protected void parseData()
	{
		for (Location location : _mineplexWorld.getSpongeLocations(String.valueOf(Material.GLASS.getId())))
		{
			Block block = location.getBlock();

			_glass.add(block);
			block.setType(Material.STAINED_GLASS);
		}
	}

	@Override
	public void disable()
	{
		_glass.clear();
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (!isLive())
		{
			return;
		}

		ItemStack itemStack = event.getItemDrop().getItemStack();

		if (UtilItem.isSword(itemStack))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateHunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : getAlivePlayers())
		{
			if (player.getFoodLevel() < 2)
			{
				player.setFoodLevel(2);
			}
		}
	}

	@EventHandler
	public void updateGlassBarrier(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !isLive() || _glass.isEmpty() || !UtilTime.elapsed(getStateTime(), BARRIER_TIME))
		{
			return;
		}

		_glass.forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR));
		_glass.clear();
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (_glass.contains(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.FLINT)
		{
			event.setCancelled(true);
		}
	}
}
