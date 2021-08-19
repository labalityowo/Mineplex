package nautilus.game.arcade.game.games.evolution.mobs.perks;

import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class PerkBounceEVO extends Perk
{
	/**
	 * @author Mysticate
	 */
	
	private NautHashMap<LivingEntity, Long> _live = new NautHashMap<LivingEntity, Long>();

	public PerkBounceEVO()
	{
		super("Slime Slam", new String[]
				{
				});
	}

	//Literally just the slime slam code ;D
	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilInv.IsItem(event.getItem(), Material.SLIME_BALL, (byte) 0))
			return;
		
		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(player, GetName(), 7000);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
		
		if (useEvent.isCancelled())
			return;
		
		if (!Recharge.Instance.use(player, useEvent.getAbility(), useEvent.getCooldown(), true, true))
			return;

		UtilAction.velocity(player, player.getLocation().getDirection(), 1.6, false, 0, 0.4, .4, true);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);

		UtilParticle.PlayParticle(ParticleType.SLIME, player.getLocation(), .5F, .5F, .5F, 0F, 5, ViewDist.NORMAL, UtilServer.getPlayers());
	}

	@EventHandler
	public void collide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!Manager.GetGame().IsLive())
			return;

		//Collide
		for (Player player : Manager.GetGame().GetPlayers(true))
		{	
			if (UtilPlayer.isSpectator(player))
			{
				_live.remove(player);
				continue;
			}
			
			if (!_live.containsKey(player))
				continue;

			for (Player cur : UtilPlayer.getNearby(player.getLocation(), 2.0))
			{	
				if (!Manager.IsAlive(cur))
					continue;

				if (UtilPlayer.isSpectator(cur))
				{
					_live.remove(player);
					continue;
				}
				
				if (cur == player)
					continue;

				DoSlam(player, cur);
				_live.remove(player);
				break;
			}
		}
	}
	
	@EventHandler
	public void end(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		//End
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
				continue;
			
			if (!UtilEnt.isGrounded(player))
				continue;

			if (!_live.containsKey(player))
				continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))  
				continue;

			_live.remove(player);			
		}	
	}

	public void DoSlam(Player damager, LivingEntity damagee)
	{
		int damage = 8;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, damage, true, true, false,
				damager.getName(), GetName());	

		UtilAction.velocity(damager, new Vector());
		
		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
		
		damager.playSound(damager.getLocation(), Sound.SLIME_ATTACK, 1, 1);
		UtilParticle.PlayParticle(ParticleType.SLIME, damager.getLocation(), .5F, .5F, .5F, 0F, 5, ViewDist.NORMAL, UtilServer.getPlayers());
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;

		event.AddKnockback(GetName(), 2);
	}
}
