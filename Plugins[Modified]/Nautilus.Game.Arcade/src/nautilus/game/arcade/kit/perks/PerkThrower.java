package nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkThrower extends Perk implements IThrown
{
	public PerkThrower() 
	{
		super("Thrower",  new String[] 
				{
				C.cGray + "You can also pick up team mates!",
				C.cYellow + "Drop Weapon" + C.cGray + " to " + C.cGreen + "Throw Sheep / Players",
				});
	}
	
	@EventHandler
	public void Throw(PlayerDropItemEvent event)
	{
		Player thrower = event.getPlayer();

		if (thrower.getPassenger() == null)
			return;

		Entity throwee = thrower.getPassenger();
		if (throwee == null)
			return;

		thrower.eject();

		Entity throweeStack = throwee.getPassenger();
		if (throweeStack != null)
		{
			throwee.eject();
			throweeStack.leaveVehicle();

			final Entity fThrower = thrower;
			final Entity fThroweeStack = throweeStack;

			Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					fThrower.setPassenger(fThroweeStack);
				}
			}, 2);
			
			UtilPlayer.message(thrower, F.main("Game", "You threw " + UtilEnt.getName(throwee) + "."));
		}

		//Throw
		UtilAction.velocity(throwee, thrower.getLocation().getDirection(), 1.4, false, 0, 0.3, 0.8, true);
		Manager.GetProjectile().AddThrow(throwee, thrower, this, -1, true, false, true, false, 1f);

		//Audio
		thrower.getWorld().playSound(thrower.getLocation(), Sound.SHEEP_IDLE, 2f, 3f);
		
		//Disallow stacking for 0.5s
		Recharge.Instance.useForce(thrower, "Sheep Stack", 500);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target == null)
			return;
		
		if (target instanceof Player)
		{
			if (!Manager.GetGame().IsAlive((Player)target))
			{
				return;
			}
		}
		else
		{
			return;
		}

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.CUSTOM, 6, false, true, false,
				UtilEnt.getName(data.getThrower()), GetName());
		
		//Bounce
		Vector dir = UtilAlg.getTrajectory(data.getThrown(), target);
		if (dir.getY() < 0)			dir.setY(0);
		UtilAction.velocity(target, dir, 1.2, false, 0, 0.4, 1, true);
		
		dir = UtilAlg.getTrajectory(target, data.getThrown());
		if (dir.getY() < 0)			dir.setY(0);
		UtilAction.velocity(data.getThrown(), dir, 0.2, false, 0, 0.2, 1, true);

		//Effect
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.SHEEP_IDLE, 3f, 5f);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}