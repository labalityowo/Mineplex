package mineplex.minecraft.game.classcombat.Skill.Knight;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Fortitude extends Skill
{
	private WeakHashMap<Player, Double> _preHealth = new WeakHashMap<Player, Double>();
	private WeakHashMap<Player, Double> _health = new WeakHashMap<Player, Double>();
	private WeakHashMap<Player, Long> _last = new WeakHashMap<Player, Long>();

	public Fortitude(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"After taking damage, you regenerate",
				"up to #0#1 of the health you lost.",
				"",
				"You restore health at a rate of",
				"1 health per #3#-0.5 seconds.",
				"",
				"This does not stack, and is reset if",
				"you are hit again."
				});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void RegisterPre(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;
		
		if (getLevel(damagee) <= 0)
			return;
		
		_preHealth.put(damagee, damagee.getHealth());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void RegisterLast(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		//Damager
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;
		
		int level = getLevel(damagee);
		if (level  == 0)		return;
		
		if (!_preHealth.containsKey(damagee))
			return;
		
		double diff = _preHealth.remove(damagee) - damagee.getHealth();
		
		_health.put(damagee, Math.min(level, diff));
		_last.put(damagee, System.currentTimeMillis());
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		Iterator<Entry<Player, Double>> healthIterator = _health.entrySet().iterator();
		
		while (healthIterator.hasNext())
		{
			Entry<Player, Double> entry = healthIterator.next();
			
			int level = getLevel(entry.getKey());
			if (level == 0)		
				continue;
			
			if (!UtilTime.elapsed(_last.get(entry.getKey()), 3000 - (500 * level)))
				continue;
			
			//Work out healing
			double toHeal = Math.min(entry.getValue(), 1);
			entry.setValue(entry.getValue() - toHeal);
			
			//Heal
			UtilPlayer.health(entry.getKey(), toHeal);
			
			//Effect
			UtilParticle.PlayParticle(ParticleType.HEART, entry.getKey().getEyeLocation(), 0, 0.2f, 0, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());
			
			//Finished
			if (entry.getValue() <= 0)
				healthIterator.remove();
			
			//Last Tick
			_last.put(entry.getKey(), System.currentTimeMillis());
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_preHealth.remove(player);
		_health.remove(player);
		_last.remove(player);
	}
}
