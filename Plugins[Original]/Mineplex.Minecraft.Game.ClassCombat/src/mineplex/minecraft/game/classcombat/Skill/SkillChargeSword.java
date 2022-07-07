package mineplex.minecraft.game.classcombat.Skill;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public abstract class SkillChargeSword extends SkillCharge implements Listener
{
	protected boolean _canChargeInWater;
	protected boolean _canChargeInAir;
	
	protected long _recharge = 2000;
	protected long _rechargePerLevel = 0;
	protected boolean _rechargeInform = false;
	protected boolean _rechargeAttach = false;
	
	protected boolean _fireOnFull = false;
	
	public SkillChargeSword(SkillFactory skills, String name, ClassType classType,
			SkillType skillType, int cost, int maxLevel,
			float base, float boost, 
			long recharge, long rechargePerLevel, boolean inform, boolean attach,
			boolean inWater, boolean inAir)
	{
		super(skills, name, classType, skillType, cost, maxLevel, base, boost);

		_canChargeInWater = inWater;
		_canChargeInAir = inAir;
		
		_recharge = recharge;
		_rechargePerLevel = rechargePerLevel;
		_rechargeInform = inform;
		_rechargeAttach = attach;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void ChargeBlock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{	
			//Charge
			if (UtilPlayer.isBlocking(cur))
			{ 
				//Flags
				if (!_canChargeInAir && !UtilEnt.isGrounded(cur))
					continue;

				if (!_canChargeInWater && cur.getLocation().getBlock().isLiquid())
					continue;
				
				//Check Allowed
				SkillTriggerEvent triggerEvent = new SkillTriggerEvent(cur, GetName(), GetClassType());
				Bukkit.getServer().getPluginManager().callEvent(triggerEvent);
				
				if (triggerEvent.IsCancelled())
					continue;
				
				//Recharged Check (uses recharge upon activation)	
				if (!_charge.containsKey(cur))
					if (!Recharge.Instance.usable(cur, GetName()))
						continue;
				
				//Charge
				if (Charge(cur) && _fireOnFull)
				{
					//Action
					float charge = _charge.remove(cur);
					
					//Set Recharge
					Recharge.Instance.recharge(cur, GetName());
					Recharge.Instance.use(cur, GetName(), _recharge + (getLevel(cur) * _rechargePerLevel), true, true);
					
					DoSkill(cur, charge);
				}
					
			}
			//Release Charge
			else if (_charge.containsKey(cur))
			{
				//Action
				float charge = _charge.remove(cur);
				
				//Set Recharge
				Recharge.Instance.recharge(cur, GetName());
				Recharge.Instance.use(cur, GetName(), _recharge + (getLevel(cur) * _rechargePerLevel), true, true);
				
				DoSkill(cur, charge);
			}
		}
	}
	
	public void DoSkill(Player player, float charge)
	{
		player.setExp(0f);
		
		DoSkillCustom(player, charge);
	}
	
	public abstract void DoSkillCustom(Player player, float charge);
	
	@Override
	public void Reset(Player player)
	{
		_charge.remove(player);
	}
}
