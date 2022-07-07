package mineplex.minecraft.game.classcombat.Skill;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class SkillActive extends Skill implements Listener
{
	protected int _energy;
	protected int _energyMod;

	protected long _recharge;
	protected long _rechargeMod;
	protected boolean _rechargeInform;

	protected final Set<Material> _itemSet = new HashSet<>();
	protected final Set<Action> _actionSet = new HashSet<>();

	public SkillActive(SkillFactory skills,
			String name, ClassType classType, SkillType skillType,
			int cost, int levels,
			int energy, int energyMod, 
			long recharge, long rechargeMod, boolean rechargeInform, 
			Material[] itemArray, Action[] actionArray)
	{
		super(skills, name, classType, skillType, cost, levels);

		_energy = energy;
		_energyMod = energyMod;

		_recharge = recharge;
		_rechargeMod = rechargeMod;
		_rechargeInform = rechargeInform;

		Collections.addAll(_itemSet, itemArray);
		Collections.addAll(_actionSet, actionArray);
	}

	public Set<Material> GetItems()
	{
		return _itemSet;
	}

	public Set<Action> GetActions()
	{
		return _actionSet;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Interact(PlayerInteractEvent event)
	{
		SkillCheck(event.getPlayer(), event.getAction(), event.getClickedBlock());
	}

	public boolean SkillCheck(Player player, Action action, Block block)
	{
		int level = getLevel(player);

		if (level == 0)
			return false;

		//Check Block
		if (UtilBlock.usable(block))
			return false;

		//Check Action
		if (!_actionSet.contains(action))
			return false;

		//Check Material
		if (!_itemSet.contains(player.getItemInHand().getType()))
			return false;
		
		//Unique Weapon
		if (player.getItemInHand().getEnchantments().containsKey(Enchantment.ARROW_DAMAGE))
			return false;

		//Check Allowed
		SkillTriggerEvent event = new SkillTriggerEvent(player, GetName(), GetClassType());
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.IsCancelled())
			return false;

		//Custom Check
		if (!CustomCheck(player, level))
			return false;

		//Check Energy/Recharge
		if (!EnergyRechargeCheck(player, level))
			return false;

		//Do Ability
		Skill(player, level);
		return true;
	}

	public abstract boolean CustomCheck(Player player, int level);

	public abstract void Skill(Player player, int level);

	public boolean EnergyRechargeCheck(Player player, int level)
	{
		//Check Energy - DO NOT USE YET
		if (!Factory.Energy().Use(player, GetName(level), Energy(level), false, true))
			return false;

		//Use Recharge
		if (!Recharge.Instance.use(player, GetName(), GetName(level), Recharge(level), _rechargeInform, true))
			return false;

		//Use Energy
		Factory.Energy().Use(player, GetName(level), Energy(level), true, true);

		return true;
	}

	public int Energy(int level) 
	{
		return _energy + (_energyMod*level);
	}

	public long Recharge(int level)
	{
		return _recharge + (_rechargeMod*level);
	}

	@Override
	public String GetEnergyString()
	{
		if (_energy == 0)
			return null;
		
		if (_energyMod != 0)
			return "Energy: " + "#" + _energy + "#" + _energyMod; 

		else
			return "Energy: " + _energy;
	}

	@Override
	public String GetRechargeString()
	{
		if (_recharge == 0)
			return null;
		
		if (_rechargeMod != 0)
			return "Recharge: " + "#" + UtilMath.trim(1, _recharge/1000d) + "#" + UtilMath.trim(1, _rechargeMod/1000d) + " Seconds";

		else
			return "Recharge: " + UtilMath.trim(1, _recharge/1000d) + " Seconds";
	}
}
