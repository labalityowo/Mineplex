package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class FreezingBlast extends SkillActive implements IThrown
{
	public FreezingBlast(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Launch a freezing blast;",
				"Creates long lasting Snow, and",
				"gives Slow 4 to nearby players.",
				"",
				"Direct hit applies Frost Armor,",
				"giving Protection 4 and Slow 4",
				"for 10 seconds.",
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
				Sound.FIZZ, 0.6f, 1.6f, null, 0, UpdateType.FASTEST, 1f);

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

		//Snow Spread
		HashMap<Block, Double> blocks = UtilBlock.getInRadius(data.getThrown().getLocation(), 4d);
		for (Block cur : blocks.keySet()) 
		{
			if (UtilBlock.airFoliage(cur) && UtilBlock.solid(cur.getRelative(BlockFace.DOWN)))
			{
				Factory.BlockRestore().snow(cur, (byte) (7 * blocks.get(cur)), (byte) 7, (long) (15000 * (1 + blocks.get(cur))), 1000, 0);
				cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, 80);
			}
		}

		//Slow Players
		for (Player curPlayer : UtilPlayer.getNearby(data.getThrown().getLocation(), 4))
			Factory.Condition().Factory().Slow(GetName(), curPlayer, player, 2.9, 0, false, true, true, true);

		if (target == null)
			return;

		//Freeze
		Factory.Condition().Factory().Protection(GetName(), target, player, 10, 3, false, true, true);
		Factory.Condition().Factory().Slow(GetName(), target, player, 10, 3, false, true, true, true);

		//Inform
		UtilPlayer.message(target, F.main(GetClassType().name(), F.name(player.getName()) + " used " + F.skill("Frost Armor") + " on you."));

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