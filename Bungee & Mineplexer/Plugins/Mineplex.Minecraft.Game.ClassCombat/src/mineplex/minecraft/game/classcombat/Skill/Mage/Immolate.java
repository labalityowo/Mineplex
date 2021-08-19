package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class Immolate extends Skill
{
	private HashSet<Entity> _active = new HashSet<Entity>();

	public Immolate(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Drop Axe/Sword to Toggle.",
				"",
				"Ignite yourself in flaming fury.",
				"You receive Strength #1#1 , Speed 1,",
				"Fire Resistance",
				"",
				"You leave a trail of fire, which",
				"ignites players for #0.25#0.25 seconds."
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: #15#-1 per Second";
	}

	@EventHandler
	public void Toggle(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (getLevel(player) == 0)				
			return;

		if (!UtilGear.isWeapon(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		UtilServer.getServer().getPluginManager().callEvent(trigger);
		if (trigger.IsCancelled())
			return;

		if (_active.contains(player))
		{
			Remove(player);	
		}
		else
		{
			if (!Factory.Energy().Use(player, "Enable " + GetName(), 10, true, true))
				return;

			Add(player);
		}
	}

	public void Add(Player player)
	{
		_active.add(player);
		Conditions();
		UtilPlayer.message(player, F.main(GetClassType().name(), GetName() + ": " + F.oo("Enabled", true)));
	}

	public void Remove(Player player)
	{
		_active.remove(player);
		UtilPlayer.message(player, F.main(GetClassType().name(), GetName() + ": " + F.oo("Disabled", false)));
		
		Factory.Condition().EndCondition(player, null, GetName());
		Factory.Condition().Factory().FireResist(GetName(), player, player, 1.9, 0, false, true, true);
	}

	@EventHandler
	public void Aura(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{	
			if (!_active.contains(cur))
				continue;

			//Level
			int level = getLevel(cur);
			if (level == 0)
			{
				Remove(cur);	
				continue;
			}

			//Check Allowed
			SkillTriggerEvent trigger = new SkillTriggerEvent(cur, GetName(), GetClassType());
			UtilServer.getServer().getPluginManager().callEvent(trigger);
			if (trigger.IsCancelled())
			{
				Remove(cur);
				continue;
			}
			
			if (cur.getLocation().getBlock().getType() == Material.WATER || cur.getLocation().getBlock().getType() == Material.STATIONARY_WATER)
			{
				Remove(cur);
				continue;
			}

			//Energy
			if (!Factory.Energy().Use(cur, GetName(), 0.65 - (level * 0.05), true, true))
			{
				Remove(cur);
				continue;
			}	

			//Put out Fire
			cur.setFireTicks(0);
		}
	}

	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		if (_active.contains(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
			Conditions();

		if (event.getType() == UpdateType.TICK)
			Flames();
	}

	public void Conditions()
	{
		for (Player cur : GetUsers())
		{	
			if (!_active.contains(cur))
				continue;
			
			int level = getLevel(cur);

			Factory.Condition().Factory().Speed(GetName(), cur, cur, 1.9, 0, false, true, true);
			Factory.Condition().Factory().Strength(GetName(), cur, cur, 1.9, level, false, true, true);
			Factory.Condition().Factory().FireResist(GetName(), cur, cur, 1.9, 0, false, true, true);
			
			for (Player other : UtilPlayer.getNearby(cur.getLocation(), 6))
			{
				if (cur.equals(other))
				{
					continue;
				}
				else if (Factory.Relation().canHurt(cur, other))
				{
					continue;
				}
				
				Factory.Condition().Factory().FireResist("Allied Immolate", other, cur, 2, 0, true, true, false);
			}
		}
	}

	public void Flames()
	{
		for (Player cur : GetUsers())
		{	
			if (!_active.contains(cur))
				continue;

			int level = getLevel(cur);
			
			//Fire
			ItemStack itemStack = ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER, 1);
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(ChatColor.RESET + "" + UtilEnt.getNewEntityId(false));
			itemStack.setItemMeta(meta);
			
			Item fire = cur.getWorld().dropItem(cur.getLocation().add(0, 0.5, 0), itemStack);
			fire.setVelocity(new Vector((Math.random() - 0.5)/3,Math.random()/3,(Math.random() - 0.5)/3));
			Factory.Fire().Add(fire, cur, 2, 0, 0.25 + (level * 0.25), 0, GetName(), false);

			//Sound
			cur.getWorld().playSound(cur.getLocation(), Sound.FIZZ, 0.2f, 1f);
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_active.remove(player);
	}
}
