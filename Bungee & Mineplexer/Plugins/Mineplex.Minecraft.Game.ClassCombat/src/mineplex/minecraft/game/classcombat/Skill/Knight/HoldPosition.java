package mineplex.minecraft.game.classcombat.Skill.Knight;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.core.condition.events.ConditionExpireEvent;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class HoldPosition extends SkillActive
{
	public HoldPosition(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Hold your position, gaining",
				"Protection 3, Slow 3 and no",
				"knockback for #3#1 seconds."
				});
	}

	private final Map<Player, Integer> _foodLevel = new HashMap<>();

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
		double duration = 3 + (1 * level);

		//Action
		Factory.Condition().Factory().Slow(GetName(), player, player, duration, 2, false, true, false, true);
		Factory.Condition().Factory().Protection(GetName(), player, player, duration, 2, false, false, true);
		Factory.Condition().Factory().ArcadeHungerDisable(GetName(), player, player, duration, false);
		Factory.Condition().Factory().Jump(GetName(), player, player, duration, 250, false, false, false);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 1.5f, 0f);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 49);

		_foodLevel.put(player, player.getFoodLevel());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		int level = getLevel(damagee);
		if (level == 0)		return;

		Condition data = Factory.Condition().GetActiveCondition(damagee, ConditionType.DAMAGE_RESISTANCE);
		if (data == null)	return;

		if (!data.GetReason().equals(GetName()))
			return;

		//Damage
		event.AddMod(damagee.getName(), GetName(), 0, false);
		event.SetKnockback(false);
	}

	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetUsers())
		{
			Condition data = Factory.Condition().GetActiveCondition(player, ConditionType.DAMAGE_RESISTANCE);
			if (data == null)	continue;

			if (!data.GetReason().equals(GetName()))
				continue;

			UtilParticle.PlayParticle(ParticleType.MOB_SPELL, player.getLocation(), 
					(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 3 + getLevel(player),
					ViewDist.NORMAL, UtilServer.getPlayers());
			
			player.setFoodLevel(2);
			player.setSprinting(false);
		}
	}

	@EventHandler
	public void on(ConditionExpireEvent event)
	{
		if (event.getCondition().GetReason().equals(GetName()) && event.getCondition().GetEnt() instanceof Player)
		{
			if (event.getCondition().GetType() == ConditionType.DAMAGE_RESISTANCE)
			{
				Player player = ((Player) event.getCondition().GetEnt());
				if (_foodLevel.get(player) != null)
				{
					player.setFoodLevel(_foodLevel.get(player));
				}
				// Could be null value. Sanity check
				_foodLevel.remove(player);
			}
		}
	}

	@EventHandler
	public void on(PlayerQuitEvent event)
	{
		_foodLevel.remove(event.getPlayer());
	}

	@Override
	public void Reset(Player player)
	{
		player.setFoodLevel(20);
	}
}
