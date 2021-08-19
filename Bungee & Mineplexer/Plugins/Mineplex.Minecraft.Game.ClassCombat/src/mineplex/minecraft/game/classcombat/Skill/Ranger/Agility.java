package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Agility extends SkillActive
{
	private HashSet<Player>	_active = new HashSet<Player>();

	public Agility(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Sprint with great agility, gaining",
				"Speed 2 for #3#1 seconds. You take",
				"#45#5 % less damage and take no knockback.",
				"",
				"Agility ends if you Left-Click."	
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
		Factory.Condition().Factory().Speed(GetName(), player, player, 3 + level, 1, false, true, true);
		_active.add(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 0.5f, 0.5f);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void End(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (event.getAction() == Action.PHYSICAL)
			return;

		if (!_active.contains(player))
			return;
		
		//Remove
		_active.remove(player);
		player.removePotionEffect(PotionEffectType.SPEED);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
			return;

		if (!damagee.isSprinting())
			return;

		if (!_active.contains(damagee))
			return;

		//Cancel
		event.AddMult(GetName(), GetName(), (0.55 - 0.05 * getLevel(damagee)), false);
		
		event.SetKnockback(false);
		
		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.BLAZE_BREATH, 0.5f, 2f);
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		HashSet<Player>	expired = new HashSet<Player>();
		for (Player cur : _active)
			if (!cur.hasPotionEffect(PotionEffectType.SPEED))
				expired.add(cur);

		for (Player cur : expired)
			_active.remove(cur);
	}
	
	@EventHandler
	public void Particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : _active)
		{
			if (player.isSprinting())
				UtilParticle.PlayParticle(ParticleType.SPELL, player.getLocation(), 
					(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 4,
					ViewDist.LONG, UtilServer.getPlayers());
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
