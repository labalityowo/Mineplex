package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.kit.Perk;

public class PerkDeathsGrasp extends Perk
{
	private Map<UUID, Long> _live = new HashMap<>();
	private HashMap<LivingEntity, Long> _weakness = new HashMap<>();

	private int _cooldown;
	private int _damage;
	private int _leapDuration;
	private int _weaknessDuration;

	public PerkDeathsGrasp() 
	{
		super("Deaths Grasp", new String[]  
				{
				C.cYellow + "Left-Click" + C.cGray + " with Bow to use " + C.cGreen + "Deaths Grasp",
				C.cGray + "+100% Arrow Damage to enemies thrown by Deaths Grasp"
				});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damage = getPerkInt("Damage");
		_leapDuration = getPerkInt("Leap Duration (ms)");
		_weaknessDuration = getPerkInt("Weakness Duration (ms)");
	}

	@EventHandler
	public void leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isBow(event.getPlayer().getItemInHand()))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
			return;

		UtilAction.velocity(player, player.getLocation().getDirection(), 1.4, false, 0, 0.2, 1.2, true);

		//Record
		_live.put(player.getUniqueId(), System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_HURT, 1f, 1.4f);
	}

	@EventHandler
	public void end(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		//Leap End & Collide
		for(Iterator<Entry<UUID, Long>> it = _live.entrySet().iterator(); it.hasNext();)
		{
			Entry<UUID, Long> e = it.next();
			Player player = UtilPlayer.searchExact(e.getKey());
			if(player == null)
			{
				it.remove();
				return;
			}
						
			if (UtilEnt.isGrounded(player) && UtilTime.elapsed(e.getValue(), _leapDuration))
			{
				it.remove();
				return;
			}
			
			List<Player> team = TeamSuperSmash.getTeam(Manager, player, true);
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if(UtilPlayer.isSpectator(player) || team.contains(other))
				{
					continue;
				}
				if (UtilMath.offset(player, other) < 2)
				{
					collide(player, other);
					it.remove();
					return;
				}
			}
		}

		//Weakness End
		for (Iterator<LivingEntity> it = _weakness.keySet().iterator(); it.hasNext();)
		{
			LivingEntity ent = it.next();
			
			if (!UtilEnt.isGrounded(ent))
			{
				continue;
			}
			
			if (!UtilTime.elapsed(_weakness.get(ent), _weaknessDuration))
			{
				continue;
			}
			
			it.remove();
		}
	}
	
	public void collide(Player damager, LivingEntity damagee)
	{
		//Damage Event
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, _damage, false, true, false,
				damager.getName(), GetName());	
		
		UtilAction.velocity(damagee, UtilAlg.getTrajectory2d(damagee, damager), 1.6, false, 0, 1.2, 1.8, true);
		
		UtilAction.zeroVelocity(damager);
		
		damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_HURT, 1f, 0.7f);
		
		_weakness.put(damagee, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
		
		Recharge.Instance.recharge(damager, GetName());
		Recharge.Instance.use(damager, GetName(), 2000, true, true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void arrowDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
			return;

		if (!(event.GetProjectile() instanceof Arrow))
			return;
		
		if (!_weakness.containsKey(event.GetDamageeEntity()))
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;

		event.AddMult(GetName(), GetName() + " Combo", 2, true);
		
		UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, event.GetDamageeEntity().getLocation(), 0.5f, 0.5f, 0.5f, 0, 20, ViewDist.MAX);
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, event.GetDamageeEntity().getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX);
		
		damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_HURT, 1f, 2f);
	}
}
