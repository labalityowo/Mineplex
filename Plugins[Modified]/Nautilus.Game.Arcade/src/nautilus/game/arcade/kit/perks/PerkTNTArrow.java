package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkTNTArrow extends Perk
{
	private final Set<Player> _active = new HashSet<>();
	private final Set<Arrow> _arrows = new HashSet<>();
	
	public PerkTNTArrow() 
	{
		super("Explosive Arrow", new String[] 
				{
				C.cYellow + "Left-Click" + C.cGray + " with Bow to prepare " + C.cGreen + "Explosive Arrow"
				});
	}

	@EventHandler
	public void activate(PlayerInteractEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (event.getPlayer().getItemInHand().getType() != Material.BOW)
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player) || !Recharge.Instance.usable(player, GetName(), true))
			return;
		
		event.setCancelled(true);
		
		if (!player.getInventory().contains(Material.TNT))
		{
			UtilPlayer.message(player, F.main("Game", "You have no " + F.item("TNT") + "."));	
			return;
		}
			
		if (_active.contains(player))
			return;

		//Use Stock
		UtilInv.remove(player, Material.TNT, (byte)0, 1);
		UtilInv.Update(player);
		
		//Action
		_active.add(player);
				
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 2.5f, 2.0f);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You prepared " + F.skill(GetName()) + "."));	
	}

	@EventHandler
	public void shootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();

		if (!_active.remove(player) || !Recharge.Instance.use(player, GetName(), 6000, true, true))
		{
			return;
		}

		//Inform
		UtilPlayer.message(player, F.main("Game", "You fired " + F.skill(GetName()) + "."));	

		_arrows.add((Arrow)event.getProjectile());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void hitEntityTrigger(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile projectile = event.GetProjectile();
		if (projectile == null)	
			return;

		if (!_arrows.remove(event.GetProjectile()))
			return;

		event.SetCancelled(GetName());

		Location loc = event.GetDamageeEntity().getLocation().subtract(event.GetProjectile().getVelocity().normalize().multiply(0.1));
		
		trigger((Arrow)event.GetProjectile(), loc);
	}

	@EventHandler
	public void hitBlockTrigger(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();

		if (!_arrows.contains(proj))
			return;

		if (proj.getShooter() == null)
			return;

		if (!(proj.getShooter() instanceof Player))
			return;

		final Arrow arrow = (Arrow)proj;

		Manager.runSyncLater(() ->
		{
			try
			{
				//If it hasnt already triggered (via damage)
				if (_arrows.remove(arrow))
					trigger(arrow, arrow.getLocation());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}, 0);

		//Remove
		proj.remove();
	}

	@EventHandler
	public void cleanTrigger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Iterator<Arrow> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Arrow arrow = arrowIterator.next();

			if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || arrow.getTicksLived() > 60)
			{
				arrowIterator.remove();
				trigger(arrow, arrow.getLocation());
			}
		}
	}

	public void trigger(Arrow arrow, Location loc)
	{
		if (arrow == null)
			return;

		if (arrow.getShooter() == null || !(arrow.getShooter() instanceof Player))
		{
			arrow.remove();
			return;
		}

		Player player = (Player)arrow.getShooter();

		//Velocity Players
		HashMap<Player,Double> hitMap = UtilPlayer.getInRadius(loc, 7);
		for (Player cur : hitMap.keySet())
		{	
			double range = hitMap.get(cur);

			//Condition
			Manager.GetCondition().Factory().Falling(GetName(), cur, player, 6, false, true);

			//Damage Event
			Manager.GetDamage().NewDamageEvent(cur, player, null, 
					DamageCause.CUSTOM, 8 * range, false, true, false,
					player.getName(), GetName());	

			//Velocity
			UtilAction.velocity(cur, UtilAlg.getTrajectory2d(loc, cur.getLocation().add(0, 1, 0)), 
					0.4 + 0.8 * range, false, 0, 0.2 + 0.5 * range, 1.2, true);

			//Inform
			UtilPlayer.message(cur, F.main("Game", F.name(player.getName()) +" hit you with " + F.skill(GetName()) + "."));
		}
		
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, loc, 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		loc.getWorld().playSound(loc, Sound.EXPLODE, 2f, 0.75f);
	}
	
	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity ent : _arrows)
			UtilParticle.PlayParticleToAll(ParticleType.LAVA, ent.getLocation(), 0, 0, 0, 0, 1, ViewDist.LONG);
	}
}
