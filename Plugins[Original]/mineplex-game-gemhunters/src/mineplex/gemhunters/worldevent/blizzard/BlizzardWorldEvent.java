package mineplex.gemhunters.worldevent.blizzard;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.playerstatus.PlayerStatus;
import mineplex.gemhunters.playerstatus.PlayerStatusModule;
import mineplex.gemhunters.playerstatus.PlayerStatusType;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventState;
import mineplex.gemhunters.worldevent.WorldEventType;

public class BlizzardWorldEvent extends WorldEvent
{

	private static final double START_CHANCE = 0.01;
	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(10);
	private static final long GRACE_TIME = TimeUnit.SECONDS.toMillis(60);
	private static final int DAMAGE = 2;
	private static final String TIP = "EQUIP LEATHER ARMOUR OR GET NEAR A FIRE";

	private final SafezoneModule _safezone;
	private final PlayerStatusModule _playerStatus;
	
	private boolean _colour;

	public BlizzardWorldEvent()
	{
		super(WorldEventType.BLZZARD);
		
		_safezone = Managers.get(SafezoneModule.class);
		_playerStatus = Managers.get(PlayerStatusModule.class);
	}

	@EventHandler
	public void trigger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWEST)
		{
			return;
		}
		
		if (Math.random() < START_CHANCE)
		{
			setEventState(WorldEventState.LIVE);
		}
	}
	
	@EventHandler
	public void weatherChange(WeatherChangeEvent event)
	{
		if (event.toWeatherState() && isEnabled())
		{
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void damage(UpdateEvent event)
	{
		if (!isInProgress())
		{
			return;
		}

		if (!UtilTime.elapsed(_start, GRACE_TIME))
		{
			if (event.getType() == UpdateType.SEC)
			{
				_colour = !_colour;
				UtilTextBottom.display((_colour ? C.cRedB : C.cWhiteB) + "STORM COMING IN " + UtilTime.MakeStr(_start + GRACE_TIME - System.currentTimeMillis()) + " " + TIP, UtilServer.getPlayers());
			}
		}
		else if (isWarmup())
		{
			setEventState(WorldEventState.LIVE);
			
			World world = _worldData.World;
			
			world.setStorm(true);
			world.setThundering(true);
		}
		else if (event.getType() == UpdateType.SEC_05 && isLive())
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (shouldDamage(player))
				{
					_playerStatus.setStatus(player, PlayerStatusType.COLD);
					_damage.NewDamageEvent(player, null, null, DamageCause.CUSTOM, DAMAGE, false, true, true, "Hurricane", "Frostbite");
				}
				else
				{
					_playerStatus.setStatus(player, PlayerStatusType.WARM);
				}
			}
		}
	}

	private boolean shouldDamage(Player player)
	{
		String safezone = _safezone.getSafezone(player.getLocation());
		
		if (safezone != null && safezone.contains(SafezoneModule.SAFEZONE_DATA_IGNORE))
		{
			return false;
		}
		
		for (Block block : UtilBlock.getInBoundingBox(player.getLocation().add(4, 2, 4), player.getLocation().subtract(4, 0, 4)))
		{
			if (block.getType() == Material.FIRE)
			{
				return false;
			}
		}
		
		for (ItemStack itemStack : player.getInventory().getArmorContents())
		{
			if (!UtilItem.isLeatherProduct(itemStack))
			{
				UtilTextBottom.display(C.cRedB + TIP, player);
				player.sendMessage(F.main(_worldEvent.getName(), "Equip leather armor or get near a fire to stop taking damage!"));
				return true;
			}
		}

		return false;
	}

	@Override
	public void onStart()
	{
	}

	@Override
	public boolean checkToEnd()
	{
		return UtilTime.elapsed(_start, MAX_TIME);
	}

	@Override
	public void onEnd()
	{
		World world = _worldData.World;
		
		world.setStorm(false);
		world.setThundering(false);
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			_playerStatus.setStatus(player, PlayerStatusType.DANGER, true);
		}
	}

	@Override
	public Location[] getEventLocations()
	{
		return null;
	}
	
	@Override
	public double getProgress()
	{
		return (double) (_start + MAX_TIME - System.currentTimeMillis()) / (double) MAX_TIME;
	}

}
