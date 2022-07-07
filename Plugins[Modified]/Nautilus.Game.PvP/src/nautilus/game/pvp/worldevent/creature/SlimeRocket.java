package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;

public class SlimeRocket extends EventMob
{
	private LivingEntity _target;
	
	public SlimeRocket(EventBase event, Location location, LivingEntity target) 
	{
		super(event, location, "Slime Rocket", false, 5000, EntityType.SLIME);
		
		_target = target;
		
		SpawnCustom();
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Slime))
			return;

		Slime slime = (Slime)GetEntity();
		slime.setSize(1);
	}
	
	@Override
	@EventHandler(priority = EventPriority.LOWEST)
	public void Damaged(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (GetEntity() == null)
			return;
		
		if (!event.GetDamageeEntity().equals(GetEntity()))
			return;

		event.SetCancelled("Slime King Rocket");
		
		if (event.GetProjectile() != null)
			Die();
	}
	
	@EventHandler
	public void Rocket(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (GetEntity() == null || !GetEntity().isValid() || GetEntity().getTicksLived() > 400 || _target == null || _target.isDead() || !_target.isValid())
		{
			Die();
			return;
		}
		
		if (UtilMath.offset(GetEntity(), _target) < 2)
			Die();
		else
			UtilAction.velocity(GetEntity(), UtilAlg.getTrajectory(GetEntity(), _target), 
				0.6, false, 0, 0.2, 1, true);
	}
	
	@Override
	public void Die()
	{
		Explode();
		
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 10, 0.5, 
				Sound.SLIME_WALK2, 1f, 1f, Material.SLIME_BALL, (byte)0, false);
		
		Loot();
		Remove();
	}
	
	@Override
	public void Loot()
	{
		
	}
	
	public void Explode()
	{
		if (GetEntity() == null)
		{
			System.out.println("NULL ENT");
			return;
		}
		
		GetEntity().getWorld().createExplosion(GetEntity().getLocation(), 0f);
		
		for (Player player : UtilPlayer.getNearby(GetEntity().getLocation(), 2))
			Event.Manager.Damage().NewDamageEvent(player, GetEntity(), null, 
					DamageCause.CUSTOM, 4, true, true, false,
					null, null);
	}
}
