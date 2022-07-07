package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class Bloodlust extends Skill
{

	private final Map<Player, Long> _time = new WeakHashMap<>();
	private final Map<Player, Integer> _str = new WeakHashMap<>();

	public Bloodlust(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Killing an enemy within 4#4 blocks",
				"you go into a Bloodlust,",
				"receiving Speed 1 and",
				"Strength 1 for #4#2 seconds.",
				"You also heal #0#1 health.",
				"",
				"Bloodlust can stack up to 3 times,",
				"boosting the level of Speed by 1."
				});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerDeath(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		if (event.GetLog().GetKiller() == null || !event.GetLog().GetKiller().IsPlayer())
			return;
		
		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (player == null)
			return;

		if (!Factory.Relation().canHurt(player, (Player)event.GetEvent().getEntity()))
			return;

		if (player.equals(event.GetEvent().getEntity()))
			return;

		//Level
		int level = getLevel(player);
		if (level == 0)		
			return;
		
		//Offset
		double distance = 4 + (4 * level);
		if (UtilMath.offset(event.GetEvent().getEntity().getLocation(), player.getLocation()) > distance)
			return;

		//Strength
		int str = 0;
		if (_str.containsKey(player))
			str = _str.get(player) + 1;
		str = Math.min(str, 2);
		_str.put(player, str);

		//Time
		double dur = 4 + (2 * level); 
		_time.put(player, (System.currentTimeMillis() + (long)(dur*1000)));

		//Condition
		Factory.Condition().Factory().Speed(GetName(), player, event.GetEvent().getEntity(), dur, str, false, true, true);
		Factory.Condition().Factory().Strength(GetName(), player, event.GetEvent().getEntity(), dur, 0, false, true, true);

		UtilPlayer.health(player, level);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You entered " + F.skill(GetName(level)) + " at " + F.elem("Level " + (str+1)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 2f, 0.6f);

	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : GetUsers())
			Expire(cur);
	}

	public boolean Expire(Player player)
	{
		if (!_time.containsKey(player))
			return false;

		if (System.currentTimeMillis() > _time.get(player))
		{
			int str = _str.remove(player);
			UtilPlayer.message(player, F.main(GetClassType().name(), "Your " + F.skill(GetName(getLevel(player))) + " has ended at " + F.elem("Level " + (str+1)) + "."));
			_time.remove(player);

			return true;
		}

		return false;
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity ent : _str.keySet())
		{
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, ent.getLocation(),
					(float)(Math.random() - 0.5), 0.2f + (float)Math.random(), (float)(Math.random() - 0.5), 0, _str.get(ent) * 2,
					ViewDist.NORMAL);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_time.remove(player);
		_str.remove(player);
	}
}
