package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkEvade extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkEvade() 
	{
		super("Evade", new String[] 
				{ 
				C.cYellow + "Block Attacks" + C.cGray + " with Sword to " + C.cGreen + "Evade",
				});
	}
		
	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isSword(event.getPlayer().getItemInHand()))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 2000, true, true))
			return;
	
		UtilPlayer.message(player, F.main("Game", "You prepared to " + F.skill(GetName()) + "."));
		
		_active.put(player, System.currentTimeMillis());
	}
	
	@EventHandler
	public void Energy(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> activeIterator = _active.keySet().iterator();
		
		while (activeIterator.hasNext())
		{
			Player player = activeIterator.next();
			long time = _active.get(player);
			
			if (UtilTime.elapsed(time, 1000))
			{
				activeIterator.remove();
				UtilPlayer.message(player, F.main("Game", "You failed to " + F.skill(GetName()) + "."));
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		//Damagee
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		//Blocking
		if (!damagee.isBlocking())
			return;

		//Active
		if (!_active.containsKey(damagee))
			return;

		//Damager
		LivingEntity damager = event.GetDamagerEntity(false);
		if (damager == null)	return;

		if (!Recharge.Instance.use(damagee, GetName(), 500, false, false))
			return;

		//Cancel
		event.SetCancelled(GetName());
		
		_active.remove(damagee);

		//Effect
		for (int i=0 ; i<3 ; i++)
			damagee.getWorld().playEffect(damagee.getLocation(), Effect.SMOKE, 5);

		//Location
		Location target = FindLocationBehind(damager, damagee);
		if (target == null)	
			return;
		
		//Effect
		UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, damagee.getLocation(), 
				(float)(Math.random() - 0.5), (float)(Math.random() * 1.4), (float)(Math.random() - 0.5), 0, 10,
				ViewDist.NORMAL, UtilServer.getPlayers());

		//Action
		damagee.teleport(target);

		//Invul/Cloak
		Manager.GetCondition().Factory().Invulnerable(GetName(), damagee, damagee, 0.5, false, false);

		//Inform
		UtilPlayer.message(damagee, F.main("Game", "You used " + F.skill(GetName()) + "."));
		UtilPlayer.message(damager, F.main("Game", F.name(damagee.getName()) +" used " + F.skill(GetName()) + "."));
	}

	private Location FindLocationBehind(LivingEntity damager, Player damagee) 
	{
		double curMult = 0;
		double maxMult = 1.5;

		double rate = 0.1;

		Location lastValid = damager.getLocation();

		while (curMult <= maxMult)
		{
			Vector vec = UtilAlg.getTrajectory(damager, damagee).multiply(curMult);
			Location loc = damager.getLocation().subtract(vec);

			if (!UtilBlock.airFoliage(loc.getBlock()) || !UtilBlock.airFoliage(loc.getBlock().getRelative(BlockFace.UP)))
				return lastValid;

			lastValid = loc;

			curMult += rate;
		}

		return lastValid;
	}
}
