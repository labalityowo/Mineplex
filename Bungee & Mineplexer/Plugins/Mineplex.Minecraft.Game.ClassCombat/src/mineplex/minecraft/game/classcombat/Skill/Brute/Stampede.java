package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Stampede extends Skill
{
	private WeakHashMap<Player, Long> _sprintTime = new WeakHashMap<Player, Long>();
	private WeakHashMap<Player, Integer> _sprintStr = new WeakHashMap<Player, Integer>();

	public Stampede(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"You slowly build up speed as you",
				"sprint. You gain a level of Speed",
				"for every #5#-1 seconds, up to a max",
				"of Speed 2.",
				"",
				"Attacking during stampede deals",
				"#0.25#0.25 bonus damage,",
				"and +50% knockback per speed level.",
				"",
				"Resets if you take damage."
				});
	}

	@EventHandler
	public void Skill(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Player cur : GetUsers())
		{
			int level = getLevel(cur);
			if (level == 0)		continue;

			//Active - Check for Disable
			if (_sprintTime.containsKey(cur))
			{
				//Stopped
				if (!cur.isSprinting() || cur.getLocation().getBlock().isLiquid())
				{
					_sprintTime.remove(cur);
					_sprintStr.remove(cur);
					cur.removePotionEffect(PotionEffectType.SPEED);
					continue;
				}

				long time = _sprintTime.get(cur);
				int str = _sprintStr.get(cur);

				//Apply Speed
				if (str > 0)
					Factory.Condition().Factory().Speed(GetName(), cur, cur, 1.9, str-1, false, true, true);

				//Upgrade Speed
				if (!UtilTime.elapsed(time, 5000 - (1000 * level)))
					continue;

				_sprintTime.put(cur, System.currentTimeMillis());

				if (str < 2)
				{	
					_sprintStr.put(cur, str+1);

					//Effect
					cur.getWorld().playSound(cur.getLocation(), Sound.ZOMBIE_IDLE, 2f, 0.2f * str+1);
				}
				
				//Event
				UtilServer.getServer().getPluginManager().callEvent(new SkillEvent(cur, GetName(), ClassType.Brute));
			}
			else if (cur.isSprinting() || !cur.getLocation().getBlock().isLiquid())
			{
				//Start Timer
				if (!_sprintTime.containsKey(cur))
				{
					_sprintTime.put(cur, System.currentTimeMillis());
					_sprintStr.put(cur, 0);
				}
			}
		}
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Entity ent : _sprintStr.keySet())
		{
			UtilParticle.PlayParticle(ParticleType.CRIT, ent.getLocation(), 
					(float)(Math.random() - 0.5), 0.2f + (float)Math.random(), (float)(Math.random() - 0.5), 0, _sprintStr.get(ent) * 2,
					ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!_sprintStr.containsKey(damager))
			return;

		if (_sprintStr.get(damager) == 0)
			return;

		int level = getLevel(damager);
		if (level == 0)			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		//Remove
		_sprintTime.remove(damager);
		int str = _sprintStr.remove(damager);
		damager.removePotionEffect(PotionEffectType.SPEED);

		//Damage
		event.AddMod(damager.getName(), GetName(), 0.25 + (0.25 * level), true);
		event.AddKnockback(GetName(), 1 + 0.5 * str);

		//Inform
		UtilPlayer.message(damager, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
		UtilPlayer.message(damagee, F.main(GetClassType().name(), F.name(damager.getName()) + " hit you with " + F.skill(GetName(level)) + "."));

		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_WOOD, 1f, 0.4f * str); 
		
		//Event
		UtilServer.getServer().getPluginManager().callEvent(new SkillEvent(damager, GetName(), ClassType.Brute, damagee));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void DamageCancel(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;
		
		Reset(damagee);
		Factory.Condition().EndCondition(damagee, null, GetName());
	}

	@Override
	public void Reset(Player player) 
	{
		_sprintTime.remove(player);
		_sprintStr.remove(player);
	}
}
