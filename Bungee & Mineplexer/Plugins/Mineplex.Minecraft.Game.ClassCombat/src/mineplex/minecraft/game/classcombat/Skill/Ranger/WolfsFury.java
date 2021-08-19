package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class WolfsFury extends SkillActive
{

	private final Map<Player, Long> _active = new WeakHashMap<>();
	private final Set<Player> _swing = new HashSet<>();
	private final Set<Player> _miss = new HashSet<>();

	public WolfsFury(SkillFactory skills, String name, ClassType classType, SkillType skillType,
			int cost, int levels,
			int energy, int energyMod, 
			long recharge, long rechargeMod, boolean rechargeInform, 
			Material[] itemArray, 
			Action[] actionArray) 
	{
		super(skills, name, classType, skillType, 
				cost, levels,
				energy, energyMod, 
				recharge, rechargeMod, rechargeInform, 
				itemArray,
				actionArray);

		SetDesc(new String[] 
				{
				"Summon the power of the wolf, gaining",
				"Strength 4 for #2#2 seconds, and giving",
				"no knockback on your attacks.",
				"",
				"If you miss two consecutive attacks,",
				"Wolfs Fury ends."
				});
	}

	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}
		
		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Action
		_active.put(player, System.currentTimeMillis() + 8000);

		//Condition
		Factory.Condition().Factory().Strength(GetName(), player, player, 2 + 2*level, 3, false, true, true);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WOLF_GROWL, 1.4f, 1.2f);
	}

	@EventHandler
	public void Expire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_active.entrySet().removeIf(entry ->
		{
			if (System.currentTimeMillis() > entry.getValue())
			{
				End(entry.getKey());
				return true;
			}

			return false;
		});
	}

	@EventHandler
	public void Swing(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if(!_active.containsKey(event.getPlayer()))
			return;

		if (!UtilGear.isAxe(event.getPlayer().getItemInHand()) && !UtilGear.isSword(event.getPlayer().getItemInHand()))
			return;

		_swing.add(event.getPlayer());						
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Hit(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		int level = getLevel(damager);
		if (level == 0)			return;
		
		if (!_active.containsKey(damager))
			return;

		//Remove Swing
		_swing.remove(damager);

		//Remove Miss
		_miss.remove(damager);

		//Damage
		event.SetKnockback(false);

		//Effect
		if (!event.IsCancelled())
			damager.getWorld().playSound(damager.getLocation(), Sound.WOLF_BARK, 0.5f, 1.2f);
	}

	@EventHandler
	public void Miss(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_swing.removeIf(player ->
		{
			if (_miss.remove(player))
			{
				End(player);
			}
			else
			{
				_miss.add(player);
			}

			return true;
		});
	}

	public void End(Player player)
	{
		Reset(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), F.skill(GetName()) + " has ended."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WOLF_WHINE, 0.6f, 0.8f);
	}
	
	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : _active.keySet())
		{
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, player.getLocation(),
					(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 4,
					ViewDist.LONG);
		}
	}

	@Override
	public void Reset(Player player)
	{
		_active.remove(player);
		_swing.remove(player);
		_miss.remove(player);
		Factory.Condition().EndCondition(player, ConditionType.INCREASE_DAMAGE, GetName());
	}
}
