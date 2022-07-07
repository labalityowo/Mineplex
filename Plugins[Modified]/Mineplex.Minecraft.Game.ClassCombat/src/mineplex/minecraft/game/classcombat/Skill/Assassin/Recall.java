package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class Recall extends Skill
{

	private final Map<Player, LinkedList<Location>> _mainLocMap = new HashMap<>();
	private final Map<Player, LinkedList<Location>> _secondaryLocMap = new HashMap<>();
	private final Map<Player, LinkedList<Double>> _secondaryHealthMap = new HashMap<>();
	
	public Recall(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Drop Axe/Sword to Use.",
				"",
				"Go back in time #3#1 seconds,",
				"restoring your location and",
				"regeneration 3 for #2#+1 seconds",
				"Cannot be used while Slowed.",
				"",
				"Hold shift when using to go",
				"back 2 seconds, restoring your",
				"location and up to #1.5#0.5 health.",
				});
	}

	@Override
	public String GetRechargeString()
	{
		return "Recharge: #35#-5 Seconds";
	}
	
	@EventHandler
	public void use(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		int level = getLevel(player);
		if (level == 0)
			return;
		
		if (!UtilGear.isWeapon(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		boolean secondary = player.isSneaking();

		if (!secondary && player.hasPotionEffect(PotionEffectType.SLOW))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " while Slowed."));
			return;
		}

		if (isInWater(player))
		{
			player.sendMessage(F.main("Skill", "You cannot use " + F.skill(GetName()) +" while in water."));
			return;
		}

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		Bukkit.getServer().getPluginManager().callEvent(trigger);
		
		if (trigger.IsCancelled())
			return;

		if (!Recharge.Instance.use(player, GetName(), GetName(level), secondary ? 18000 - (level * 2000) : 35000 - (level * 5000), true, false))
			return;

		LinkedList<Location> locs = secondary ? _secondaryLocMap.remove(player) : _mainLocMap.remove(player);
		if (locs == null)
			return;

		LinkedList<Double> health =  _secondaryHealthMap.remove(player);
		if (health == null)
			return;

		Factory.runSync(() ->
		{
			if (player.isDead() || !player.getWorld().equals(locs.getLast().getWorld()))
			{
				return;
			}

			if (secondary)
			{
				//Heal
				double newHealth = Math.min(health.getLast(), player.getHealth() + 1.5 + (level / 2D));
				player.setHealth(newHealth);
			}
			else
			{
				Factory.Condition().Factory().Regen(GetName(), player, player, 1 + level, 2, false, true, false);
			}

			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);

			//Teleport
			Location current = player.getLocation();
			Location target = locs.getLast();

			player.teleport(target);

			//Inform
			UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill((secondary ? "Secondary " : "") + GetName(level)) + "."));

			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);

			while (UtilMath.offsetSquared(current, target) > 0.25)
			{
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, current, 0, 1f, 0, 0, 1, ViewDist.LONG);
				current = current.add(UtilAlg.getTrajectory(current, target).multiply(0.1));
			}
		});
	}

	@EventHandler
	public void storeLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			//Store
			_mainLocMap.computeIfAbsent(cur, (key) -> new LinkedList<>()).addFirst(cur.getLocation());
			_secondaryLocMap.computeIfAbsent(cur, (key) -> new LinkedList<>()).addFirst(cur.getLocation());
			_secondaryHealthMap.computeIfAbsent(cur, (key) -> new LinkedList<>()).addFirst(cur.getHealth());
			
			int level = getLevel(cur);
			
			long maxMainSize = (3 + level) * 20;
			long maxSecondarySize = 2 * 20;
			
			//Cull
			if (_mainLocMap.get(cur).size() > maxMainSize)
			{
				_mainLocMap.get(cur).removeLast();
			}
			
			if (_secondaryLocMap.get(cur).size() > maxSecondarySize)
			{
				_secondaryLocMap.get(cur).removeLast();
			}

			if (_secondaryHealthMap.get(cur).size() > maxSecondarySize)
			{
				_secondaryHealthMap.get(cur).removeLast();
			}
		}
	}
	
	@Override
	public void Reset(Player player) 
	{
		_mainLocMap.remove(player);
		_secondaryLocMap.remove(player);
		_secondaryHealthMap.remove(player);
	}
}