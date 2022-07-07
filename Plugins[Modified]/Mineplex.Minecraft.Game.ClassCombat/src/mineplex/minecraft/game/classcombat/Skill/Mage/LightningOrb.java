package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class LightningOrb extends SkillActive implements IThrown
{
	public static class LightningOrbEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		private final List<LivingEntity> _struck;

		LightningOrbEvent(Player who, List<LivingEntity> struck)
		{
			super(who);

			_struck = struck;
		}

		public List<LivingEntity> getStruck()
		{
			return _struck;
		}
	}

	public LightningOrb(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Launch a lightning orb. Upon a direct",
				"hit with player, or 1.7 seconds, it will",
				"strike all enemies within #3#0.5 Blocks ",
				"with lightning, dealing #4#1 damage and",
				"giving Slow 2 for 4 seconds."
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
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),new ItemBuilder(Material.DIAMOND_BLOCK).setTitle(UtilMath.random.nextDouble() + "").build());
		item.setVelocity(player.getLocation().getDirection());
		Factory.Projectile().AddThrow(item, player, this, 1700, true, true, false, false, 
				Sound.FIZZ, 0.6f, 1.6f, ParticleType.FIREWORKS_SPARK, UpdateType.TICK, 0.4f);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));

		//Effect 
		item.getWorld().playSound(item.getLocation(), Sound.SILVERFISH_HIT, 2f, 1f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Strike(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Strike(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Strike(data);
	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void Strike(ProjectileUser data)
	{
		//Remove
		data.getThrown().remove();

		//Thrower
		if (!(data.getThrower() instanceof Player))
			return;

		Player player = (Player)data.getThrower();

		//Level
		int level = getLevel(player);
		if (level == 0)				return;

		List<LivingEntity> struck = new ArrayList<>();

		UtilEnt.getInRadius(data.getThrown().getLocation(), 3 + 0.5 * level).forEach((cur, scale) ->
		{
			if (cur.equals(player) || (cur instanceof Player && !Factory.Relation().canHurt(player, (Player) cur)))
			{
				return;
			}

			//Inform
			UtilPlayer.message(cur, F.main(GetClassType().name(), F.name(player.getName()) + " hit you with " + F.skill(GetName(level)) + "."));

			//Lightning
			cur.getWorld().strikeLightningEffect(cur.getLocation());

			//Damage Event
			Factory.Damage().NewDamageEvent(cur, player, null, DamageCause.LIGHTNING, 4 + level, false, true, false, player.getName(), GetName());
			Factory.Condition().Factory().Slow(GetName(), cur, player, 4, 1, false, true, true, true);
			Factory.Condition().Factory().Shock(GetName(), cur, player, 1, false, true);

			struck.add(cur);
		});

		Bukkit.getPluginManager().callEvent(new LightningOrbEvent(player, struck));
	}
	
	@EventHandler
	public void CancelFire(BlockIgniteEvent event)
	{
		if (event.getCause() == IgniteCause.LIGHTNING)
			event.setCancelled(true);
	}
	
	@Override
	public void Reset(Player player) 
	{

	}
}