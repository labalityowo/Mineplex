package nautilus.game.arcade.game.games.evolution.mobs.perks;

import java.util.HashMap;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
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
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class PerkSiesmicSlamEVO extends Perk
{
	/**
	 * @author Mysticate
	 */
	
	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	public PerkSiesmicSlamEVO()
	{
		super("Seismic Slam", new String[0]);
	}

	@EventHandler
	public void deactivateDeath(PlayerDeathEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!Kit.HasKit(event.getEntity()))
			return;
		
		if (_live.containsKey(event.getEntity()))
		{
			_live.remove(event.getEntity());
		}
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!Manager.GetGame().IsLive())
			return;

		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!UtilInv.IsItem(event.getItem(), Material.IRON_INGOT, (byte) 0))
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
		
		//Action
		Vector vec = player.getLocation().getDirection();
		if (vec.getY() < 0)
			vec.setY(vec.getY() * -1);

		UtilAction.velocity(player, vec, 1, true, 1, 0, 1, true);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void Slam(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!Manager.GetGame().IsLive())
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!_live.containsKey(player))
				continue;

			if (UtilPlayer.isSpectator(player))
			{
				_live.remove(player);
				continue;
			}
			
			if (!player.isOnGround())
				continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))  
				continue;

			_live.remove(player);
			
			//Action
			int damage = 16;
			double range = 6;
			
			HashMap<Player, Double> targets = UtilPlayer.getInRadius(player.getLocation(), range);
			for (Player cur : targets.keySet())
			{
				if (cur == player)
					continue;

				if (!Manager.IsAlive((Player) cur))
					continue;
				
				//Damage Event
				Manager.GetDamage().NewDamageEvent(cur, player, null, 
						DamageCause.CUSTOM, damage * targets.get(cur) + 0.5, true, true, false,
						player.getName(), GetName());	

				//Condition
				Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);
			}
			
			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);
			for (Block cur : UtilBlock.getInRadius(player.getLocation(), range).keySet())
				if (UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(cur))
					cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, cur.getTypeId());
		}	
	}
	
	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;
		
		event.AddKnockback(GetName(), 2.4);
	}
}
