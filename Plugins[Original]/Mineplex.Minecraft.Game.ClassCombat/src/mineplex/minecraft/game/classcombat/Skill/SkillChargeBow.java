package mineplex.minecraft.game.classcombat.Skill;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;

public abstract class SkillChargeBow extends SkillCharge implements Listener
{	
	protected boolean _canChargeInWater;
	protected boolean _canChargeInAir;
	

	public SkillChargeBow(SkillFactory skills, String name, ClassType classType,
			SkillType skillType, int cost, int maxLevel,
			float base, float boost, boolean inWater, boolean inAir)
	{
		super(skills, name, classType, skillType, cost, maxLevel, base, boost);
		
		_canChargeInWater = inWater;
		_canChargeInAir = inAir;
	}

	@EventHandler
	public void ChargeBowInit(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilGear.isMat(event.getItem(), Material.BOW))
			return;

		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		if (!player.getInventory().contains(Material.ARROW))
			return;
		
		//Flags
		if (!_canChargeInAir && !UtilEnt.isGrounded(player))
			return;

		if (!_canChargeInWater && player.getLocation().getBlock().isLiquid())
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		//Level
		int level = getLevel(player);
		if (level == 0)			return;

		//Start Charge
		_charge.put(player, 0f);
		_chargeStart.put(player, System.currentTimeMillis());
	}
	
	@EventHandler
	public void ChargeBow(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			//Not Charging
			if (!_charge.containsKey(cur))
				continue;
			
			if (!_chargeStart.containsKey(cur))
			{
				_charge.remove(cur);
				continue;
			}
			
			//Level
			int level = getLevel(cur);
			if (level == 0)			
			{
				_charge.remove(cur);
				continue;
			}
			
			//No Longer Holding Bow
			if (!UtilGear.isMat(cur.getItemInHand(), Material.BOW))
			{
				_charge.remove(cur);
				continue;
			}
			
			// Client sends a packet when you stop charging a bow and sets this datawatcher value.
			if (!UtilPlayer.isChargingBow(cur))
			{
				_charge.remove(cur);
				continue;
			}
			
			//Flags
			if (!_canChargeInAir && !UtilEnt.isGrounded(cur))
				continue;

			if (!_canChargeInWater && cur.getLocation().getBlock().isLiquid())
				continue;
				
			//Delay
			if (!UtilTime.elapsed(_chargeStart.get(cur), 1000))
				continue;

			float charge = _charge.get(cur);
			
			//Increase Charge
			charge = Math.min(1f, charge + _rateBase + (_rateBoost * level));
			_charge.put(cur, charge);
			
			//Display
			DisplayProgress(cur, GetName(), charge);
		}
	}
	
	@EventHandler
	public void TriggerBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();

		if (!_charge.containsKey(player))
			return;

		DoSkill(player, _charge.remove(player), (Arrow)event.getProjectile());
	}
	
	public void DoSkill(Player player, float charge, Arrow arrow)
	{
		player.setExp(0f);
		
		DoSkillCustom(player, charge, arrow);
	}
	
	public abstract void DoSkillCustom(Player player, float charge, Arrow arrow);
	
	@Override
	public void Reset(Player player)
	{
		_charge.remove(player);
	}
}
