package nautilus.game.arcade.game.games.battleroyale;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.ArcadeManager;

class BattleRoyalePlayer
{
	private final Player _player;
	private final Location _location;
	private final Location _goal;
	private final Set<Location> _cageBlocks;
	private EnderDragon _dragon;
	private Chicken _chicken;
	private int _kills;
	private int _assists;

	BattleRoyalePlayer(ArcadeManager manager, Player player, Location location, Location goal)
	{
		_player = player;
		_location = location;
		_goal = goal;
		_cageBlocks = new HashSet<>();

		// Colour the cage based on the player's rank
		PermissionGroup group = manager.GetClients().Get(player).getPrimaryGroup();
		byte data = UtilColor.chatColorToWoolData(group.getColor());

		// Build the cage
		buildCage(data);
		// Teleport the player to the cage
		player.teleport(_location.add(0, 1, 0));
	}

	private void buildCage(byte colourData)
	{
		// Floor
		for (int x = -2; x <= 2; x++)
		{
			for (int z = -2; z <= 2; z++)
			{
				_location.add(x, -1, z);
				MapUtil.QuickChangeBlockAt(_location, Material.STAINED_GLASS, colourData);
				_cageBlocks.add(_location.clone());
				_location.subtract(x, -1, z);
			}
		}

		// Roof
		for (int x = -2; x <= 2; x++)
		{
			for (int z = -2; z <= 2; z++)
			{
				_location.add(x, 4, z);
				MapUtil.QuickChangeBlockAt(_location, Material.STAINED_GLASS, colourData);
				_cageBlocks.add(_location.clone());
				_location.subtract(x, 4, z);
			}
		}

		// Walls
		for (int y = 0; y < 4; y++)
		{
			for (int x = -2; x <= 2; x++)
			{
				for (int z = -2; z <= 2; z++)
				{
					if (x != -2 && x != 2 && z != -2 && z != 2)
					{
						continue;
					}

					_location.add(x, y, z);
					MapUtil.QuickChangeBlockAt(_location, Material.STAINED_GLASS, colourData);
					_cageBlocks.add(_location.clone());
					_location.subtract(x, y, z);
				}
			}
		}
	}

	public void removeCage()
	{
		for (Location location : _cageBlocks)
		{
			MapUtil.QuickChangeBlockAt(location, Material.AIR);
		}
	}

	public void spawnDragon()
	{
		_dragon = _location.getWorld().spawn(_location, EnderDragon.class);
		UtilEnt.vegetate(_dragon);
		UtilEnt.ghost(_dragon, true, false);

		_chicken = _location.getWorld().spawn(_location, Chicken.class);
		_chicken.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

		_dragon.setPassenger(_chicken);
		_chicken.setPassenger(_player);

		((CraftEnderDragon) _dragon).getHandle().setTargetBlock(_goal.getBlockX(), _goal.getBlockY(), _goal.getBlockZ());
	}

	public Location getLocation()
	{
		return _location;
	}

	public EnderDragon getDragon()
	{
		return _dragon;
	}

	public Chicken getChicken()
	{
		return _chicken;
	}

	public void incrementKills()
	{
		_kills++;
	}

	public int getKills()
	{
		return _kills;
	}

	public void incrementAssists()
	{
		_assists++;
	}

	public int getAssists()
	{
		return _assists;
	}
}
