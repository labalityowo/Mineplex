package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class PerkFlameSlam extends Perk
{
	private HashMap<Player, Long> _live = new HashMap<Player, Long>();

	public PerkFlameSlam() 
	{
		super("Flame Charge", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Flame Charge"
				});
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return; 
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " while airborne."));
			return;
		}
		
		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		//UtilAction.velocity(player, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 0.4, true);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void End(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Collide
		HashMap<Player, Long> copy = new HashMap<Player, Long>();
		copy.putAll(_live);
		
		for (Player player : copy.keySet())
		{
			if (!Manager.IsAlive(player) || UtilPlayer.isSpectator(player))
			{
				_live.remove(player);
				continue;
			}
			
			Vector vel = player.getLocation().getDirection();
			vel.setY(0);
			UtilAlg.Normalize(vel);
			UtilAction.velocity(player, vel.multiply(0.8));
			
			//Particle
			UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0, 5,
					ViewDist.LONGER, UtilServer.getPlayers());
			
			for (Entity other : player.getWorld().getEntities())
			{
				if (!(other instanceof LivingEntity))
					continue;
				
				if (other instanceof Player)
					continue;
				
				if (UtilMath.offset(player, other) > 1.5)
					continue;
	
				DoSlam(player, (LivingEntity)other);
				_live.remove(player);
				break;
			}
		}
		
		//End
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!_live.containsKey(player))
				continue;
			
			if (UtilTime.elapsed(_live.get(player), 800))
				_live.remove(player);			
		}	
	}
	
	public void DoSlam(Player damager, LivingEntity damagee)
	{
		UtilAction.velocity(damager, UtilAlg.getTrajectory2d(damagee, damager), 1, true, 0.4, 0, 0.4, true);
		
		for (Entity other : damagee.getWorld().getEntities())
		{
			if (!(other instanceof LivingEntity))
				continue;
			
			if (other instanceof Player)
				continue;
			
			if (other.equals(damagee))
				continue;
			
			if (UtilMath.offset(other, damagee) < 2.5)
			{
				//Damage Event
				Manager.GetDamage().NewDamageEvent((LivingEntity)other, damager, null, 
						DamageCause.CUSTOM, 6, true, true, false,
						damager.getName(), GetName());	
			}
		}
		
		//Damage Event
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, 24, true, true, false,
				damager.getName(), GetName());	

		UtilParticle.PlayParticle(ParticleType.LAVA, damagee.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 30,
				ViewDist.MAX, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, damagee.getLocation().add(0, 1, 0), 0f, 0f, 0f, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		
		damager.getWorld().playSound(damagee.getLocation(), Sound.EXPLODE, 1f, 1f);
	}
	
	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;
		
		event.AddKnockback(GetName(), 3);
	}
}
