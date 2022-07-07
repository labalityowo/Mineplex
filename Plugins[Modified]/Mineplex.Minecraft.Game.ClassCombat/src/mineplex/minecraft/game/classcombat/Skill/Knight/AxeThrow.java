package mineplex.minecraft.game.classcombat.Skill.Knight;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class AxeThrow extends SkillActive implements IThrown
{
	private HashMap<Item, Player> _thrown = new HashMap<Item, Player>();
	
	public AxeThrow(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Throw your axe with #0.7#0.1 velocity, ",
				"dealing #4.5#0.5 damage.",
				"",
				"You pull your axe back to you when it",
				"collides with anything.",
				"",
				"Your axe is returned to you if you do",
				"not pick it up within #15#-1 seconds."
				});
		
		setAchievementSkill(true);
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
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		
		//Throw
		Item item = player.getWorld().dropItem(player.getEyeLocation(), player.getItemInHand());
		UtilAction.velocity(item, player.getLocation().getDirection(), 0.7 + 0.1 * level, false, 0, 0.2, 10, true);
		
		player.setItemInHand(null);
		
		//Projectile
		Factory.Projectile().AddThrow(item, player, this, -1, true, true, true, false, 0.4f);
		
		//Store
		_thrown.put(item, player);
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Rebound(data.getThrower(), data.getThrown());
		
		if (target == null)
			return;
		
		int level = getLevel(data.getThrower());
		if (level <= 0)
			return;
		
		double damage = 4.5 + 0.5 * level;
		
		//Damage Event
		Factory.Damage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.CUSTOM, damage, true, true, false,
				UtilEnt.getName(data.getThrower()), GetName());
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Rebound(data.getThrower(), data.getThrown());
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Rebound(data.getThrower(), data.getThrown());
	}

	public void Rebound(LivingEntity player, Entity ent)
	{
		ent.getWorld().playSound(ent.getLocation(), Sound.ZOMBIE_WOOD, 0.6f, 0.5f);
		
		double mult = 0.5 + (0.6 * (UtilMath.offset(player.getLocation(), ent.getLocation())/16d));
		
		//Velocity
		ent.setVelocity(player.getLocation().toVector().subtract(ent.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));
		
		//Ticks
		if (ent instanceof Item)
			((Item)ent).setPickupDelay(5);
	}
	
	@EventHandler
	public void pickup(PlayerPickupItemEvent event)
	{
		if (!_thrown.containsKey(event.getItem()))
			return;
		
		event.setCancelled(true);
		
		Player player = _thrown.remove(event.getItem());
		
		player.getInventory().addItem(event.getItem().getItemStack());
		
		UtilInv.Update(event.getPlayer());
		
		event.getItem().remove();
		
		player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1f, 1f);
	}
	
	@EventHandler
	public void timeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		Iterator<Item> itemIterator = _thrown.keySet().iterator();
		
		while (itemIterator.hasNext())
		{
			Item item = itemIterator.next();
			
			Player player = _thrown.get(item);
			if (!player.isOnline())
			{
				item.remove();
				itemIterator.remove();
				continue;
			}
			
			int level = getLevel(player);
			
			if (item.getTicksLived() > 300 - level * 20)
			{
				_thrown.get(item).getInventory().addItem(item.getItemStack());
				item.remove();
				itemIterator.remove();
			}
		}
	}
	
	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		Iterator<Item> i = _thrown.keySet().iterator();
		
		while (i.hasNext())
		{
			Item item = i.next();
			Player player = _thrown.get(item);
			
			if (player.equals(event.getEntity()))
			{
				i.remove();
				item.remove();
			}
		}
	}
	
	@Override
	public void Reset(Player player) 
	{
		Iterator<Item> i = _thrown.keySet().iterator();
		
		while (i.hasNext())
		{
			Item item = i.next();
			Player thrower = _thrown.get(item);
			
			if (player.equals(thrower))
			{
				i.remove();
				item.remove();
			}
		}
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}