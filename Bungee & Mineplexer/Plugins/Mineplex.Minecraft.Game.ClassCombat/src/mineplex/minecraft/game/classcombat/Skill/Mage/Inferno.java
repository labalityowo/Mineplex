package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;

public class Inferno extends SkillActive
{
	private HashSet<Player> _active = new HashSet<Player>();
	
	public Inferno(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Hold Block to use Inferno;",
				"You spray fire at #1.05#0.15 velocity,",
				"igniting enemies for #0.3#0.1 ."
				});
	}
	
	@Override
	public String GetEnergyString()
	{
		return "Energy: #34#-1 per Second";
	}
	
	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " .in water."));
			return false;
		}

		return true;
	}
	
	@Override
	public void Skill(Player player, int level)
	{
		_active.add(player);
	}

	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur))
				continue;
			
			if (!UtilPlayer.isBlocking(cur))
			{
				_active.remove(cur);
				continue;
			}
  
			//Level
			int level = getLevel(cur);
			if (level == 0)		
			{
				_active.remove(cur);
				continue;
			}
			
			//Energy
			if (!Factory.Energy().Use(cur, GetName(), 1.7 - (0.05 * level), true, false))
			{
				_active.remove(cur);
				continue;
			} 

			//Fire
			ItemStack itemStack = ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER, 1);
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(ChatColor.RESET + "" + UtilEnt.getNewEntityId(false));
			itemStack.setItemMeta(meta);
			
			Item fire = cur.getWorld().dropItem(cur.getEyeLocation().add(cur.getLocation().getDirection()), itemStack);
			Factory.Fire().Add(fire, cur, 0.7, 0, 0.3 + (0.1 * level), .375, GetName(), false);

			fire.teleport(cur.getEyeLocation());
			double x = 0.07 - (UtilMath.r(14)/100d);
			double y = 0.07 - (UtilMath.r(14)/100d);
			double z = 0.07 - (UtilMath.r(14)/100d);
			fire.setVelocity(cur.getLocation().getDirection().add(new Vector(x,y,z)).multiply(1.05 + (0.15 * level)));

			//Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.GHAST_FIREBALL, 0.1f, 1f);
		}
	}

	@EventHandler
	public void ignite(ConditionApplyEvent event)
	{
		Condition condition = event.GetCondition();
		LivingEntity entity = condition.GetEnt(), source = condition.GetSource();

		if (condition.GetType() != ConditionType.BURNING || source == null)
		{
			return;
		}

		if (!(entity instanceof Player) || !(source instanceof Player))
		{
			return;
		}

		if (!Factory.Relation().canHurt((Player) source, (Player) entity))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
