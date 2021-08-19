package nautilus.game.arcade.game.games.quiver.module;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.quiver.Quiver;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;

public class ModulePowerup extends QuiverTeamModule implements Listener
{

	private static final int ARROWS_TO_GIVE = 1;
	private static final int FIREWORK_VIEW_DISTANCE = 10;
	private static final String DATA_POINT_POWERUP = "YELLOW";

	private Set<PowerupData> _data = new HashSet<>();
	
	private long _respawnDelay;

	public ModulePowerup(QuiverTeamBase base)
	{
		super(base);
		
		_respawnDelay = 20000;
	}

	@Override
	public void setup()
	{
		getBase().Manager.registerEvents(this);
		
		for (Location location : getBase().WorldData.GetDataLocs(DATA_POINT_POWERUP))
		{
			_data.add(new PowerupData(location));
		}
	}

	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.SEC)
		{
			return;
		}
		
		for (PowerupData data : _data)
		{
			data.update();
		}
	}

	@Override
	public void finish()
	{
		UtilServer.Unregister(this);
	}
	
	public void setRespawnDelay(long respawnDelay)
	{
		_respawnDelay = respawnDelay;
	}

	private void playFirework(Location location)
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (UtilMath.offset(location, player.getLocation()) < FIREWORK_VIEW_DISTANCE)
			{
				UtilFirework.packetPlayFirework(player, location, Type.BALL_LARGE, Color.YELLOW, false, true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		for (PowerupData data : _data)
		{
			data.pickup(event.getPlayer(), event.getItem());
		}
	}

	private class PowerupData
	{
		private Location _location;
		private Item _item;
		private long _lastPickup;
		
		public PowerupData(Location location)
		{
			_location = location;
			_lastPickup = System.currentTimeMillis();

			location.getBlock().getRelative(BlockFace.DOWN).setType(Material.IRON_BLOCK);
		}
		
		public void update()
		{
			if (_item != null)
			{
				if (!_item.isValid())
				{
					_item.remove();
					_item = null;
				}

				return;
			}

			if (UtilTime.elapsed(_lastPickup, _respawnDelay))
			{
				_item = _location.getWorld().dropItem(_location, new ItemStack(Material.ARROW));

				_item.setVelocity(new Vector(0, 1, 0));
				_item.getLocation().getBlock().getRelative(BlockFace.DOWN).setType(Material.GOLD_BLOCK);

				playFirework(_location);
			}
		}
		
		public void pickup(Player player, Item item)
		{
			if (_item == null)
			{
				return;
			}

			if (!_item.equals(item))
			{
				return;
			}

			if (UtilPlayer.isSpectator(player))
			{
				return;
			}

			GameTeam gameTeam = getBase().GetTeam(player);

			if (gameTeam == null)
			{
				return;
			}

			_item.remove();
			_item = null;

			_lastPickup = System.currentTimeMillis();

			_location.getBlock().getRelative(BlockFace.DOWN).setType(Material.IRON_BLOCK);

			player.sendMessage(F.main("Game", "You collected the resupply powerup."));

			playFirework(_location);

			ItemStack itemStack = Quiver.SUPER_ARROW.clone();
			itemStack.setAmount(ARROWS_TO_GIVE);

			player.getInventory().addItem(itemStack);
		}
	}
	
}
