package mineplex.minecraft.game.classcombat.Skill.Knight;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class HiltSmash extends SkillActive
{
	private HashSet<Player>	_used = new HashSet<Player>();

	public HiltSmash(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Smash the hilt of your sword into",
				"your opponent, dealing #1#1 damage",
				"and Slow 3 for #0.5#0.5 seconds."
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
		
		if (_used.contains(player))
			return false;

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You missed " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Miss(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		_used.clear();
	}

	public boolean CanUse(Player player)
	{
		int level = getLevel(player);
		if (level == 0)
			return false;

		//Check Material
		if (!_itemSet.contains(player.getItemInHand().getType()))
			return false;

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		UtilServer.getServer().getPluginManager().callEvent(trigger);
		if (trigger.IsCancelled())
			return false;

		//Check Energy/Recharge
		if (!EnergyRechargeCheck(player, level))
			return false;
		
		//Water
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		//Allow
		return true;
	}

	@EventHandler
	public void Hit(PlayerInteractEntityEvent event)
	{
		if (event.isCancelled())
			return;
	
		if (isInWater(event.getPlayer()))
			return;
		
		Player player = event.getPlayer();

		//Level
		int level = getLevel(player);
		if (level == 0)			return;

		if (!CanUse(player))
			return;

		Entity ent = event.getRightClicked();

		if (UtilPlayer.isSpectator(ent))
			return;
		
		if (ent == null)
			return;

		if (!(ent instanceof LivingEntity))
			return;
		
		if (UtilMath.offset(player, ent) > 4)
		{
			UtilPlayer.message(player, F.main(GetClassType().name(), "You missed " + F.skill(GetName()) + "."));
			return;
		}

		//Set Used
		_used.add(player);

		//Damage Event
		Factory.Damage().NewDamageEvent((LivingEntity)ent, player, null, 
				DamageCause.CUSTOM, 1 + level, false, true, false,
				player.getName(), GetName());
		
		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 1f, 1.4f);
	}

	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.CUSTOM)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		int level = getLevel(damager);
		if (level == 0)			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
			return;

		//Condition
		Factory.Condition().Factory().Slow(GetName(), damagee, damager, 0.5 + 0.5 * level, 2, false, true, true, true);

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.ZOMBIE_WOOD, 1f, 1.2f);

		//Inform
		UtilPlayer.message(damager, F.main(GetClassType().name(), "You used " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main(GetClassType().name(), F.name(damager.getName()) + " hit you with " + F.skill(GetName(level)) + "."));
	}

	@Override
	public void Reset(Player player) 
	{
		_used.remove(player);
	}
}
