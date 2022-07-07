package mineplex.minecraft.game.classcombat.Skill.Ranger;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillChargeSword;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class WolfsPounce extends SkillChargeSword
{
	private NautHashMap<Player, Long> _live = new NautHashMap<Player, Long>();
	private NautHashMap<Player, Float> _chargeStore = new NautHashMap<Player, Float>();

	public WolfsPounce(SkillFactory skills, String name, 
			ClassType classType, SkillType skillType, 
			int cost, int maxLevel)
	{
		super(skills, name, classType, skillType, cost, maxLevel, 
				0.012f, 0.008f, 
				8000, -1000, true, true,
				false, false);

		SetDesc(new String[] 
				{
				"Hold Block to charge pounce.",
				"Release Block to pounce.",
				"",
				GetChargeString(),
				"Taking damage cancels charge.",
				"",
				"Colliding with another player",
				"mid-air deals up to #2#1 damage",
				"and Slow 2 for 3 seconds."
				
				});
	}
	
	@Override
	public String GetRechargeString() 
	{
		return "Recharge: " + "#8#-1 Seconds";
	}

	@Override
	public void DoSkillCustom(Player player, float charge)
	{
		//Action
		UtilAction.velocity(player, 0.4 + (1.4*charge), 0.2, 0.4 + (0.9*charge), true);
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(getLevel(player))) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WOLF_BARK, 1f, 0.8f + (1.2f * charge));
		
		_chargeStore.put(player, charge);
	}

	@EventHandler
	public void CheckCollide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//End
		for (Player player : GetUsers())
		{
			if (!UtilEnt.onBlock(player))
				continue;

			if (!_live.containsKey(player))
				continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))  
				continue;

			_live.remove(player);			
		}	

		//Collide
		for (Player player : GetUsers())
		{
			if (!_live.containsKey(player))
				continue;

			for (Player other : player.getWorld().getPlayers())
			{
				if (UtilPlayer.isSpectator(other))
					continue;

				if (other.equals(player))
					continue;

				if (!Factory.Relation().canHurt(player, other))
					continue;

				if (UtilMath.offset(player, other) > 2)
					continue;


				HandleCollide(player, other);
				_live.remove(player);
				return;
			}			
		}
	}

	public void HandleCollide(Player damager, LivingEntity damagee)
	{
		float charge = 0.1f;
		if (_chargeStore.containsKey(damager))
			charge = _chargeStore.remove(damager);
		
		int damage = (int)((2 + getLevel(damager)) * charge);

		//Damage Event
		Factory.Damage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, damage, true, true, false,
				damager.getName(), GetName());	

		//Conditions
		Factory.Condition().Factory().Slow(GetName(), damagee, damagee, 3, 1, false, true, true, true);

		//Inform
		UtilPlayer.message(damager, F.main(GetClassType().name(), "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main(GetClassType().name(), F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));

		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.WOLF_BARK, 0.5f, 0.5f);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void DamageCancelCharge(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		//Damagee
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		if (_charge.remove(damagee) == null)
			return;
		
		//Inform
		UtilPlayer.message(damagee, F.main(GetClassType().name(), F.skill(GetName()) + " was interrupted."));

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.WOLF_WHINE, 0.6f, 1.2f);

	}

	@Override
	public void Reset(Player player) 
	{
		_charge.remove(player);
		_live.remove(player);
	}
}