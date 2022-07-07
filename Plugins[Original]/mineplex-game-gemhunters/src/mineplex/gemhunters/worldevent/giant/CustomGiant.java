package mineplex.gemhunters.worldevent.giant;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.spawn.SpawnModule;
import mineplex.minecraft.game.core.damage.DamageManager;

public class CustomGiant implements Listener
{

	private static final int GIANT_HEALTH = 500;
	private static final int GIANT_WIDTH = 5;
	private static final int GIANT_HEIGHT = 13;
	private static final float DESTORY_FALLING_BLOCK_CHANCE = 0.04F;
	private static final float MOVE_FACTOR = 0.3F;
	private static final int MAX_SEARCH_DISTANCE_SQUARED = 2500;
	private static final int TOO_CLOSE_DISTANCE_SQUARED = 625;
	private static final int DAMAGE_RADIUS = 4;
	private static final int DAMAGE = 4;

	private final DamageManager _damage;
	private final SafezoneModule _safezone;

	private final Monster _giant;

	private final Location _fallback;
	private Location _target;

	public CustomGiant(Location spawn)
	{
		_damage = Managers.require(DamageManager.class);
		_safezone = Managers.require(SafezoneModule.class);

		spawn.getChunk().load();
		_giant = spawn.getWorld().spawn(spawn, Giant.class);

		_giant.setMaxHealth(GIANT_HEALTH);
		_giant.setHealth(_giant.getMaxHealth());
		_giant.setRemoveWhenFarAway(false);

		UtilEnt.vegetate(_giant);
		UtilEnt.ghost(_giant, true, false);
		UtilEnt.setFakeHead(_giant, true);
		
		_fallback = Managers.get(SpawnModule.class).getCenter();
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || Bukkit.getOnlinePlayers().isEmpty())
		{
			if (event.getType() == UpdateType.SEC)
			{
				_target = acquireTarget();
			}

			return;
		}

		if (_target == null)
		{
			return;
		}
		
		if (_target.equals(_fallback) && UtilMath.offsetSquared(_giant.getLocation(), _fallback) < TOO_CLOSE_DISTANCE_SQUARED)
		{
			return;
		}

		if (_safezone.isInSafeZone(_giant.getLocation()))
		{
			_target = _fallback;
		}

		Vector direction = UtilAlg.getTrajectory2d(_giant.getLocation(), _target).multiply(MOVE_FACTOR);
		Location toTeleport = _giant.getLocation().add(direction);
		Location heightCheck = _giant.getLocation().add(direction.clone().multiply(2));
		
		if (Math.abs(UtilBlock.getHighest(_giant.getWorld(), heightCheck).getLocation().getY() - _giant.getLocation().getY()) <= 1)
		{
			toTeleport.add(0, 1, 0);
		}
		
		toTeleport.setYaw(UtilAlg.GetYaw(direction));
		toTeleport.setPitch(UtilAlg.GetPitch(direction));

		_giant.teleport(toTeleport);
	}

	@EventHandler
	public void updateBlockDestroy(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Block block : UtilBlock.getInBoundingBox(_giant.getLocation().add(-GIANT_WIDTH, 1, -GIANT_WIDTH), _giant.getLocation().add(GIANT_WIDTH, GIANT_HEIGHT, GIANT_WIDTH)))
		{
			if (_safezone.isInSafeZone(block.getLocation()))
			{
				continue;
			}

			if (Math.random() < DESTORY_FALLING_BLOCK_CHANCE)
			{
				FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());

				fallingBlock.setDropItem(false);
				fallingBlock.setHurtEntities(false);
				fallingBlock.setVelocity(new Vector(UtilMath.random(-1, 1), UtilMath.random(0.5, 1), UtilMath.random(-1, 1)));
			}

			block.setType(Material.AIR);
		}
	}
	
	@EventHandler
	public void updateDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}
		
		for (Player player : UtilPlayer.getInRadius(_giant.getLocation(), DAMAGE_RADIUS).keySet())
		{
			_damage.NewDamageEvent(player, _giant, null, DamageCause.ENTITY_ATTACK, DAMAGE, true, true, false, UtilEnt.getName(_giant), "Zombie Awakening");
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		if (event.getPlayer().equals(_target))
		{
			_target = acquireTarget();
		}
	}

	public Location acquireTarget()
	{
		Collection<Player> ignore = new ArrayList<>();

		for (Player player : UtilServer.getPlayers())
		{
			if (UtilPlayer.isSpectator(player) || _safezone.isInSafeZone(player.getLocation()))
			{
				ignore.add(player);
			}
		}

		Player player = UtilPlayer.getClosest(_giant.getLocation(), ignore);

		if (player == null)
		{
			return _fallback;
		}

		return UtilMath.offsetSquared(_giant, player) > MAX_SEARCH_DISTANCE_SQUARED ? _fallback : player.getLocation();
	}

	public Monster getGiant()
	{
		return _giant;
	}

}
