package nautilus.game.arcade.game.games.minecraftleague.variation.wither.data;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.hologram.Hologram;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.tracker.PlaceSkullEvent;
import nautilus.game.arcade.game.games.minecraftleague.variation.wither.WitherVariation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntitySpawnEvent;

public class TeamAltar
{
	private WitherVariation _host;
	private GameTeam _team;
	private Location _center;

	//private List<Location> _protected;

	private Location[] _skullSpots;

	private int _placed;

	public TeamAltar(WitherVariation host, GameTeam team, Location center)
	{
		_host = host;
		_team = team;
		_center = center;

		spawnSoulsand();
		
		//_protected = UtilShapes.getSphereBlocks(center, 7, 7, false);
		
		Location labelLoc = center.clone().add(0/*.5*/, 5, 0.5);
		String labelStr = team.GetColor() + team.getDisplayName() + "'s Altar";
		Hologram label = new Hologram(host.Host.getArcadeManager().getHologramManager(), labelLoc, labelStr);
		label.start();
	}

	public boolean isInsideAltar(Location location)
	{
		return UtilMath.offset(_center, location) <= 7;
		//return _protected.contains(location);
	}

	public void spawnSoulsand()
	{
		_placed = 0;
		Location s1 = null;
		Location s2 = null;
		Location s3 = null;

		for (int i = -1; i <= 1; i++)
		{
			_center.getBlock().getRelative(i, 1, 0).setType(Material.SOUL_SAND);
			if (i == -1)
				s1 = _center.getBlock().getRelative(i, 2, 0).getLocation();
			if (i == 0)
				s2 = _center.getBlock().getRelative(i, 2, 0).getLocation();
			if (i == 1)
				s3 = _center.getBlock().getRelative(i, 2, 0).getLocation();
		}
		_center.getBlock().setType(Material.SOUL_SAND);

		_skullSpots = new Location[] {s1, s2, s3};
	}

	public boolean canBreak(Player player, Block block, boolean notify)
	{
		if (isInsideAltar(block.getLocation()))
		{
			if (notify)
				UtilPlayer.message(player, F.main("Game", "You cannot break blocks inside an Altar!"));
			return false;
			/*if (!_team.HasPlayer(player))
			{
				if (notify)
					UtilPlayer.message(player, F.main("Game", "This is not your Altar!"));
				return false;
			}
			if (block.getType() != Material.SKULL)
			{
				if (notify)
					UtilPlayer.message(player, F.main("Game", "You cannot break that block inside an Altar!"));
				return false;
			}*/
		}
		return true;
	}

	public boolean canPlace(Player player, Material blockType, Location location, boolean notify)
	{
		if (isInsideAltar(location))
		{
			if (!_team.HasPlayer(player))
			{
				if (notify)
					UtilPlayer.message(player, F.main("Game", "This is not your Altar!"));
				return false;
			}

			if (_host.WitherSpawned)
			{
				if (notify)
					UtilPlayer.message(player, F.main("Game", "A Wither is already spawned!"));
				return false;
			}

			if (blockType != Material.SKULL)
			{
				if (notify)
					UtilPlayer.message(player, F.main("Game", "You cannot place that inside an Altar!"));
				return false;
			}

			boolean passes = false;
			for (Location l : _skullSpots)
			{
				if (l.equals(location))
					passes = true;
			}

			if (!passes)
			{
				if (notify)
					UtilPlayer.message(player, F.main("Game", "That doesn't go there!"));
				return false;
			}

			if (_team.GetColor() == ChatColor.RED)
			{
				if (_host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.AQUA)) < 1)
				{
					if (notify)
						UtilPlayer.message(player, F.main("Game", "You do not need a Wither!"));
					return false;
				}
			}
			if (_team.GetColor() == ChatColor.AQUA)
			{
				if (_host.Host.getTowerManager().getAmountAlive(_host.Host.GetTeam(ChatColor.RED)) < 1)
				{
					if (notify)
						UtilPlayer.message(player, F.main("Game", "You do not need a Wither!"));
					return false;
				}
			}

			if (_team.HasPlayer(player) && blockType == Material.SKULL)
			{
				//_host.Host.Objective.resetPlayerToMainObjective(player);
				Bukkit.getPluginManager().callEvent(new PlaceSkullEvent(player));
				if (_placed < 2)
				{
					UtilTextMiddle.display("", _team.GetColor() + _team.getDisplayName() + " has placed a Skull on their Altar!");
					for (Player scare : UtilServer.getPlayers())
					{
						scare.playSound(scare.getLocation(), Sound.WITHER_SPAWN, 10, 0);
					}
				}
				else
				{
					_host.Host.CreatureAllowOverride = true;
				}
				_placed++;
			}
		}
		return true;
	}

	public boolean ownsWither(EntitySpawnEvent event)
	{
		if (event.getEntity() instanceof Wither)
		{
			Location base = event.getLocation();
			return (isInsideAltar(base.getBlock().getLocation()));
		}

		return false;
	}

	public Location getLocation()
	{
		return _center.clone();
	}
	
	public int getPlacedSkulls()
	{
		return _placed;
	}
}
