package mineplex.minecraft.game.classcombat.Skill.Knight;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Riposte extends SkillActive
{
	private HashMap<Player, Long> _prepare = new HashMap<Player, Long>();
	private HashMap<Player, LivingEntity> _block = new HashMap<Player, LivingEntity>();

	public Riposte(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Block an incoming attack to parry,",
				"then quickly return the attack ",
				"to riposte.",
				"",
				"If successful, you deal an additional",
				"#0#0.5 bonus damage.",
				"",
				"You must block, parry, then riposte",
				"all within 1 second of each other."
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
		//Prepare
		_prepare.put(player, System.currentTimeMillis() + 1000);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared to " + F.skill(GetName(level)) + "."));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void DoParry(CustomDamageEvent event)
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

		if (!_prepare.containsKey(damagee))
			return;

		//Damager
		LivingEntity damager = event.GetDamagerEntity(false);
		if (damager == null)	return;

		//Level
		int level = getLevel(damagee);
		if (level == 0)			return;

		//Prepare
		if (!_block.containsKey(damagee))
		{
			//Cancel
			event.SetCancelled(GetName() + " Parry");

			//Add
			_block.put(damagee, damager);
			_prepare.put(damagee, System.currentTimeMillis() + 1000);

			//Effect
			damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_METAL, 0.5f, 1.6f);
			
			Factory.Condition().Factory().Invulnerable(GetName(), damagee, damagee, 0.3, false, false);

			//Inform
			UtilPlayer.message(damagee, F.main(GetClassType().name(), "You parried with " + F.skill(GetName(level)) + "."));
			UtilPlayer.message(event.GetDamagerPlayer(false), F.main(GetClassType().name(), F.name(damagee.getName()) + " parried with " + F.skill(GetName(level)) + "."));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DoRiposte(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		//Damager
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!_prepare.containsKey(damager))
			return;

		//Level
		int level = getLevel(damager);
		if (level == 0)			return;

		//Counter
		if (_block.containsKey(damager))
		{
			//End
			LivingEntity target = _block.remove(damager);
			_prepare.remove(damager);

			//Damage
			event.AddMod(damager.getName(), GetName(), 0.5 * level, true);

			//Effect
			damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_METAL, 1f, 1.2f);

			//UtilPlayer.health(damager, 1);
			//UtilParticle.PlayParticle(ParticleType.HEART, damager.getEyeLocation(), 0, 0.3f, 0, 0, 1);

			//Inform
			UtilPlayer.message(damager, F.main(GetClassType().name(), "You countered with " + F.skill(GetName(level)) + "."));
			UtilPlayer.message(target, F.main(GetClassType().name(), F.name(damager.getName()) + " countered with " + F.skill(GetName(level)) + "."));
		}
	}

	@EventHandler
	public void End(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		HashSet<Player>	expired = new HashSet<Player>();

		for (Player cur : _prepare.keySet())
			if (System.currentTimeMillis() > _prepare.get(cur))
				expired.add(cur);

		for (Player cur : expired)
		{
			//Remove
			_prepare.remove(cur);
			_block.remove(cur);

			//Inform
			UtilPlayer.message(cur, F.main(GetClassType().name(), "You failed to " + F.skill(GetName()) + "."));
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_prepare.remove(player);
		_block.remove(player);
	}
}
