package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class IcePrison extends SkillActive implements IThrown
{
	public IcePrison(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Launch an icy orb. When it collides,",
				"it creates a hollow sphere of ice",
				"thats lasts for #3#1 seconds.",
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
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(79));
		item.setVelocity(player.getLocation().getDirection());
		Factory.Projectile().AddThrow(item, player, this, -1, true, true, true, true, 
				Sound.FIZZ, 0.6f, 1.6f, ParticleType.SNOW_SHOVEL, UpdateType.TICK, 1.5f); 
 
		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + ".")); 

		//Effect
		item.getWorld().playSound(item.getLocation(), Sound.SILVERFISH_HIT, 2f, 1f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Action(target, data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Action(null, data);	
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Action(null, data);	
	}

	public void Action(LivingEntity target, ProjectileUser data)
	{
		//Effect
		data.getThrown().getWorld().playEffect(data.getThrown().getLocation(), Effect.STEP_SOUND, 79);

		//Remove
		data.getThrown().remove();

		//Thrower
		if (!(data.getThrower() instanceof Player))
			return;

		Player player = (Player)data.getThrower();

		//Level
		int level = getLevel(player);
		if (level == 0)				return;

		Block block = data.getThrown().getLocation().getBlock();

		//Sphere
		HashMap<Block, Double> blocks = UtilBlock.getInRadius(block, 3.8d);
		// To save having to calculate everything again
		Set<Block> acceptable = new HashSet<>();
		boolean failed = false;
		for (Block cur : blocks.keySet()) 
		{
			if (!UtilBlock.airFoliage(cur))
				continue;

			if (UtilMath.offset(block.getLocation(), cur.getLocation()) <= 2.9)
				continue;
			
			//Leave roof hole
			if (cur.getX() == block.getX() && cur.getZ() == block.getZ() && cur.getY() > block.getY())
				continue;

			if (getLocationFilter().accept(cur.getLocation()))
			{
				acceptable.add(cur);
			}
			else
			{
				failed = true;
				break;
			}
		}

		if (!failed)
		{
			for (Block cur : acceptable)
			{
				FreezeBlock(cur, block, level);
			}
		}
		else
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " here."));
		}

		/*
		FreezeBlock(block.getRelative(3, 0, 3), block, level);
		FreezeBlock(block.getRelative(-3, 0, -3), block, level);
		FreezeBlock(block.getRelative(-3, 0, 3), block, level);
		FreezeBlock(block.getRelative(3, 0, -3), block, level);
		*/
	}
	
	public void FreezeBlock(Block freeze, Block mid, int level)
	{
		if (!UtilBlock.airFoliage(freeze))
			return;
		
		long time = 3500 + (1000 * level);
		
		int yDiff = freeze.getY() - mid.getY();
		
		time -= yDiff * 1000 - Math.random() * 1000;

		Factory.BlockRestore().add(freeze, 79, (byte) 0, time);
		freeze.getWorld().playEffect(freeze.getLocation(), Effect.STEP_SOUND, 79);
	}
	
	@EventHandler
	public void BlockFade(BlockFadeEvent event)
	{
		if (event.getBlock().getType() == Material.ICE)
			event.setCancelled(true);
	}

	@Override
	public void Reset(Player player) 
	{

	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}