package mineplex.minecraft.game.core.boss.snake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.WorldEvent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

public class SnakeCreature extends EventCreature<Silverfish>
{
	private ArrayList<SnakeSegment> _segments = new ArrayList<SnakeSegment>();
	private double _seperator = 0.5;
	private ArrayList<Player> _canSee = new ArrayList<Player>();

	private Location _waypoint;
	private Vector _velocity = new Vector(0, 0, 0);

	private boolean _enabled = true;
	
	private double _speed = 0.75;
	
	private int _ticks = 0;

	public SnakeCreature(WorldEvent event, Location spawnLocation)
	{
		super(event, spawnLocation, "Serpent Lord", false, 2000, Silverfish.class);
	}

	@Override
	protected void spawnCustom()
	{
		getEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 0));
		UtilEnt.vegetate(getEntity());
		UtilEnt.ghost(getEntity(), true, false);
		Vector dir = new Vector(UtilMath.rr(1, true), 0, UtilMath.rr(1, true)).normalize().multiply(_seperator);

		getNewWaypoint();

		for (int i = 0; i < getHealth() / 50; i++)
		{
			SnakeSegment tail = new SnakeSegment(getSpawnLocation().toVector().subtract(dir.clone().multiply(-i)),
					new ItemStack(i == 0 ? Material.DROPPER : Material.BEDROCK));

			_segments.add(tail);
		}
	}

	private void getNewWaypoint()
	{
		// Bukkit.broadcastMessage("NEW WAYPOINT!");
		_waypoint = getSpawnLocation().clone().add(Math.random() * 60 - 30, Math.random() * 24 - 16, Math.random() * 60 - 30);
	}

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Location loc = _segments.get(0).getLocation().toLocation(getSpawnLocation().getWorld());

		ArrayList<Player> canSee = new ArrayList<Player>();

		for (Player player : loc.getWorld().getPlayers())
		{
			if (player.getLocation().distance(loc) < 120)
			{
				canSee.add(player);
			}
		}

		Iterator<Player> iter2 = _canSee.iterator();

		int[] ids = new int[_segments.size()];

		for (int a = 0; a < _segments.size(); a++)
		{
			ids[a] = _segments.get(a).getId();
		}

		Packet destroy = new PacketPlayOutEntityDestroy(ids);

		while (iter2.hasNext())
		{
			Player player = iter2.next();
			if (!canSee.contains(player))
			{
				iter2.remove();
				UtilPlayer.sendPacket(player, destroy);
			}
		}
		for (Player player : canSee)
		{
			if (!_canSee.contains(player))
			{
				_canSee.add(player);

				for (SnakeSegment tail : _segments)
				{
					UtilPlayer.sendPacket(player, tail.getSpawn());
				}
			}
		}
	}

	@Override
	public void dieCustom()
	{
		int[] ids = new int[_segments.size()];

		for (int a = 0; a < _segments.size(); a++)
		{
			ids[a] = _segments.get(a).getId();
		}

		Packet destroy = new PacketPlayOutEntityDestroy(ids);

		for (Player player : _canSee)
		{
			UtilPlayer.sendPacket(player, destroy);
		}
	}

	@EventHandler
	public void onTickMove(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_enabled)
		{
			return;
		}

		ArrayList<Packet> packets = new ArrayList<Packet>();

		for (int i = 0 ; i < _segments.size() ; i++)
		{
			SnakeSegment seg = _segments.get(i);

			Vector vec = seg.getLocation().clone();

			if (i == 0)
			{
				Vector direction = UtilAlg.getTrajectory(vec, _waypoint.toVector());

			
				_velocity.add(direction.multiply(0.1 * _speed));
				
				if (_velocity.length() > _speed)
					_velocity.normalize().multiply(_speed);

				// Bukkit.broadcastMessage("Loc: " + UtilWorld.vecToStrClean(moveTo));

				vec.add(_velocity);

				// Bukkit.broadcastMessage("Loc: " + UtilWorld.vecToStrClean(vec));

				if (UtilMath.offset(vec, _waypoint.toVector()) < 6)
				{
					getNewWaypoint();
				}
			}
			else
			{
				Vector infront = _segments.get(i - 1).getLocation();
				Vector behind = _segments.get(i - 1).getLastLocation();
				
				vec = infront.clone().add(UtilAlg.getTrajectory(infront, behind).multiply(_seperator));
			}
			
			Block block = _waypoint.getWorld().getBlockAt(seg.getLocation().getBlockX(),seg.getLocation().getBlockY(),seg.getLocation().getBlockZ()).getRelative(BlockFace.UP);
			if (block.getType() != Material.AIR && UtilBlock.isVisible(block))
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			}

			packets.addAll(Arrays.asList(seg.moveEntity(vec)));
		}

		Packet[] packetArray = packets.toArray(new Packet[0]);

		for (Player player : _canSee)
		{
			UtilPlayer.sendPacket(player, packetArray);
		}
	}

	@EventHandler
	public void command(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().contains("speed"))
		{
			try
			{
				_speed = Double.parseDouble(event.getMessage().split(" ")[1]);
				
				Bukkit.broadcastMessage("SPEED " + _speed);
			}
			catch (Exception e)
			{
				
			}
		}
	}

}
