package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkBladeVortex extends Perk
{
	public PerkBladeVortex() 
	{
		super("Blade Vortex", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to use " + C.cGreen + "Blade Vortex"
				});
	}


	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isWeapon(event.getPlayer().getItemInHand()))
			return;
		
		//Dont allow usage in early game
		if (!UtilTime.elapsed(Manager.GetGame().GetStateTime(), 30000))
			return;

		final Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 16000, true, true))
			return;

		Recharge.Instance.setDisplayForce(player, GetName(), true);

		event.setCancelled(true); 

		//Pull + Damage
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), 7);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(player))
				continue;

			//Damage Event
			Manager.GetDamage().NewDamageEvent(cur, player, null, 
					DamageCause.CUSTOM, 2 * targets.get(cur), false, true, false,
					player.getName(), GetName());

			//Velocity
			UtilAction.velocity(cur, 
					UtilAlg.getTrajectory2d(cur.getLocation().toVector(), player.getLocation().toVector()), 
					1.6 - 0.6*targets.get(cur), true, 0, 0.2, 1, true);

			//Inform
			if (cur instanceof Player)
				UtilPlayer.message((Player)cur, F.main("Game", F.name(player.getName()) +" hit you with " + F.skill(GetName()) + "."));	
		}

		//Animation
		for (double i=0 ; i<Math.PI * 2 ; i += 0.1)
		{
			final double j = i;

			final int ticksLived = player.getTicksLived();
			final Location loc = player.getLocation();

			Bukkit.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					double x = Math.sin(j + (ticksLived/50d)) * (j%(Math.PI/2d)) * 3;
					double z = Math.cos(j + (ticksLived/50d)) * (j%(Math.PI/2d)) * 3;

					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.clone().add(x, 1, z), 0f, 0f, 0f, 0, 1,
							ViewDist.LONG, UtilServer.getPlayers());

					x = Math.sin(j + (ticksLived/50d) + Math.PI/4) * (j%(Math.PI/2d)) * 3;
					z = Math.cos(j + (ticksLived/50d) + Math.PI/4) * (j%(Math.PI/2d)) * 3;

					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.clone().add(x, 1, z), 0f, 0f, 0f, 0, 1,
							ViewDist.LONG, UtilServer.getPlayers());

					//Sound
					player.getWorld().playSound(player.getLocation(), Sound.STEP_WOOL, 2f, 1f + (float)((j%(Math.PI/2d))/(Math.PI/2)));
				}
			}, (long) ((Math.PI/2d - (j%(Math.PI/2d))) * 8));
		}

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
}
