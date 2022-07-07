package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;

public class SlimeShield extends EventMob
{
	private SlimeBase _host;
	private double _radialLead = 0;
	private boolean teleport = true;

	public SlimeShield(EventBase event, Location location, SlimeBase host) 
	{
		super(event, location, "Slime Shield", false, 5000, EntityType.SLIME);

		_host = host;

		SpawnCustom();

		_host.ShieldRegister(this);
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Slime))
			return;

		Slime slime = (Slime)GetEntity();
		slime.setSize(2);	
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
	public void Orbit(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (
				GetEntity() == null || !GetEntity().isValid() || 
				_host.GetEntity() == null || _host.GetEntity().isDead() || !_host.GetEntity().isValid() ||
				UtilMath.offset(GetEntity(), _host.GetEntity()) > 10)
		{
			Die();
			return;
		}

		double sizeMod = 2;
		if (_host.GetEntity() instanceof SlimeKing) 			sizeMod = 2;
		else if (_host.GetEntity() instanceof SlimeKingHalf) 	sizeMod = 1.5;
		else if (_host.GetEntity() instanceof SlimeKingQuarter) sizeMod = 1;

		//Orbit
		double speed = 10d;
		double oX = Math.sin(_host.GetEntity().getTicksLived()/speed + _radialLead) * 2 * sizeMod; 
		double oY = 3 * sizeMod;
		double oZ = Math.cos(_host.GetEntity().getTicksLived()/speed + _radialLead) * 2 * sizeMod;

		if (teleport)
		{
			GetEntity().teleport(_host.GetEntity().getLocation().add(oX, oY, oZ));
			teleport = false;
		}
		else
		{
			UtilAction.velocity(GetEntity(), 
					UtilAlg.getTrajectory(GetEntity().getLocation(), _host.GetEntity().getLocation().add(oX, oY, oZ)), 
					0.4, false, 0, 0.1, 1, true);
		}
	}

	@Override
	public void Die()
	{
		_host.ShieldDeregister(this);

		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 10, 0.5, 
				Sound.SLIME_WALK2, 2f, 1f, Material.SLIME_BALL, (byte)0, false);

		Loot();
		Remove();	
	}

	@Override
	public void Loot()
	{

	}

	public void SetRadialLead(double lead)
	{
		_radialLead = lead;
	}

}
