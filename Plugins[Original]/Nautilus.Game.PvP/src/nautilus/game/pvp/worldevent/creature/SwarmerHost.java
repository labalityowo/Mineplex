package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobBoss;
import nautilus.game.pvp.worldevent.EventMobMinion;

public class SwarmerHost extends EventMobBoss
{
	private int _minionsMax = 60;

	public SwarmerHost(EventBase event, Location location) 
	{
		super(event, location, "Swarmer", true, 120, EntityType.SILVERFISH);
	}

	@Override
	public void DamagedCustom(CustomDamageEvent event)
	{
		if (!_minions.isEmpty())
		{
			event.SetCancelled("Minion");
			return;
		}

		if (event.GetCause() == DamageCause.FALL)
		{
			if (!event.GetDamageeEntity().equals(GetEntity()))
				return;

			event.SetCancelled("Swarm Resistance");
		}

		if (event.GetCause() == DamageCause.FIRE)
		{
			if (!event.GetDamageeEntity().equals(GetEntity()))
				return;

			GetEntity().setFireTicks(0);

			event.SetCancelled("Swarm Resistance");
		}
	}

	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 50, 0.8, 
				Sound.SILVERFISH_KILL, 2f, 0.2f, Material.BONE, (byte)0, false);
		Loot();
		Remove();
	}

	@Override
	public void Loot() 
	{
		Event.Manager.Loot().DropLoot(GetEntity().getEyeLocation(), 40, 40, 0.2f, 0.05f, 3d);
	}

	@EventHandler
	public void MinionSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (GetEntity() == null)
			return;

		if (_minions.size() >= _minionsMax)
			return;
		
		if (GetHealthCur() <= _minions.size())
			return;

		Event.CreatureRegister(new Swarmer(Event, GetEntity().getLocation(), this));
	}

	@Override
	public void MinionDeregister(EventMobMinion minion) 
	{
		_minions.remove(minion);
		ApplyDamage(1);
	}	

	@EventHandler
	public void SendAttacker(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		if (_minions.size() == 0)
			return;
		
		for (Player player : UtilPlayer.getNearby(GetEntity().getLocation(), 16))
		{
			EventMobMinion minion = _minions.get(UtilMath.r(_minions.size()));
			minion.SetTarget(player);
		}
	}

	@Override
	public void DistanceAction() 
	{
		//XXX
	}
}
