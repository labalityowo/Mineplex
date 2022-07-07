package nautilus.game.arcade.game.games.micro;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.micro.kits.KitArcher;
import nautilus.game.arcade.game.games.micro.kits.KitFighter;
import nautilus.game.arcade.game.games.micro.kits.KitWorker;
import nautilus.game.arcade.game.games.micro.mission.Last2Tracker;
import nautilus.game.arcade.game.modules.MapCrumbleModule;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.KillsWithinGameStatTracker;

public class Micro extends TeamGame
{

	private static final String[] DESCRIPTION =
			{
					"Gather some " + C.cGreen + "Blocks" + C.Reset + ".",
					"The " + C.cAqua + "Glass Barrier" + C.Reset + " will disappear in " + C.cRed + "10 seconds" + C.Reset + ".",
					"Be the " + C.cYellow + "Last Team" + C.Reset + " standing!"
			};
	private static final long BARRIER_TIME = TimeUnit.SECONDS.toMillis(10);
	private static final long CHEAT_TIME = TimeUnit.SECONDS.toMillis(4);

	private final Set<Block> _glass = new HashSet<>();
	private final Map<GameTeam, Location> _teamCenters = new HashMap<>();

	public Micro(ArcadeManager manager)
	{
		super(manager, GameType.Micro, new Kit[]
				{
						new KitArcher(manager),
						new KitWorker(manager),
						new KitFighter(manager)
				}, DESCRIPTION);

		StrictAntiHack = true;
		InventoryClick = true;
		ItemDrop = true;
		ItemPickup = true;
		BlockBreak = true;
		BlockPlace = true;

		new CompassModule()
				.register(this);

		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);

		new MapCrumbleModule()
				.setRate(3)
				.register(this);

		registerStatTrackers(
				new KillsWithinGameStatTracker(this, 8, "Annihilation")
		);

		registerMissions(
				new Last2Tracker(this)
		);

		registerChatStats(
				Kills,
				Assists,
				BlankLine,
				DamageTaken,
				DamageDealt
		);
	}

	@Override
	public void ParseData()
	{
		for (Location location : WorldData.GetCustomLocs("20"))
		{
			Block block = location.getBlock();

			_glass.add(block);
			block.setType(Material.STAINED_GLASS);
		}

		for (GameTeam team : GetTeamList())
		{
			_teamCenters.put(team, UtilAlg.getAverageLocation(team.GetSpawns()));
		}
	}

	@EventHandler
	public void preventGrow(BlockGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void preventFlow(BlockFromToEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (!IsLive())
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

		for (Player player : GetPlayers(true))
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
		if (event.getType() != UpdateType.FASTER || !IsLive() || _glass.isEmpty())
		{
			return;
		}

		if (!UtilTime.elapsed(GetStateTime(), CHEAT_TIME))
		{
			for (GameTeam team : GetTeamList())
			{
				Location expected = _teamCenters.get(team);

				for (Player player : team.GetPlayers(true))
				{
					Location location = UtilAlg.findClosest(player.getLocation(), _teamCenters.values());

					if (!location.equals(expected))
					{
						Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, GetName(), "Cheating");
					}
				}
			}
		}

		if (UtilTime.elapsed(GetStateTime(), BARRIER_TIME))
		{
			for (Block block : _glass)
			{
				MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
			}

			_glass.clear();
			_teamCenters.clear();
		}
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
	public void arrowDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
		{
			return;
		}

		event.AddKnockback("Increase", 1.6d);
	}
}
