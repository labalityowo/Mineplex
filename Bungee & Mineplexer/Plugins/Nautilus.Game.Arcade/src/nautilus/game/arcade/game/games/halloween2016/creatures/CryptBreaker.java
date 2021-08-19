package nautilus.game.arcade.game.games.halloween2016.creatures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween.creatures.InterfaceMove;
import nautilus.game.arcade.game.games.halloween2016.Crypt;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

public abstract class CryptBreaker<T extends Creature> extends CreatureBase<T> implements InterfaceMove
{
	
	protected final Crypt _crypt;
	protected final List<Location> _doorLocations = new ArrayList<>();
	
	protected final int _cryptDamage;
	protected final int _cryptDamageRate;
	
	protected double _playerTargetForwardRange = 4;
	protected double _playerTargetBackRange = 1;
	protected double _playerFollowRange = 5;
	
	protected double _customCryptRange = -1;
	
	protected double _extraDamage;
	
	protected float _speed;
	
	protected boolean _targetPlayers = true;
	
	protected Halloween2016 Host16;
	
	public CryptBreaker(Halloween2016 game, String name, Class<T> mobClass, Location loc, int cryptDamage, int cryptDamageRate, float speed)
	{
		super(game, name, mobClass, loc);
		_crypt = game.getCrypt();
		_doorLocations.addAll(game.getInfrontOfDoorTargets());
		_cryptDamage = cryptDamage;
		_cryptDamageRate = cryptDamageRate;
		_speed = speed;
		Host16 = game;
		
		Creature ent = GetEntity();
		UtilEnt.setTickWhenFarAway(ent, true);
		ent.setRemoveWhenFarAway(false);
	}
	
	@Override
	public void SpawnCustom(T ent)
	{}

	@Override
	public void Update(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		
		if(inCryptRange())
		{
			attackCrypt();
		}
	}
	
	public boolean inCryptRange()
	{
		double width = UtilEnt.getWidth(GetEntity());
		if(_customCryptRange != -1)
		{
			width = _customCryptRange;
		}
		
		if(getClosestDoor().distanceSquared(GetEntity().getLocation()) <= width*width)
		{
			return true;
		}
		return false;
	}
	
	public void attackCrypt()
	{
		if(!_crypt.tryDamage(GetEntity(), _cryptDamage, _cryptDamageRate)) return;
		
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 0.7f + UtilMath.random.nextFloat() * 0.5f);
		swingArms();
	}
	
	public void swingArms()
	{
		PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
		packet.a = GetEntity().getEntityId();
		packet.b = 0;
		
		for(Player p : Host.GetPlayers(false))
		{
			UtilPlayer.sendPacket(p, packet);
		}
	}

	@Override
	public void Damage(CustomDamageEvent event)
	{
		if(_extraDamage > 0)
		{
			if(GetEntity().equals(event.GetDamagerEntity(true)))
			{
				event.AddMod("Mod", _extraDamage);
			}
		}
	}

	@Override
	public void Target(EntityTargetEvent event)
	{
		if(_crypt.isDestroyed()) return;
		
		if(!event.getEntity().equals(GetEntity())) return;
		if(event.getTarget() == null) return;
		
		if(!_targetPlayers)
		{
			event.setCancelled(true);
			return;
		}
		
		if(!(event.getTarget() instanceof Player))
		{
			event.setCancelled(true);
			return;
		}
		
		if(inCryptRange())
		{
			event.setCancelled(true);
			return;
		}
		
		Location door = getClosestDoor();
		Location target = event.getTarget().getLocation();
		if(target.distanceSquared(door) - _playerTargetBackRange*_playerTargetBackRange > door.distanceSquared(GetEntity().getLocation()))
		{
			event.setCancelled(true);
			return;
		}
		if(target.distanceSquared(GetEntity().getLocation()) <= _playerTargetForwardRange*_playerTargetForwardRange)
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@Override
	public Location GetRoamTarget()
	{
		if(_crypt.isDestroyed()) return GetPlayerTarget();
		
		Location door = getClosestDoor();
		double distDoor = door.distanceSquared(GetEntity().getLocation());
		
		if(!_targetPlayers) return door;
		
		Player player = null;
		double distance = -1;
		
		for(Player p : Host.GetPlayers(true))
		{
			if(p.getLocation().distanceSquared(door) - _playerTargetBackRange*_playerTargetBackRange > distDoor) continue;
			
			double dist = GetEntity().getLocation().distanceSquared(p.getLocation());
			if(player == null || dist < distance)
			{
				player = p;
				distance = dist;
			}
		}
		
		if(player != null && distance <= _playerTargetForwardRange*_playerTargetForwardRange) 
		{
			GetEntity().setTarget(player);
			return player.getLocation();
		}
		
		return door;
	}
	
	@Override
	public void Move()
	{
		if(_crypt.isDestroyed())
		{
			CreatureMove(GetEntity());
			return;
		}
		
		Location door = getClosestDoor();
		double distDoor = door.distanceSquared(GetEntity().getLocation());
		if(GetEntity().getTarget() != null)
		{
			Location target = GetEntity().getTarget().getLocation();
			if(target.distanceSquared(door) - _playerFollowRange*_playerFollowRange > distDoor)
			{
				GetEntity().setTarget(null);
				SetTarget(GetRoamTarget());
			}
		}
			
		if(GetEntity().getTarget() == null)
		{
			SetTarget(GetRoamTarget());
		}
		
		if(_customCryptRange > 0)
		{
			if(distDoor <= _customCryptRange*_customCryptRange)
			{
				SetTarget(GetEntity().getLocation());
			}
		}
		
		if(GetTarget() != null)
		{
			UtilEnt.CreatureMove(GetEntity(), GetTarget(), _speed);
			Host.moves++;
		}
	}
	
	public Location getClosestDoor()
	{
		Location loc = GetEntity().getLocation();
		Location door = null;
		double dist = -1;
		for(Location d : _doorLocations)
		{
			double testDist = d.distanceSquared(loc);
			if(door == null || testDist < dist)
			{
				door = d;
				dist = testDist;
			}
		}
		return door.clone();
	}

}
